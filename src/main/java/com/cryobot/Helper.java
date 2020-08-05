package com.cryobot;

import com.cryobot.entities.Command;
import com.cryobot.entities.Dialogue;
import com.cryobot.entities.Game;

import java.util.ArrayList;

public interface Helper {

    int getRights(String username);

    String getDisplayName(String username);

    void sendFriendsChatMessage(String owner, String displayName, String message);

    int getPlayersOnline();

    int getLevel(String username, int skill);

    double getXp(String username, int skill);

    String getEquip(String username, int index);

    String[] getStatuses(String username);

    boolean isDynamicRegion(int x, int y, int z);

    default ArrayList<Game> getExtraGames() { return new ArrayList<>(); }

    default ArrayList<Dialogue> getExtraDialogues() { return new ArrayList<>(); }

    default ArrayList<Command> getExtraCommands() { return new ArrayList<>(); }

    default String getStatus() {
        return ".help | " + getPlayersOnline() + " online";
    }
}
