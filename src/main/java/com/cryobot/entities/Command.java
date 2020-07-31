package com.cryobot.entities;

import net.dv8tion.jda.core.entities.Message;

public interface Command {

    int getPermissionsReq(String command);

    String[] getAliases();

    void handleCommand(Message message, String command, String[] cmd);
}
