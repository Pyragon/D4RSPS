package com.cryo.db.impl;

import com.cryo.DiscordBot;
import com.cryo.db.DBConnectionManager;
import com.cryo.db.DatabaseConnection;
import com.cryo.entities.SQLQuery;

import java.util.HashMap;

public class FriendsChatConnection extends DatabaseConnection {

    public FriendsChatConnection() {
        super("discord");
    }

    public static FriendsChatConnection connection() {
        return (FriendsChatConnection) DiscordBot.getInstance().getConnectionManager().getConnection(DBConnectionManager.Connection.FRIENDS_CHAT);
    }

    @Override
    public Object[] handleRequest(Object... data) {
        String opcode = (String) data[0];
        switch (opcode) {
            case "link-friends-chat":
                String owner = (String) data[1];
                long discordId = (long) data[2];
                data = select("friends_chats", "owner=? OR discord_id=?", GET_FRIENDS_CHAT, owner, discordId);
                if (data != null) return null;
                insert("friends_chats", new Object[]{"DEFAULT", owner, discordId, "DEFAULT"});
                return new Object[]{};
            case "unlink":
                long channelId = (long) data[1];
                data = select("friends_chats", "discord_id=?", GET_FRIENDS_CHAT, channelId);
                if (data == null) return null;
                owner = (String) data[0];
                delete("friends_chats", "discord_id=?", data[1]);
                return new Object[]{owner};
            case "get-discord-channel":
                data = select("friends_chats", "owner=?", GET_FRIENDS_CHAT, data[1]);
                if (data == null) return null;
                return new Object[]{data[1]};
            case "get-friends-chat":
                data = select("friends_chats", "discord_id=?", GET_FRIENDS_CHAT, data[1]);
                if (data == null) return null;
                return new Object[]{data[0]};
            case "get-linked-chats":
                return select("friends_chats", GET_FRIENDS_CHATS);
        }
        return null;
    }

    private final SQLQuery GET_FRIENDS_CHAT = set -> {
        if (empty(set)) return null;
        String owner = getString(set, "owner");
        long discordId = getLongInt(set, "discord_id");
        return new Object[]{owner, discordId};
    };

    private final SQLQuery GET_FRIENDS_CHATS = set -> {
        HashMap<String, Long> chats = new HashMap<>();
        if (wasNull(set)) return new Object[]{chats};
        while (next(set)) {
            String owner = getString(set, "owner");
            long discordId = getLongInt(set, "discord_id");
            chats.put(owner, discordId);
        }
        return new Object[]{chats};
    };
}
