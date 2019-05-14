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

    private long startTime = 0L;

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

    public <K> K getGame(String key) {
        if (!games.containsKey(key)) return null;
        return (K) games.get(key);
    }

    public void checkLength() {
        if (startTime == 0L || currentGame == null) return;
        long elapsed = System.currentTimeMillis() - startTime;
        if (elapsed >= (1000 * 60 * 5) && currentGame != null) {
            currentGame.end();
            currentGame = null;
        }
    }

    public void startNewGame() {
        if (currentGame != null || Utilities.random(300) != 1) return;
        List<Game> games = new ArrayList<>(this.games.values());
        Collections.shuffle(games);
        Game game = games.get(0);
        if (game.startGame()) {
            currentGame = game;
            startTime = System.currentTimeMillis();
        }
    }

    public void startNewGame(String name) {
        if (!games.containsKey(name)) return;
        startNewGame(games.get(name));
    }

    public void startNewGame(Game game) {
        if (currentGame != null)
            currentGame.end();
        if (game.startGame()) {
            currentGame = game;
            startTime = System.currentTimeMillis();
        }
    }

    public void endGame() {
        currentGame = null;
    }
}
