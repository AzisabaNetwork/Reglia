package dev.felnull.reglia.commands;

import dev.felnull.reglia.Notification;
import dev.felnull.reglia.NotifyService;
import dev.felnull.reglia.RegliaSystem;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class CommandSource implements CommandExecutor {
    private final NotifyService notifyService;

    public CommandSource(NotifyService notifyService) {
        this.notifyService = notifyService;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (args.length == 0) {
            sender.sendMessage("§c引数が不足しています。 /dnotice <subcommand> ...");
            return true;
        }

        switch (args[0].toLowerCase()) {

            // /dnotice send <format> <message...>
            case "send": {
                if (args.length < 3) {
                    sender.sendMessage("§c引数が不足しています。/dnotice send <format> <message...>");
                    return true;
                }

                String format = args[1];
                String message = String.join(" ", Arrays.copyOfRange(args, 2, args.length));

                Map<String, String> data = new HashMap<>();
                data.put("message", message);

                Notification n = new Notification(format, data, RegliaSystem.serverIdCache, sender.getName(), null);

                sender.sendMessage("§a通知をキューに追加しました。送信処理は非同期で行われます。");
                notifyService.notifyAsync(n).whenComplete((ok, ex) -> {
                    if (ex != null) {
                        sender.sendMessage("§c送信に失敗しました: " + ex.getMessage());
                    } else if (!ok) {
                        sender.sendMessage("§c通知はキューに入りませんでした（format不明/過負荷/未接続）。");
                    }
                });
                return true;
            }

            // /dnotice reload
            case "reload": {
                sender.sendMessage("§eフォーマットを再読み込み中...");
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        notifyService.getRegistry().reload();
                        Bukkit.getScheduler().runTask(RegliaSystem.getINSTANCE(), () ->
                                sender.sendMessage("§aフォーマットを再読み込みしました。"));
                    }
                }.runTaskAsynchronously(RegliaSystem.getINSTANCE());
                return true;
            }

            // /dnotice help
            case "help":
            default: {
                sender.sendMessage("§6=== Reglia コマンド一覧 ===");
                sender.sendMessage("§e/dnotice send <format> <message...> §7- 指定フォーマットで通知を送信");
                sender.sendMessage("§e/dnotice reload §7- formats/*.yml を再読み込み");
                sender.sendMessage("§e/dnotice help §7- このヘルプを表示");
                return true;
            }
        }
    }
}
