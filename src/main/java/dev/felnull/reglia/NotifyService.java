package dev.felnull.reglia;

import org.bukkit.plugin.Plugin;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

public class NotifyService {
    private final Plugin plugin;
    private final FormatRegistry registry;
    private final FormatEngine engine;
    private final SendQueue queue;
    private final DiscordTransport transport;

    public NotifyService(Plugin plugin, FormatRegistry registry, FormatEngine engine, SendQueue queue, BotRuntime bot) {
        this.plugin = plugin;
        this.registry = registry;
        this.engine = engine;
        this.queue = queue;
        this.transport = new DiscordTransport(bot);
    }

    public CompletableFuture<Boolean> notifyAsync(Notification n) {
        FormatSpec spec = registry.get(n.formatName);
        if (spec == null) {
            plugin.getLogger().warning("[Reglia] Unknown format: " + n.formatName);
            return CompletableFuture.completedFuture(false);
        }

        String content = engine.render(spec, n);
        List<String> chans = spec.effectiveChannels();
        if (chans.isEmpty()) return CompletableFuture.completedFuture(false);

        // 各チャンネルへ個別にジョブ投入
        List<CompletableFuture<Boolean>> futures = new java.util.ArrayList<>(chans.size());
        for (String cid : chans) {
            futures.add(queue.enqueue(spec.priority, cid, content, transport));
        }

        // 集約：全チャンネル成功で true。どれか false → false。例外があれば例外。
        return allTrueAggregate(futures);
    }

    public java.util.concurrent.CompletableFuture<Boolean> notifyAsync(String formatName, java.util.Map<String,String> data) {
        return notifyAsync(new Notification(formatName, data, null, null, java.util.Collections.emptyList()));
    }

    public java.util.concurrent.CompletableFuture<Boolean> notifyAsync(String formatName, java.lang.String message) {
        return notifyAsync(Notification.simple(formatName, message));
    }

    public java.util.concurrent.CompletableFuture<Boolean> notifyAsync(String formatName, java.util.Map<String,String> data, java.util.List<String> tags) {
        return notifyAsync(new Notification(formatName, data, null, null, tags));
    }

    /** allOf集約（どれかfalseならfalse、例外が一つでもあれば例外） */
    private static CompletableFuture<Boolean> allTrueAggregate(List<CompletableFuture<Boolean>> futures) {
        CompletableFuture<Void> all = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
        return all.thenApply(v -> {
            boolean allOk = true;
            for (CompletableFuture<Boolean> f : futures) {
                try {
                    Boolean r = f.join();
                    if (r == null || !r) allOk = false;
                } catch (CompletionException ce) {
                    // 例外は上に投げる
                    throw ce;
                }
            }
            return allOk;
        });
    }

    public FormatRegistry getRegistry() {
        return registry;
    }

}
