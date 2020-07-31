package com.cryobot.tasks;

import com.cryobot.DiscordBot;
import net.dv8tion.jda.core.entities.Game;

import java.util.TimerTask;

public class StatusTask extends TimerTask {

    @Override
    public void run() {
        String status = DiscordBot.getInstance().getHelper().getStatus();
        DiscordBot.getInstance().getJda().getPresence().setGame(Game.playing(status));
    }
}
