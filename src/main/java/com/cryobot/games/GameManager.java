package com.cryobot.games;

import com.cryobot.DiscordBot;
import com.cryobot.entities.Game;
import com.cryobot.games.impl.GuessThatItemGame;
import com.cryobot.games.impl.GuessThatPlaceGame;
import com.cryobot.games.impl.TriviaGame;
import com.cryobot.utils.Utilities;
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

    private static Class[] GAMES = {GuessThatItemGame.class, GuessThatPlaceGame.class, TriviaGame.class };

    public void load() {
        try {
            games = new HashMap<>();
            for (Class<?> c : GAMES) {
                if (!Game.class.isAssignableFrom(c)) continue;
                Object o = c.newInstance();
                if (!(o instanceof Game)) continue;
                Game game = (Game) o;
                games.put(game.getName(), game);
            }
            ArrayList<Game> extra = DiscordBot.getInstance().getHelper().getExtraGames();
            if(extra == null || extra.size() == 0) return;
            for(Game game : extra) {
                if(games.containsKey(game.getName())) {
                    System.err.println("[GameManager]Duplicate game exists: "+game.getName());
                    continue;
                }
                games.put(game.getName(), game);
            }
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
