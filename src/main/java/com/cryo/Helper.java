package com.cryo;

public interface Helper {

    int getRights(String username);

    String getDisplayName(String username);

    void sendFriendsChatMessage(String owner, String username, String message);

    int getPlayersOnline();

    int getLevel(String username, int skill);

    double getXp(String username, int skill);

    String getEquip(String username, int index);

    String[] getStatuses(String username);

    boolean isDynamicRegion(int x, int y, int z);

    default String getStatus() {
        return ".help | " + getPlayersOnline() + " online";
    }
}
