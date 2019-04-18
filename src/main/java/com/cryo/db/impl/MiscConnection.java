package com.cryo.db.impl;

import com.cryo.DiscordBot;
import com.cryo.db.DBConnectionManager;
import com.cryo.db.DatabaseConnection;
import com.cryo.entities.SQLQuery;

public class MiscConnection extends DatabaseConnection {

    public MiscConnection() {
        super("discord");
    }

    public static MiscConnection connection() {
        return (MiscConnection) DiscordBot.getInstance().getConnectionManager().getConnection(DBConnectionManager.Connection.MISC);
    }

    @Override
    public Object[] handleRequest(Object... data) {
        String opcode = (String) data[0];
        switch (opcode) {
            case "set-value":
                String key = (String) data[1];
                Object value = data[2];
                data = select("misc", "`key`=?", GET_VALUE, key);
                if (data == null) insert("misc", new Object[]{"DEFAULT", key, value.toString(), "DEFAULT"});
                else set("misc", "value=?", "`key`=?", value.toString(), key);
                break;
            case "get-value":
                return select("misc", "`key`=?", GET_VALUE, data[1]);
        }
        return null;
    }

    public static void setLong(String key, long value) {
        connection().handleRequest("set-value", key, Long.toString(value));
    }

    public static long getLong(String key) {
        Object[] data = connection().handleRequest("get-value", key);
        if (data == null) return 0L;
        return Long.parseLong((String) data[0]);
    }

    private final SQLQuery GET_VALUE = set -> {
        if (empty(set)) return null;
        return new Object[]{getString(set, "value")};
    };

    private final SQLQuery GET_INT = set -> {
        if (empty(set)) return null;
        return new Object[]{Integer.parseInt(getString(set, "value"))};
    };


}
