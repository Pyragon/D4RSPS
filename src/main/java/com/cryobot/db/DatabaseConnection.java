package com.cryobot.db;

import com.cryobot.DiscordBot;
import com.cryobot.entities.SQLQuery;

import java.sql.*;
import java.util.ArrayList;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class DatabaseConnection {

    private Connection connection;

    private final String database;

    public DatabaseConnection(String database) {
        this.database = database;
        connect();
        ping();
    }

    public void connect() {
        try {
            Properties prop = DiscordBot.getInstance().getProperties();
            String user = prop.getProperty("db-user");
            String pass = prop.getProperty("db-pass");
            String ip = prop.getProperty("db-host");
            connection = DriverManager.getConnection("jdbc:mysql://" + ip + "/" + database, user, pass);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public abstract Object[] handleRequest(Object... data);

    public void ping() {
        try {
            if (connection == null) {
                System.out.println("The SQL server needs to be started.");
                System.exit(-1);
                return;
            }
            if (connection.isClosed()) return;
            long start = System.currentTimeMillis();
            connection.createStatement().execute("/* ping */ SELECT 1");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void set(String database, String update, String clause, SQLQuery query_inter, Object... params) {
        if (params == null || params.length == 0) {
            String query = "UPDATE " + database + " SET " + update + " WHERE " + clause + ";";
            execute(query);
            return;
        }
        try {
            StringBuilder builder = new StringBuilder();
            builder.append("UPDATE ").append("`" + database + "`").append(" SET ").append(update).append(" WHERE ").append(clause + ";");
            PreparedStatement stmt = connection.prepareStatement(builder.toString());
            try {
                setParams(stmt, params);
                stmt.execute();
            } finally {
                stmt.close();
            }
        } catch (Exception e) {

        }
    }

    public void updateExisting(String database, int id, String[] toUpdate, Object[] values) {
        String query = Stream.of(toUpdate).map(s -> "`" + s + "`=?").collect(Collectors.joining(","));
        Object[] real = new Object[values.length + 1];
        System.arraycopy(values, 0, real, 0, values.length);
        real[real.length - 1] = id;
        set(database, query, "id=?", true, real);
    }

    public void set(String database, String update, String clause, Object... params) {
        set(database, update, clause, false, params);
    }

    public void set(String database, String update, String clause, boolean keepNulls, Object... params) {
        if (params == null || params.length == 0) {
            String query = "UPDATE " + database + " SET " + update + " WHERE " + clause + ";";
            execute(query);
            return;
        }
        try {
            StringBuilder builder = new StringBuilder();
            builder.append("UPDATE ").append("`" + database + "`").append(" SET ").append(update).append(" WHERE ").append(clause + ";");
            PreparedStatement stmt = connection.prepareStatement(builder.toString());
            setParams(stmt, params, keepNulls);
            stmt.execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setParams(PreparedStatement stmt, Object[] params, boolean keepNulls) {
        try {
            if (params == null) return;
            int index = 0;
            for (int i = 0; i < params.length; i++) {
                Object obj = params[i];
                index++;
                if (obj instanceof String) {
                    String string = (String) obj;
                    if (string.equals("DEFAULT")) {
                        index--;
                        continue;
                    }
                    if (string.equals("NULL")) {
                        if (!keepNulls) {
                            index--;
                            continue;
                        }
                        int type = (int) params[++i];
                        stmt.setNull(index, type);
                        continue;
                    }
                    stmt.setString(index, (String) obj);
                } else if (obj instanceof Integer) stmt.setInt(index, (int) obj);
                else if (obj instanceof Double) stmt.setDouble(index, (double) obj);
                else if (obj instanceof Long) stmt.setLong(index, (long) obj);
                else if (obj instanceof Timestamp) stmt.setTimestamp(index, (Timestamp) obj);
                else if (obj instanceof Time) stmt.setTime(index, (Time) obj);
                else if (obj instanceof Boolean) stmt.setBoolean(index, (Boolean) obj);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void setParams(PreparedStatement stmt, Object[] params) {
        setParams(stmt, params, false);
    }

    public Object[] select(String database, String condition, String orderClause, SQLQuery query, Object... values) {
        try {
            if (connection.isClosed() || !connection.isValid(5)) connect();
            StringBuilder builder = new StringBuilder();
            builder.append("SELECT * FROM `" + database + "`");
            if (condition != null && !condition.equals("")) builder.append(" WHERE ").append(condition);
            if (orderClause != null && !orderClause.equals("")) builder.append(" " + orderClause);
            return getResults(builder.toString(), query, values);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public Object[] selectDistinct(String database, String field, String order, SQLQuery query) {
        try {
            if (connection.isClosed() || !connection.isValid(5)) connect();
            StringBuilder builder = new StringBuilder();
            builder.append("SELECT DISTINCT " + field + " FROM " + database);
            if (order != null) builder.append(" ORDER BY " + order);
            return getResults(builder.toString(), query, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public Object[] select(String database, SQLQuery query) {
        return select(database, null, query);
    }

    public Object[] select(String database, String condition, SQLQuery query, Object... values) {
        try {
            if (connection.isClosed() || !connection.isValid(5)) connect();
            StringBuilder builder = new StringBuilder();
            builder.append("SELECT * FROM " + database);
            if (condition != null && !condition.equals("")) builder.append(" WHERE ").append(condition);
            Object[] data = getResults(builder.toString(), query, values);
            return data;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public int selectDistinctCount(String database, String field, Object... values) {
        try {
            if (connection.isClosed() || !connection.isValid(5)) connect();
            StringBuilder builder = new StringBuilder();
            builder.append("SELECT COUNT(distinct " + field + ") FROM " + database);
            PreparedStatement stmt = connection.prepareStatement(builder.toString());
            setParams(stmt, values);
            ResultSet set = stmt.executeQuery();
            if (!set.next()) return 0;
            int count = set.getInt(1);
            set.close();
            stmt.close();
            return count;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public int selectCount(String database, String condition, Object... values) {
        try {
            if (connection.isClosed() || !connection.isValid(5)) connect();
            StringBuilder builder = new StringBuilder();
            builder.append("SELECT COUNT(*) FROM " + database);
            if (condition != null && !condition.equals("")) builder.append(" WHERE ").append(condition);
            PreparedStatement stmt = connection.prepareStatement(builder.toString());
            setParams(stmt, values);
            ResultSet set = stmt.executeQuery();
            if (!set.next()) return 0;
            int count = set.getInt(1);
            set.close();
            stmt.close();
            return count;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public ResultSet callProcedure(String procedureName) {
        try {
            CallableStatement stmt = connection.prepareCall("{call " + procedureName + "}");
            return stmt.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public Object[] getResults(String builder, SQLQuery query, Object... values) {
        try {
            PreparedStatement stmt = null;
            ResultSet set = null;
            Object[] result;
            try {
                stmt = connection.prepareStatement(builder);
                setParams(stmt, values);
//                System.out.println(stmt);
                set = stmt.executeQuery();
                result = query.handleResult(set);
            } finally {
                if (stmt != null) stmt.close();
                if (set != null) set.close();
            }
            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public int insert(String database, Object... objects) {
        try {
            if (connection.isClosed() || !connection.isValid(5)) connect();
            int inserts = objects.length;
            Object[] objs = objects;
            StringBuilder insert = new StringBuilder();
            for (int i = 0; i < inserts; i++) {
                Object obj = objs[i];
                if (obj == null) {
                    insert.append("NULL");
                    if (i != inserts - 1) insert.append(", ");
                    continue;
                } else if (obj instanceof String) {
                    String string = (String) obj;
                    if (string.equals("DEFAULT") || string.equals("NULL")) {
                        insert.append(string);
                        if (i != inserts - 1) insert.append(", ");
                        continue;
                    }
                }
                insert.append("?");
                if (i != inserts - 1) insert.append(", ");
            }
            String query = "INSERT INTO `" + database + "` VALUES(" + insert.toString() + ")";
            PreparedStatement stmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            setParams(stmt, objects);
            System.out.println(stmt);
            stmt.execute();
            ResultSet set = stmt.getGeneratedKeys();
            if (set.next()) return set.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public void delete(String database, String condition, Object... values) {
        StringBuilder builder = new StringBuilder();
        builder.append("DELETE FROM ").append("`" + database + "`").append(" WHERE ").append(condition);
        try {
            PreparedStatement stmt = connection.prepareStatement(builder.toString());
            setParams(stmt, values);
            stmt.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    public void delete(String database, String condition) {
        StringBuilder builder = new StringBuilder();
        builder.append("DELETE FROM ").append("`" + database + "`").append(condition != null ? " WHERE " : "").append(condition != null ? condition : "");
        execute(builder.toString());
    }

    public void execute(String query) {
        try {
            if (connection.isClosed() || !connection.isValid(5)) connect();
            PreparedStatement statement = connection.prepareStatement(query);
            statement.execute();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void execute(String query, Object... values) {
        try {
            if (connection.isClosed() || !connection.isValid(5)) connect();
            PreparedStatement stmt = connection.prepareStatement(query);
            for (int i = 0; i < values.length; i++) {
                Object obj = values[i];
                int index = i + 1;
                if (obj instanceof String) stmt.setString(index, (String) obj);
                else if (obj instanceof Integer) stmt.setInt(index, (int) obj);
                else if (obj instanceof Double) stmt.setDouble(index, (double) obj);
                else if (obj instanceof Long) stmt.setTimestamp(index, new Timestamp((long) obj));
            }
            stmt.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public ResultSet executeQuery(String query) {
        try {
            if (connection.isClosed() || !connection.isValid(5)) connect();
            PreparedStatement statement = connection.prepareStatement(query);
            ResultSet set = statement.executeQuery();
            if (set != null) return set;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public ResultSet executeQuery(String query, Object... values) {
        try {
            if (connection.isClosed() || !connection.isValid(5)) connect();
            PreparedStatement statement = connection.prepareStatement(query);
            for (int i = 0; i < values.length; i++) {
                Object obj = values[i];
                int index = i + 1;
                if (obj instanceof String) statement.setString(index, (String) obj);
                else if (obj instanceof Integer) statement.setInt(index, (int) obj);
                else if (obj instanceof Double) statement.setDouble(index, (double) obj);
                else if (obj instanceof Long) statement.setTimestamp(index, new Timestamp((long) obj));
            }
            ResultSet set = statement.executeQuery();
            if (set != null) return set;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public <T> ArrayList<T> getArrayList(Class<T> returnType) {
        return null;
    }

    public boolean containsRow(ResultSet set, String row) {
        try {
            ResultSetMetaData rsMetaData = set.getMetaData();
            int numberOfColumns = rsMetaData.getColumnCount();

            // get the column names; column indexes start from 1
            for (int i = 1; i < numberOfColumns + 1; i++) {
                String columnName = rsMetaData.getColumnName(i);
                // Get the name of the column's table name
                if (row.equals(columnName)) return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public int getFetchSize(ResultSet set) {
        try {
            return set.getFetchSize();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public boolean getBoolean(ResultSet set, String string) {
        try {
            return set.getBoolean(string);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public Object getObject(ResultSet set, String string) {
        try {
            return set.getObject(string);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public long getLongInt(ResultSet set, String string) {
        try {
            return set.getLong(string);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public int getInt(ResultSet set, String string) {
        try {
            return set.getInt(string);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public int getInt(ResultSet set, int index) {
        try {
            return set.getInt(index);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public double getDouble(ResultSet set, String string) {
        try {
            return set.getDouble(string);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public Date getDate(ResultSet set, String string) {
        try {
            return set.getDate(string);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public Time getTime(ResultSet set, String string) {
        try {
            return set.getTime(string);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public Timestamp getTimestamp(ResultSet set, String string) {
        try {
            return set.getTimestamp(string);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getString(ResultSet set, String string) {
        try {
            return set.getString(string);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public int getRow(ResultSet set) {
        try {
            return set.getRow();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public boolean last(ResultSet set) {
        try {
            return set.last();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean next(ResultSet set) {
        try {
            return set.next();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean wasNull(ResultSet set) {
        if (set == null) return true;
        try {
            return set.wasNull();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return true;
    }

    public boolean empty(ResultSet set) {
        return set == null || wasNull(set) || !next(set);
    }

}
