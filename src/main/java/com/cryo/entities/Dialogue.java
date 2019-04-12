package com.cryo.entities;

import com.cryo.DiscordBot;
import lombok.Data;

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

    public void end() {
        DiscordBot.getInstance().getDialogueManager().endConversation(id);
    }

}
