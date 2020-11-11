package com.cryobot.entities;

import net.dv8tion.jda.api.entities.Message;

@FunctionalInterface
public interface DiscordMessageEvent {

    void run(Message message, String username);
}
