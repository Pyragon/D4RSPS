package com.cryo.entities;

import com.cryo.DiscordBot;
import lombok.Data;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.entities.PrivateChannel;

@Data
public abstract class Dialogue {

    protected final long id;

    protected int stage;

    public abstract String getName();

    public abstract void start(String[] parameters);

    public abstract void run(PrivateChannel channel, Message message, String response, String[] res);

    protected void run(PrivateChannel channel, Message message) {
        run(channel, message, null, null);
    }

    protected long sendMessage(String message) {
        PrivateChannel channel = DiscordBot.getInstance()
                .getJda()
                .getUserById(id)
                .openPrivateChannel()
                .complete();
        if (channel == null) return 0L;
        Message m = channel.sendMessage(message).complete();
        return m == null ? 0L : m.getIdLong();
    }

    protected long sendMessage(MessageEmbed embed) {
        PrivateChannel channel = DiscordBot.getInstance()
                .getJda()
                .getUserById(id)
                .openPrivateChannel()
                .complete();
        if (channel == null) return 0L;
        Message m = channel.sendMessage(embed).complete();
        return m == null ? 0L : m.getIdLong();
    }

    public void end() {
        DiscordBot.getInstance().getDialogueManager().endConversation(id);
    }

}
