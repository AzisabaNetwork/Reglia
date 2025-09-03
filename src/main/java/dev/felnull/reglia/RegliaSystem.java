package dev.felnull.reglia;

import dev.felnull.reglia.commands.CommandSource;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public final class RegliaSystem extends JavaPlugin {

    private BotRuntime botRuntime;
    private FormatRegistry formatRegistry;
    private FormatEngine formatEngine;
    private NotifyService notifyService;
    private SendQueue sendQueue;
    public static RegliaSystem INSTANCE;
    public static String serverIdCache;

    @Override
    public void onEnable() {
        INSTANCE = this;
        saveDefaultConfig();
        ensureDefaultFormats();
        VelocityIdListener velocityIdListener = new VelocityIdListener(this);

        // 1) フォーマット読み込み
        formatRegistry = new FormatRegistry(this);
        formatRegistry.loadAll();

        // 2) フォーマット変換
        formatEngine = new FormatEngine(this);

        // 3) 送信キュー
        sendQueue = new SendQueue(this);
        sendQueue.start();

        // 4) Discord 起動
        botRuntime = new BotRuntime(this);
        if (!botRuntime.start()) {
            getLogger().severe("[Reglia] Failed to start JDA. Disabling plugin.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // 5) 通知サービス
        notifyService = new NotifyService(this, formatRegistry, formatEngine, sendQueue, botRuntime);

        // 6) コマンド
        getCommand("dnotice").setExecutor(new CommandSource(notifyService));

        getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
        getServer().getMessenger().registerIncomingPluginChannel(this, "BungeeCord", velocityIdListener);

        velocityIdListener.kickOffServerIdQuery();
        serverIdCache = velocityIdListener.getServerId();

        getLogger().info("[Reglia] Enabled.");

    }

    @Override
    public void onDisable() {
        if (sendQueue != null) sendQueue.shutdown(); // 先に送信ワーカー停止（JDAに依存してるなら尚更）
        if (botRuntime != null) botRuntime.stop();   // 次にJDAを停止
        getLogger().info("[Reglia] Disabled.");
    }

    public static RegliaSystem getINSTANCE() {
        return INSTANCE;
    }

    private void ensureDefaultFormats() {
        // 念のためデータフォルダ作成
        if (!getDataFolder().exists()) getDataFolder().mkdirs();

        // 必要なformatsを列挙して、無ければコピー
        String[] defaults = {"formats/info.yml", "formats/warn.yml", "formats/emergency.yml"};
        for (String path : defaults) {
            File out = new File(getDataFolder(), path);
            if (!out.exists()) {
                // Jar内に無いと IllegalArgumentException になるので try-catch 推奨
                try {
                    // replace=false なので既存があれば上書きしない
                    saveResource(path, false);
                } catch (IllegalArgumentException e) {
                    getLogger().warning("[Reglia] Resource not found in jar: " + path);
                }
            }
        }
    }
}
