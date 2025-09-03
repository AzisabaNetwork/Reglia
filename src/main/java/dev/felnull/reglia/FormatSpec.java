package dev.felnull.reglia;

import java.util.List;
import java.util.Map;

public class FormatSpec {
    public String name;
    public int priority = 1;
    public String format;

    /** Discord チャンネルIDのリスト */
    public java.util.List<String> channels = new java.util.ArrayList<>();


    public java.util.List<String> placeholders;
    public java.util.Map<String, Object> embed;
    public java.util.Map<String, Object> extra;

    public boolean isValid() {
        return name != null && !name.isEmpty()
                && format != null && !format.isEmpty()
                && channels != null && !channels.isEmpty();
    }

    /** 実際に使うチャンネル一覧 */
    public java.util.List<String> effectiveChannels() {
        return (channels == null) ? java.util.Collections.emptyList() : channels;
    }
}
