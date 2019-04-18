package com.cryo.db.impl;

import com.cryo.DiscordBot;
import com.cryo.db.DBConnectionManager;
import com.cryo.db.DatabaseConnection;
import com.cryo.entities.Item;
import com.cryo.entities.SQLQuery;

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
            case "get-points":
                return select("points", "discord_id=?", GET_POINTS, data[1]);
            case "add-points":
                set("points", "points=points+?", "discord_id=?", data[1], data[2]);
                break;
            case "remove-points":
                set("points", "points=points-?", "discord_id=?", data[1], data[2]);
                break;
            case "set-points":
                long id = (long) data[1];
                int points = (int) data[2];
                data = handleRequest("get-points", id);
                if (data == null)
                    insert("points", "DEFAULT", id, points);
                else
                    set("points", "points=?", "discord_id=?", points, id);
                break;
            case "get-guess-items":
                return select("guess_items", GET_GUESS_ITEMS);
            case "add-guess-item":
                insert("guess_items", ((Item) data[1]).data());
                break;
        }
        return null;
    }

    public static void addPoints(long id, int points) {
        Object[] data = connection().handleRequest("get-points", id);
        if (data == null)
            connection().handleRequest("set-points", id, points);
        else
            connection().handleRequest("add-points", points, id);
    }

    public static void removePoints(long id, int points) {
        Object[] data = connection().handleRequest("get-points", id);
        if (data == null)
            connection().handleRequest("set-points", id, -points);
        else
            connection().handleRequest("remove-points", points, id);
    }

    private final SQLQuery GET_POINTS = set -> {
        if (empty(set)) return null;
        int points = getInt(set, "points");
        return new Object[]{points};
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

    private Item loadGuessItem(ResultSet set) {
        int id = getInt(set, "id");
        String name = getString(set, "item_name");
        String itemPicUrl = getString(set, "item_pic_url");
        String hint = getString(set, "hint");
        Timestamp added = getTimestamp(set, "added");
        return new Item(id, name, itemPicUrl, hint, added);
    }
}
