package com.cryo.commands.impl;

import com.cryo.Links;
import com.cryo.entities.Command;
import net.dv8tion.jda.core.entities.Message;

public class FinishLinkCommand implements Command {

    @Override
    public int getPermissionsReq(String command) {
        return 2;
    }

    @Override
    public String[] getAliases() {
        return new String[]{"finish-link"};
    }

    @Override
    public void handleCommand(Message message, String command, String[] cmd) {
        String random = cmd[1];
        String name = command.replace("finish-link " + random + " ", "");
        boolean linked = Links.linkDiscordAccount(name, random);
        message.getChannel().sendMessage("Account is now " + (!linked ? "not " : "") + "linked").queue();
    }
}
