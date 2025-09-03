package dev.felnull.reglia;

import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import java.util.concurrent.CompletableFuture;

public class DiscordTransport {
    private final BotRuntime bot;
    public DiscordTransport(BotRuntime bot) { this.bot = bot; }

    public CompletableFuture<Boolean> sendToChannelId(String channelId, String content) {
        CompletableFuture<Boolean> cf = new CompletableFuture<>();
        if (bot.jda() == null) {
            cf.complete(false);
            return cf;
        }
        TextChannel ch = bot.jda().getTextChannelById(channelId);
        if (ch == null) {
            cf.complete(false);
            return cf;
        }
        ch.sendMessage(content).queue(
                s -> cf.complete(true),
                cf::completeExceptionally
        );
        return cf;
    }
}
