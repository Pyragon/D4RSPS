package com.cryo.entities;

import net.dv8tion.jda.core.entities.Message;

public abstract class Game {

    public abstract String getName();

    public abstract boolean startGame();

    public void processGuessCommand(Message message, String command, String[] cmd) {

    }

    public void win(long id) {

    }

    public abstract int getPoints();

}
