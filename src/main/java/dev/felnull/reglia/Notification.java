package dev.felnull.reglia;

import java.util.*;

public class Notification {
    public final String formatName;
    public final Map<String,String> data;
    public final String metaServer;
    public final String metaPlayer;
    public final List<String> tags;

    public Notification(String formatName, Map<String,String> data, String metaServer, String metaPlayer, List<String> tags) {
        this.formatName = formatName;
        this.data = (data == null) ? new HashMap<>() : data;
        this.metaServer = metaServer;
        this.metaPlayer = metaPlayer;
        this.tags = (tags == null) ? new ArrayList<>() : tags;
    }

    public static Notification simple(String format, String message) {
        Map<String,String> d = new HashMap<>();
        d.put("message", message);
        return new Notification(format, d, null, null, Collections.emptyList());
    }
}
