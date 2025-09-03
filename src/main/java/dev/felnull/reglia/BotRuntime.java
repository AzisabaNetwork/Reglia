package dev.felnull.reglia;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.bukkit.plugin.Plugin;

public class BotRuntime {
    private final Plugin plugin;
    private JDA jda;
    private final java.util.concurrent.atomic.AtomicBoolean stopping = new java.util.concurrent.atomic.AtomicBoolean(false);

    public BotRuntime(Plugin plugin) { this.plugin = plugin; }

    public boolean start() {
        String token = plugin.getConfig().getString("discord.token", "");
        if (token.isEmpty()) {
            plugin.getLogger().severe("[Reglia] discord.token is empty");
            return false;
        }
        try {
            JDABuilder b = JDABuilder.createDefault(token)
                    .setActivity(Activity.playing("LGW NotificationSystem"))
                    .enableIntents(GatewayIntent.GUILD_MESSAGES);

            boolean msgContent = plugin.getConfig().getBoolean("discord.intents.messageContent", false);
            if (msgContent) {
                b.enableIntents(GatewayIntent.MESSAGE_CONTENT); // ポータルで許可必須
            }

            jda = b.build();
            jda.awaitReady();
            plugin.getLogger().info("[Reglia] JDA ready.");
            return true;
        } catch (Exception e) {
            plugin.getLogger().severe("[Reglia] JDA start error: " + e.getMessage());
            return false;
        }
    }

    public void stop() {
        if (!stopping.compareAndSet(false, true)) return; // 二重停止防止

        JDA tmp = this.jda;
        this.jda = null;

        if (tmp != null) {
            try {
                // 待たずに即時停止
                tmp.shutdownNow(); // queue をキャンセルしてスレッドを止める
            } catch (Throwable ignored) {
                // ここで例外を握りつぶす（アンロード中のクラスローダ競合を避ける）
            }
        }
    }
    public JDA jda() { return jda; }
}
