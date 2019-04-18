package com.cryo.tasks;

import com.cryo.DiscordBot;

import java.util.TimerTask;

public class GameCheckTask extends TimerTask {

    @Override
    public void run() {
        DiscordBot.getInstance().getGameManager().startNewGame();
    }
}
