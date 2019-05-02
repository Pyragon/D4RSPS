package com.cryo.commands.impl;

import com.cryo.DiscordBot;
import com.cryo.entities.Command;
import net.dv8tion.jda.core.entities.Message;

public class DialogueCommands implements Command {

    @Override
    public int getPermissionsReq(String command) {
        return 2;
    }

    @Override
    public String[] getAliases() {
        return new String[]{"setup", "setup-guess-item", "setup-trivia"};
    }

    @Override
    public void handleCommand(Message message, String command, String[] cmd) {
        message.delete().queue();
        DiscordBot.getInstance()
                .getDialogueManager()
                .startConversation(message.getAuthor().getIdLong(), command);
    }
}
