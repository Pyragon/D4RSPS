package com.cryo.commands.impl;

import com.cryo.DiscordBot;
import com.cryo.db.impl.MiscConnection;
import com.cryo.entities.Command;
import net.dv8tion.jda.core.entities.Message;

public class DialogueCommands implements Command {

    @Override
    public int getPermissionsReq(String command) {
        return 2;
    }

    @Override
    public String[] getAliases() {
        return DiscordBot.getInstance().getDialogueManager().getDialogueNameList();
    }

    @Override
    public void handleCommand(Message message, String command, String[] cmd) {
        if (command.equals("setup")) {
            long ownerId = DiscordBot.getInstance().getJda().getGuildById(MiscConnection.getLong("guild-id")).getOwner().getUser().getIdLong();
            if (ownerId != message.getAuthor().getIdLong()) {
                message.delete().queue();
                message.getAuthor().openPrivateChannel().queue(channel -> channel.sendMessage("Only the owner of the guild can use this command.").queue());
                return;
            }
        }
        message.delete().queue();
        DiscordBot.getInstance()
                .getDialogueManager()
                .startConversation(message.getAuthor().getIdLong(), command);
    }
}
