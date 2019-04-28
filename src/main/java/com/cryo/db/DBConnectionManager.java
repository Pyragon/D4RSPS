package com.cryo.db;

import com.cryo.db.impl.*;

import java.util.HashMap;

/**
 * @author Cody Thompson <eldo.imo.rs@hotmail.com>
 * <p>
 * Created on: Mar 7, 2017 at 7:35:35 PM
 */
public class DBConnectionManager {

    private HashMap<Connection, DatabaseConnection> connections;

    public DBConnectionManager() {
        //loadDriver();
        init();
    }

    public DatabaseConnection getConnection(Connection connection) {
        return connections.get(connection);
    }

    public void loadDriver() {
        try {
            Class.forName("com.mysql.jdbc.Driver").newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void init() {
        connections = new HashMap<>();
        connections.put(Connection.LINKED_ACCOUNTS, new AccountConnection());
        connections.put(Connection.FRIENDS_CHAT, new FriendsChatConnection());
        connections.put(Connection.MISC, new MiscConnection());
        connections.put(Connection.GAMES, new GamesConnection());
        connections.put(Connection.ROLES, new RolesConnection());
    }

    public enum Connection {
        LINKED_ACCOUNTS, FRIENDS_CHAT, MISC, GAMES, ROLES
    }

}
