package com.cryo.games;

import com.cryo.entities.Game;
import com.cryo.utils.Utilities;
import lombok.Data;
import lombok.Getter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

@Data
public class GameManager {

    @Getter
    private HashMap<String, Game> games;

    private Game currentGame;

    public void load() {
        try {
            games = new HashMap<>();
            for (Class<?> c : Utilities.getClasses("com.cryo.games.impl")) {
                if (!Game.class.isAssignableFrom(c)) continue;
                Object o = c.newInstance();
                if (!(o instanceof Game)) continue;
                Game game = (Game) o;
                games.put(game.getName(), game);
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }
    }

    public void startNewGame() {
        if (Utilities.random(5) != 1) return;
        List<Game> games = new ArrayList<>(this.games.values());
        Collections.shuffle(games);
        Game game = games.get(0);
        if (game.startGame())
            currentGame = game;
    }

    public void startNewGame(Game game) {
        if (currentGame != null)
            currentGame.win(0L);
        if (game.startGame())
            currentGame = game;
    }

    public void endGame() {
        currentGame = null;
    }
}
