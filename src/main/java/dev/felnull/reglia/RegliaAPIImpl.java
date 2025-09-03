package dev.felnull.reglia;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/** ServicesManager に登録する実装。内部の NotifyService に委譲するだけの薄いラッパ。 */
public class RegliaAPIImpl implements RegliaAPI {
    private final NotifyService notifyService;
    private final String version;

    public RegliaAPIImpl(NotifyService notifyService, String version) {
        this.notifyService = notifyService;
        this.version = version;
    }

    @Override
    public String getApiVersion() {
        return version;
    }

    @Override
    public CompletableFuture<Boolean> notify(String formatName, String message) {
        return notifyService.notifyAsync(formatName, message);
    }

    @Override
    public CompletableFuture<Boolean> notify(String formatName, Map<String, String> data) {
        return notify(formatName, data, Collections.emptyList());
    }

    @Override
    public CompletableFuture<Boolean> notify(String formatName, Map<String, String> data, List<String> tags) {
        if (data == null) data = Collections.emptyMap();
        if (tags == null) tags = Collections.emptyList();
        Notification n = new Notification(formatName, data, /*metaServer*/ null, /*metaPlayer*/ null, tags);
        return notifyService.notifyAsync(n);
    }
}