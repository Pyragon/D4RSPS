package com.cryo.entities;

import com.cryo.DiscordBot;
import lombok.Data;
import net.dv8tion.jda.core.entities.MessageEmbed;

@Data
public abstract class Dialogue {

    protected final long id;

    protected int stage;

    public abstract String getName();

    public abstract void start(String[] parameters);

    public abstract void run(String response, String[] res);

    protected void run() {
        run(null, null);
    }

    protected void sendMessage(String message) {
        DiscordBot.getInstance()
                .getJda()
                .getUserById(id)
                .openPrivateChannel()
                .queue(privateChannel -> privateChannel.sendMessage(message).queue());
    }

    protected void sendMessage(MessageEmbed embed) {
        DiscordBot.getInstance()
                .getJda()
                .getUserById(id)
                .openPrivateChannel()
                .queue(privateChannel -> privateChannel.sendMessage(embed).queue());
    }

    public void end() {
        DiscordBot.getInstance().getDialogueManager().endConversation(id);
    }

}
