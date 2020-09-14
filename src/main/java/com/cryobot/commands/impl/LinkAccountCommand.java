package com.cryobot.commands.impl;

import com.cryobot.Links;
import com.cryobot.db.impl.AccountConnection;
import com.cryobot.entities.Command;
import net.dv8tion.jda.core.entities.Message;

public class LinkAccountCommand implements Command {

    @Override
    public int getPermissionsReq(String command) {
        return 0;
    }

    @Override
    public String[] getAliases() {
        return new String[]{"link", "unlink", "verify"};
    }

    @Override
    public void handleCommand(Message message, String command, String[] cmd) {
        long discordId = message.getAuthor().getIdLong();
        if (cmd[0].equals("unlink")) {
            Object[] data = AccountConnection.connection().handleRequest("get-username", discordId);
            if (data == null) {
                message.delete().queue();
                message.getAuthor().openPrivateChannel().queue(privateChannel -> privateChannel.sendMessage("Your discord account is not linked with any in-game account. Start this process using the .link command").queue());
                return;
            }
            AccountConnection.connection().handleRequest("unlink", discordId);
            message.delete().queue();
            message.getAuthor().openPrivateChannel().queue(privateChannel -> privateChannel.sendMessage("Success. Your discord account is no longer linked to your in-game account").queue());
            return;
        } else if(cmd[0].equals("verify")) {
            String pMessage;
            if(cmd.length != 2)
                pMessage = "Incorrect usage. Please try again. .verify (random)";
            else {
                String random = cmd[1];
                boolean linked = Links.linkDiscordAccountFromServer(discordId, random);
                if (linked)
                    pMessage = "Success! Your in-game account is now linked with your discord account.";
                else
                    pMessage = "Unsuccessful! Are you sure your accounts aren't already linked?";
            }
            message.delete().queue();
            message.getAuthor().openPrivateChannel().queue(channel -> channel.sendMessage(pMessage).queue());
            Links.recheckAllRoles();
            return;
        }
        Object[] data = AccountConnection.connection().handleRequest("get-username", discordId);
        if (data != null) {
            message.delete().queue();
            message.getAuthor().openPrivateChannel().queue(privateChannel -> privateChannel.sendMessage("Your discord account is already linked with an in-game account. There is no way to undo this at the moment.").queue());
            return;
        }
        data = AccountConnection.connection().handleRequest("add-verification", discordId);
        if (data == null) {
            message.delete().queue();
            message.getAuthor().openPrivateChannel().queue(privateChannel -> privateChannel.sendMessage("There was an error linking your accounts. Please try again later.").queue());
            return;
        }
        String random = (String) data[0];
        message.delete().queue();
        message.getAuthor().openPrivateChannel().queue(privateChannel -> {
            privateChannel.sendMessage("Congratulations! Your verification process has been started!").queue();
            privateChannel.sendMessage("Use command ::verify " + random + " in-game to finish the process!").queue();
        });
        return;
    }
}
