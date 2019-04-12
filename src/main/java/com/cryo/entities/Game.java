package com.cryo.entities;

public abstract class Game {

    public abstract void startGame();

    public void processGuessCommand(String command, String[] cmd) {

    }

    public void win(long id) {

    }

    public abstract int getPoints();

}
