package com.cryo.entities;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.sql.Timestamp;

@Data
@AllArgsConstructor
public class Role {

    private final int id;
    private long roleId;
    private final String name;
    private final boolean usingColour;
    private final String roleColour;
    private final boolean displaySeparately;
    private final boolean mentionableByAnyone;

    private final boolean giveOnPointsRequirement;
    private final int pointsRequirement;

    private final boolean giveOnCurrentPointsRequirement;
    private final int currentPointsRequirement;

    private final boolean giveToPointsLeader;

    private final boolean giveToInGameStatus;
    private final String inGameStatus;

    private final Timestamp added;

    public String getDescription() {
        if (giveOnPointsRequirement) return "Given when a user has earned " + pointsRequirement + " or more points.";
        if (giveOnCurrentPointsRequirement)
            return "Given when a user has a current number of points that is " + currentPointsRequirement + " or higher.";
        if (giveToPointsLeader) return "Given to the user(s) with the most points.";
        if (giveToInGameStatus) return "Given to users with the in-game rank of " + inGameStatus + ".";
        return null;
    }

    public Object[] data() {
        return new Object[]{"DEFAULT", roleId, name, usingColour, roleColour, displaySeparately, mentionableByAnyone, giveOnPointsRequirement, pointsRequirement, giveOnCurrentPointsRequirement, currentPointsRequirement, giveToPointsLeader, giveToInGameStatus, inGameStatus, added};
    }

}
