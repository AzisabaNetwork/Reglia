package dev.felnull.reglia;

import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;

public final class VelocityIdListener implements PluginMessageListener, Listener {
    private final JavaPlugin plugin;
    private volatile String serverId = "unknown";
    boolean isTrue = false;

    public VelocityIdListener(JavaPlugin plugin) { this.plugin = plugin; }

    public String getServerId() { return serverId; }

    // プレイヤーが入ったら一度問い合わせる
    @org.bukkit.event.EventHandler
    public void onJoin(org.bukkit.event.player.PlayerJoinEvent e) {
        if(!isTrue){
            requestServerId(e.getPlayer());
            isTrue = true;
        }
    }

    public static void requestServerId(org.bukkit.entity.Player player) {
        try {
            com.google.common.io.ByteArrayDataOutput out = com.google.common.io.ByteStreams.newDataOutput();
            out.writeUTF("GetServer");
            player.sendPluginMessage(RegliaSystem.getINSTANCE(), "BungeeCord", out.toByteArray());
        } catch (Exception ignored) {}
    }

    @Override
    public void onPluginMessageReceived(String channel, org.bukkit.entity.Player player, byte[] message) {
        if (!"BungeeCord".equals(channel)) return;
        com.google.common.io.ByteArrayDataInput in = com.google.common.io.ByteStreams.newDataInput(message);
        String sub = in.readUTF();
        if ("GetServer".equals(sub)) {
            String id = in.readUTF(); // ← Velocity/Bungee が返すサーバID
            this.serverId = id;
            RegliaSystem.serverIdCache = id;
            plugin.getLogger().info("[Reglia] Detected proxy server id: " + id);
        }
    }

    public void kickOffServerIdQuery() {
        org.bukkit.entity.Player p = org.bukkit.Bukkit.getOnlinePlayers().stream().findAny().orElse(null);
        if (p != null) {
            VelocityIdListener.requestServerId(p);
        }
    }
}