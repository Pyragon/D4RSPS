package com.cryo;

public interface Helper {

    int getRights(String username);

    String getDisplayName(String username);

    void sendFriendsChatMessage(String owner, String username, String message);

    long[] getRoles(String username);

    long getOwnerId();

    long getGuildId();

    int getPlayersOnline();

    int getLevel(String username, int skill);

    double getXp(String username, int skill);

    String getEquip(String username, int index);

    default String getStatus() {
        return ".help | " + getPlayersOnline() + " online";
    }
}
