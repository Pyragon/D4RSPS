package com.cryo.db.impl;

import com.cryo.DiscordBot;
import com.cryo.db.DBConnectionManager;
import com.cryo.db.DatabaseConnection;
import com.cryo.entities.Item;
import com.cryo.entities.SQLQuery;
import com.cryo.entities.Trivia;

import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;

public class GamesConnection extends DatabaseConnection {

    public GamesConnection() {
        super("discord");
    }

    public static GamesConnection connection() {
        return (GamesConnection) DiscordBot.getInstance().getConnectionManager().getConnection(DBConnectionManager.Connection.GAMES);
    }

    @Override
    public Object[] handleRequest(Object... data) {
        String opcode = (String) data[0];
        switch (opcode) {
            case "add-total-points":
                set("points", "total_points=total_points+?", "discord_id=?", data[1], data[2]);
            case "get-points-leader":
                return select("points", null, "ORDER BY points DESC LIMIT 1", GET_POINTS);
            case "get-points":
                return select("points", "discord_id=?", GET_POINTS, data[1]);
            case "add-points":
                set("points", "points=points+?", "discord_id=?", data[1], data[2]);
                break;
            case "remove-points":
                set("points", "points=points-?", "discord_id=?", data[1], data[2]);
                break;
            case "set-total-points":
                long id = (long) data[1];
                int points = (int) data[2];
                data = handleRequest("get-points", id);
                if (data == null)
                    insert("points", "DEFAULT", id, points, points);
                else
                    set("points", "total_points=?", "discord_id=?", points, id);
                break;
            case "set-points":
                id = (long) data[1];
                points = (int) data[2];
                data = handleRequest("get-points", id);
                if (data == null)
                    insert("points", "DEFAULT", id, points);
                else
                    set("points", "points=?", "discord_id=?", points, id);
                break;
            case "get-trivia":
                return select("trivia", GET_TRIVIA_QUESTIONS);
            case "add-trivia":
                insert("trivia", ((Trivia) data[1]).data());
                break;
            case "get-guess-items":
                return select("guess_items", GET_GUESS_ITEMS);
            case "add-guess-item":
                insert("guess_items", ((Item) data[1]).data());
                break;
        }
        return null;
    }

    public static int getTotalPoints(long id) {
        Object[] data = connection().handleRequest("get-points", id);
        if (data == null) return 0;
        return (int) data[2];
    }

    public static int getPoints(long id) {
        Object[] data = connection().handleRequest("get-points", id);
        if (data == null) return 0;
        return (int) data[0];
    }

    public static void setPoints(long id, int points, boolean total) {
        connection().handleRequest("set-" + (total ? "total-" : "") + "points", id, points);
        DiscordBot.getInstance().getRoleManager().recheckRoles(id);
    }

    public static void addPoints(long id, int points) {
        Object[] data = connection().handleRequest("get-points", id);
        if (data == null)
            connection().handleRequest("set-points", id, points);
        else
            connection().handleRequest("add-points", points, id);
        connection().handleRequest("add-total-points", points, id);
        DiscordBot.getInstance().getRoleManager().recheckRoles(id);
    }

    public static void removePoints(long id, int points) {
        Object[] data = connection().handleRequest("get-points", id);
        if (data == null)
            connection().handleRequest("set-points", id, -points);
        else
            connection().handleRequest("remove-points", points, id);
        DiscordBot.getInstance().getRoleManager().recheckRoles(id);
    }

    private final SQLQuery GET_POINTS = set -> {
        if (empty(set)) return null;
        long discordId = getLongInt(set, "discord_id");
        int points = getInt(set, "points");
        int totalPoints = getInt(set, "total_points");
        return new Object[]{points, discordId, totalPoints};
    };

    private final SQLQuery GET_GUESS_ITEMS = set -> {
        ArrayList<Item> items = new ArrayList<>();
        if (wasNull(set)) return new Object[]{items};
        while (next(set)) items.add(loadGuessItem(set));
        return new Object[]{items};
    };

    private final SQLQuery GET_GUESS_ITEM = set -> {
        if (empty(set)) return null;
        return new Object[]{loadGuessItem(set)};
    };

    private final SQLQuery GET_TRIVIA_QUESTIONS = set -> {
        ArrayList<Trivia> questions = new ArrayList<>();
        if (wasNull(set)) return new Object[]{questions};
        while (next(set)) questions.add(loadTrivia(set));
        return new Object[]{questions};
    };

    private final SQLQuery GET_TRIVIA_QUESTION = set -> {
        if (empty(set)) return null;
        return new Object[]{loadTrivia(set)};
    };

    private Item loadGuessItem(ResultSet set) {
        int id = getInt(set, "id");
        String name = getString(set, "item_name");
        String itemPicUrl = getString(set, "item_pic_url");
        String hint = getString(set, "hint");
        Timestamp added = getTimestamp(set, "added");
        return new Item(id, name, itemPicUrl, hint, added);
    }

    private Trivia loadTrivia(ResultSet set) {
        int id = getInt(set, "id");
        String question = getString(set, "question");
        String[] answers = DiscordBot.getInstance().getGson().fromJson(getString(set, "answers"), String[].class);
        int correct = getInt(set, "correct");
        Timestamp added = getTimestamp(set, "added");
        Timestamp updated = getTimestamp(set, "updated");
        return new Trivia(id, question, answers, correct, added, updated);
    }
}
