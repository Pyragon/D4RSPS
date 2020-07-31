package com.cryobot.db.impl;

import com.cryobot.DiscordBot;
import com.cryobot.db.DBConnectionManager;
import com.cryobot.db.DatabaseConnection;
import com.cryobot.entities.Role;
import com.cryobot.entities.SQLQuery;

import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.HashMap;

public class RolesConnection extends DatabaseConnection {

    public RolesConnection() {
        super("discord");
    }

    public static RolesConnection connection() {
        return (RolesConnection) DiscordBot.getInstance().getConnectionManager().getConnection(DBConnectionManager.Connection.ROLES);
    }

    @Override
    public Object[] handleRequest(Object... data) {
        String opcode = (String) data[0];
        switch (opcode) {
            case "add-role":
                insert("roles", ((Role) data[1]).data());
                break;
            case "remove-role":
                delete("roles", "id=?", data[1]);
                break;
            case "get-roles":
                return select("roles", GET_ROLES);
            case "get-role-by-role-id":
                return select("roles", "role_id=?", GET_ROLE, data[1]);
            case "get-role-by-id":
                return select("roles", "id=?", GET_ROLE, data[1]);
        }
        return null;
    }

    private final SQLQuery GET_ROLE = set -> {
        if (empty(set)) return null;
        return new Object[]{loadRole(set)};
    };

    private final SQLQuery GET_ROLES = set -> {
        HashMap<Long, Role> roles = new HashMap<>();
        if (wasNull(set)) return new Object[]{roles};
        while (next(set)) {
            Role role = loadRole(set);
            roles.put(role.getRoleId(), role);
        }
        return new Object[]{roles};
    };

    private Role loadRole(ResultSet set) {
        int id = getInt(set, "id");
        long roleId = getLongInt(set, "role_id");
        String name = getString(set, "name");
        boolean hasColour = getBoolean(set, "has_colour");
        String roleColour = getString(set, "role_colour");
        boolean displaySeparately = getBoolean(set, "display_separately");
        boolean mentionableByAnyone = getBoolean(set, "mentionable_by_anyone");
        boolean giveOnPointsRequirement = getBoolean(set, "give_on_points_requirement");
        int pointsRequirement = getInt(set, "points_requirement");
        boolean giveOnCurrentPointsRequirement = getBoolean(set, "give_on_current_points_requirement");
        int currentPointsRequirement = getInt(set, "current_points_requirement");
        boolean giveToPointsLeader = getBoolean(set, "give_to_points_leader");
        boolean giveToIngameStatus = getBoolean(set, "give_to_ingame_status");
        String ingameStatus = getString(set, "ingame_status");
        Timestamp added = getTimestamp(set, "added");
        return new Role(id, roleId, name, hasColour, roleColour, displaySeparately, mentionableByAnyone, giveOnPointsRequirement, pointsRequirement, giveOnCurrentPointsRequirement, currentPointsRequirement, giveToPointsLeader, giveToIngameStatus, ingameStatus, added);
    }
}
