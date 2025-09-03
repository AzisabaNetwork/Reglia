package dev.felnull.reglia;

import org.bukkit.plugin.Plugin;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class FormatRegistry {
    private final Plugin plugin;
    private final Map<String, FormatSpec> specs = new HashMap<>();
    private final File formatsDir;

    public FormatRegistry(Plugin plugin) {
        this.plugin = plugin;
        this.formatsDir = new File(plugin.getDataFolder(), "formats");
        if (!formatsDir.exists()) formatsDir.mkdirs();
    }

    /** 全フォーマットを読み込み */
    public synchronized void loadAll() {
        specs.clear();
        File[] files = formatsDir.listFiles((dir, name) -> name.endsWith(".yml") || name.endsWith(".yaml"));
        if (files == null) return;

        Yaml yaml = new Yaml();
        for (File f : files) {
            try (FileInputStream fis = new FileInputStream(f)) {
                Map<String, Object> raw = yaml.load(fis);
                if (raw == null) continue;

                FormatSpec spec = new FormatSpec();
                spec.name = getStr(raw, "name", fileBase(f.getName()));
                spec.format = getStr(raw, "format", null);

                // --- channel フィールドを単数/複数どちらも許容 ---
                Object chs = raw.get("channel");
                if (chs instanceof Iterable<?>) {
                    Iterable<?> it = (Iterable<?>) chs;
                    java.util.List<String> list = new java.util.ArrayList<>();
                    for (Object o : it) {
                        if (o != null) list.add(String.valueOf(o).trim());
                    }
                    spec.channels = list;
                } else if (chs instanceof String) {
                    String str = ((String) chs).trim();
                    // "[id1, id2]" のような配列文字列を許容
                    if (str.startsWith("[") && str.endsWith("]")) {
                        str = str.substring(1, str.length() - 1);
                        java.util.List<String> list = new java.util.ArrayList<>();
                        for (String p : str.split(",")) {
                            if (!p.trim().isEmpty()) list.add(p.trim());
                        }
                        spec.channels = list;
                    } else {
                        spec.channels = java.util.Collections.singletonList(str);
                    }
                }

                Object pr = raw.get("priority");
                if (pr instanceof Number) spec.priority = ((Number) pr).intValue();

                if (spec.isValid()) {
                    specs.put(spec.name.toLowerCase(Locale.ROOT), spec);
                } else {
                    plugin.getLogger().warning("[Reglia] Invalid format file: " + f.getName());
                }
            } catch (Exception e) {
                plugin.getLogger().warning("[Reglia] Failed to load " + f.getName() + ": " + e.getMessage());
            }
        }
        plugin.getLogger().info("[Reglia] Loaded formats: " + specs.keySet());
    }


    /** 再読み込み */
    public synchronized void reload() {
        loadAll();
    }

    public synchronized FormatSpec get(String name) {
        return specs.get(name.toLowerCase(Locale.ROOT));
    }

    private static String getStr(Map<String,Object> raw, String key, String def) {
        Object o = raw.get(key);
        return (o == null) ? def : String.valueOf(o);
    }

    private static String fileBase(String n) {
        int i = n.lastIndexOf('.');
        return (i > 0) ? n.substring(0, i) : n;
    }

    private static java.util.List<String> toChannelList(Object v) {
        java.util.List<String> out = new java.util.ArrayList<>();
        if (v == null) return out;

        if (v instanceof Iterable<?>) {
            for (Object o : (Iterable<?>) v) if (o != null) out.add(String.valueOf(o).trim());
        } else if (v instanceof Number) {
            out.add(String.valueOf(((Number) v).longValue()));
        } else {
            String s = String.valueOf(v).trim();
            // "[id1, id2]" みたいな配列文字列も許容
            if (s.startsWith("[") && s.endsWith("]")) {
                s = s.substring(1, s.length() - 1);
                for (String p : s.split(",")) {
                    String t = p.trim();
                    if (!t.isEmpty()) out.add(t);
                }
            } else if (!s.isEmpty()) {
                out.add(s);
            }
        }
        return out;
    }
}
