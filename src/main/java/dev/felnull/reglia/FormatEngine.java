package dev.felnull.reglia;

import org.bukkit.plugin.Plugin;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

public class FormatEngine {
    private final DateTimeFormatter timeFmt;
    private final ZoneId zone;

    public FormatEngine(Plugin plugin) {
        String pat = plugin.getConfig().getString("time.pattern", "uuuu-MM-dd HH:mm:ss");
        String zid = plugin.getConfig().getString("time.zone", "Asia/Tokyo");
        this.timeFmt = DateTimeFormatter.ofPattern(pat, Locale.ROOT);
        this.zone = ZoneId.of(zid);
    }

    public String render(FormatSpec spec, Notification n) {
        String out = spec.format;
        String now = ZonedDateTime.now(zone).format(timeFmt);
        out = out.replace("{time}", now);
        out = out.replace("{server}", empty(n.metaServer));
        out = out.replace("{player}", empty(n.metaPlayer));
        out = out.replace("{tags}", n.tags == null ? "" : n.tags.stream().collect(Collectors.joining(",")));

        if (n.data != null) {
            for (Map.Entry<String,String> e : n.data.entrySet()) {
                out = out.replace("{" + e.getKey() + "}", e.getValue() == null ? "" : e.getValue());
            }
        }
        return out;
    }

    private static String empty(String s) { return s == null ? "" : s; }
}
