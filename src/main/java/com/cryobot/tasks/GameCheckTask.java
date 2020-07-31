package com.cryobot.tasks;

import com.cryobot.DiscordBot;

import java.util.TimerTask;

public class GameCheckTask extends TimerTask {

    @Override
    public void run() {
        DiscordBot.getInstance().getGameManager().checkLength();
        DiscordBot.getInstance().getGameManager().startNewGame();
    }
}
