package com.cryo.commands.impl;

import com.cryo.DiscordBot;
import com.cryo.entities.Command;
import net.dv8tion.jda.core.entities.Message;

public class DialogueCommands implements Command {

    @Override
    public int getPermissionsReq(String command) {
        return command.split(" ")[0].equals("setup") ? 2 : 0;
    }

    @Override
    public String[] getAliases() {
        return new String[]{"setup"};
    }

    @Override
    public void handleCommand(Message message, String command, String[] cmd) {
        switch (cmd[0]) {
            case "setup":
                DiscordBot.getInstance().getDialogueManager().startConversation(message.getAuthor().getIdLong(), "setup");
                break;
        }
    }
}
