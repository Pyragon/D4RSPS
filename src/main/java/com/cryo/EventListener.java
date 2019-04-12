package com.cryo;

import com.cryo.db.impl.MiscConnection;
import net.dv8tion.jda.core.events.guild.GuildJoinEvent;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.SubscribeEvent;

public class EventListener {

    @SubscribeEvent
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getAuthor().getIdLong() == DiscordBot.getInstance().getId())
            return;
        switch (event.getChannel().getType()) {
            case TEXT:
                if (DiscordBot.getInstance().getCommandManager().processCommand(event.getMessage())) return;
                Links.handleDiscordMessage(event.getMessage());
                break;
            case PRIVATE:
                DiscordBot.getInstance().getDialogueManager().continueConversation(event.getAuthor().getIdLong(), event.getMessage().getContentRaw());
                break;
        }
    }

    public void onGuildJoin(GuildJoinEvent event) {
        Object[] data = MiscConnection.connection().handleRequest("get-guild-id");
        if (data != null) {
            long ownerId = DiscordBot.getInstance().getHelper().getOwnerId();
            DiscordBot.getInstance().getJda().getUserById(ownerId).openPrivateChannel().queue(privateChannel -> {
                privateChannel.sendMessage("I sensed that I have been added to a second guild improperly").queue();
                privateChannel.sendMessage("Please remove me from the guild I was just added to, and follow instructions on the rune-server page on how to switch guildes.").queue();
            });
            return;
        }
        long id = event.getGuild().getIdLong();
        MiscConnection.connection().handleRequest("set-guild-id", id);
        System.out.println("Joined server: " + id);
    }
}
