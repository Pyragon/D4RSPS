package com.cryobot.db.impl;

import com.cryobot.DiscordBot;
import com.cryobot.db.DBConnectionManager;
import com.cryobot.db.DatabaseConnection;
import com.cryobot.entities.SQLQuery;
import com.cryobot.utils.Utilities;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.stream.IntStream;

public class AccountConnection extends DatabaseConnection {

    public AccountConnection() {
        super("discord");
    }

    public static AccountConnection connection() {
        return (AccountConnection) DiscordBot.getInstance().getConnectionManager().getConnection(DBConnectionManager.Connection.LINKED_ACCOUNTS);
    }

    public static int getRights(long discordId) {
        Object[] data = connection().handleRequest("get-username", discordId);
        if (data == null) return 0;
        String username = (String) data[0];
        return DiscordBot.getInstance().getHelper().getRights(username);
    }

    @Override
    public Object[] handleRequest(Object... data) {
        String opcode = (String) data[0];
        switch (opcode) {
            case "verify":
                String username = (String) data[1];
                String random = (String) data[2];
                data = select("verify", "random=?", GET_VERIFICATION, random);
                if (data == null) return null;
                long discordId = (long) data[0];
                Timestamp expiry = (Timestamp) data[1];
                delete("verify", "random=?", random);
                if (expiry.getTime() <= System.currentTimeMillis())
                    return null;
                insert("linked", new Object[]{"DEFAULT", username, discordId, "DEFAULT"});
                return new Object[]{discordId};
            case "verify-server":
                discordId = (long) data[1];
                random = (String) data[2];
                data = select("verify_server", "random=?", GET_SERVER_VERIFICATION, random);
                if(data == null) return null;
                username = (String) data[0];
                expiry = (Timestamp) data[1];
                delete("verify_server", "random=?", random);
                if(expiry.getTime() <= System.currentTimeMillis())
                    return null;
                insert("linked", new Object[] { "DEFAULT", username, discordId, "DEFAULT" });
                return new Object[] { username };
            case "unlink":
                delete("linked", "discord_id=?", data[1]);
                break;
            case "get-all-discord-ids":
                return select("linked", null, GET_DISCORD_IDS);
            case "get-discord-id":
                data = select("linked", "username=?", GET_LINKED_DATA, data[1]);
                if (data == null) return null;
                return new Object[]{data[2]};
            case "get-username":
                data = select("linked", "discord_id=?", GET_LINKED_DATA, data[1]);
                if (data == null) return null;
                return new Object[]{data[1]};
            case "add-server-verification":
                username = (String) data[1];
                random = randomString(6);
                delete("verify_server", "username=?", username);
                long millis = System.currentTimeMillis() + (1000 * 60 * 60 * 24);
                insert("verify_server", new Object[]{"DEFAULT", username, random, new Timestamp(millis)});
                return new Object[]{random};
            case "add-verification":
                discordId = (long) data[1];
                random = randomString(6);
                delete("verify", "discord_id=?", discordId);
                millis = System.currentTimeMillis() + (1000 * 60 * 60 * 24);
                insert("verify", new Object[]{"DEFAULT", discordId, random, new Timestamp(millis)});
                return new Object[]{random};
        }
        return null;
    }

    public static String randomString(int length) {
        String chars = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
        StringBuilder builder = new StringBuilder();
        IntStream.range(0, length).forEach(e -> builder.append(chars.charAt(Utilities.random(chars.length()))));
        return builder.toString();
    }

    private final SQLQuery GET_VERIFICATION = set -> {
        if (empty(set)) return null;
        long discordId = getLongInt(set, "discord_id");
        Timestamp expiry = getTimestamp(set, "expiry");
        return new Object[]{discordId, expiry};
    };

    private final SQLQuery GET_SERVER_VERIFICATION = set -> {
        if(empty(set)) return null;
        String username = getString(set, "username");
        Timestamp expiry = getTimestamp(set, "expiry");
        return new Object[] { username, expiry };
    };

    private final SQLQuery GET_DISCORD_IDS = set -> {
        ArrayList<Long> ids = new ArrayList<>();
        if (wasNull(set)) return new Object[]{ids};
        while (next(set)) ids.add(getLongInt(set, "discord_id"));
        return new Object[]{ids};
    };

    private final SQLQuery GET_LINKED_DATA = set -> {
        if (empty(set)) return null;
        int id = getInt(set, "id");
        String username = getString(set, "username");
        long discordId = getLongInt(set, "discord_id");
        Timestamp added = getTimestamp(set, "added");
        return new Object[]{id, username, discordId, added};
    };
}
