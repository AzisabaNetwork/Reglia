package dev.felnull.reglia;

import org.bukkit.plugin.Plugin;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.*;

public class SendQueue {
    private final Plugin plugin;
    private final PriorityBlockingQueue<Job> pq = new PriorityBlockingQueue<>(256,
            Comparator.comparingInt((Job j) -> j.priority).reversed());
    private final ScheduledExecutorService worker = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "Reglia-SendQueue");
        t.setDaemon(true);
        return t;
    });

    private volatile boolean running = false;
    private int throttleMs;

    public SendQueue(Plugin plugin) {
        this.plugin = plugin;
        this.throttleMs = Math.max(50, plugin.getConfig().getInt("queue.throttleMs", 120));
    }

    public void start() {
        if (running) return;
        running = true;
        worker.scheduleWithFixedDelay(this::drainOnce, 0, throttleMs, TimeUnit.MILLISECONDS);
    }

    public void shutdown() {
        running = false;
        worker.shutdownNow();
        pq.clear();
    }

    public CompletableFuture<Boolean> enqueue(int priority, String channelId, String content, DiscordTransport transport) {
        CompletableFuture<Boolean> cf = new CompletableFuture<>();
        pq.offer(new Job(priority, channelId, content, transport, cf));
        return cf;
    }

    public CompletableFuture<Boolean> enqueue(FormatSpec spec, String content, DiscordTransport transport) {
        List<String> chans = spec.effectiveChannels();
        if (chans.isEmpty()) return CompletableFuture.completedFuture(false);
        // 互換：最初の1件だけ送る（使わないなら呼ばなくてOK）
        return enqueue(spec.priority, chans.get(0), content, transport);
    }

    private void drainOnce() {
        try {
            Job job = pq.poll();
            if (job == null) return;
            job.send();
        } catch (Exception e) {
            plugin.getLogger().warning("[Reglia] SendQueue error: " + e.getMessage());
        }
    }

    private static class Job {
        final String channelId;
        final int priority;
        final String content;
        final DiscordTransport transport;
        final CompletableFuture<Boolean> cf;

        Job(int priority, String channelId, String content, DiscordTransport transport, CompletableFuture<Boolean> cf) {
            this.channelId = Objects.requireNonNull(channelId);
            this.priority = priority;
            this.content = content;
            this.transport = transport;
            this.cf = cf;
        }

        void send() {
            // 最小実装: そのまま送信（リトライ・ドロップは後で拡張）
            transport.sendToChannelId(channelId, content)
                    .whenComplete((ok, ex) -> {
                        if (ex != null) cf.completeExceptionally(ex);
                        else cf.complete(ok != null && ok);
                    });
        }
    }
}
