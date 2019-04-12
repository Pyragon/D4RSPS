package com.cryo;

import com.cryo.commands.CommandManager;
import com.cryo.db.DBConnectionManager;
import com.cryo.db.impl.MiscConnection;
import com.cryo.dialogue.DialogueManager;
import com.cryo.tasks.TaskManager;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.Data;
import lombok.Getter;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.hooks.AnnotatedEventManager;

import javax.security.auth.login.LoginException;
import java.io.*;
import java.util.ArrayList;
import java.util.Properties;

@Data
public class DiscordBot {

    private CommandManager commandManager;
    private DBConnectionManager connectionManager;
    private DialogueManager dialogueManager;
    private TaskManager taskManager;
    private Gson gson;
    private Properties properties;
    private JDA jda;

    private ArrayList<Long> worldNewsChannels;

    @Getter
    private static DiscordBot instance;

    private final Helper helper;

    public void load() {
        gson = buildGson();
        loadProperties();
        connectionManager = new DBConnectionManager();
        connectionManager.init();
        commandManager = new CommandManager();
        commandManager.loadCommands();
        taskManager = new TaskManager();
        taskManager.start();
        dialogueManager = new DialogueManager();
        dialogueManager.load();
        loadNewsChannels();
        try {
            jda = new JDABuilder(AccountType.BOT)
                    .setToken(properties.getProperty("token"))
                    .setEventManager(new AnnotatedEventManager())
                    .addEventListener(new EventListener())
                    .buildBlocking();
        } catch (LoginException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void addNewsChannel(long channelId) {
        if (worldNewsChannels.contains(channelId)) return;
        worldNewsChannels.add(channelId);
        saveNewsChannels();
    }

    public void removeNewsChannel(long channelId) {
        if (!worldNewsChannels.contains(channelId)) return;
        worldNewsChannels.remove(channelId);
        saveNewsChannels();
    }

    public void loadNewsChannels() {
        Object[] data = MiscConnection.connection().handleRequest("get-value", "news-channels");
        if (data == null) worldNewsChannels = new ArrayList<>();
        else worldNewsChannels = gson.fromJson((String) data[0], ArrayList.class);
    }

    public void saveNewsChannels() {
        MiscConnection.connection().handleRequest("set-value", "news-channels", gson.toJson(worldNewsChannels));
    }

    public static void sendMessage(long channelId, String message) {
        getInstance().getJda().getTextChannelById(channelId).sendMessage(message).queue();
    }

    public static void main(String[] args) {
        instance = new DiscordBot(new Helper() {

            @Override
            public int getRights(String username) {
                return username.equals("cody") ? 2 : 0;
            }

            @Override
            public String getDisplayName(String username) {
                return Character.toString(username.charAt(0)).toUpperCase() + username.substring(1);
            }

            @Override
            public void sendFriendsChatMessage(String owner, String username, String message) {
                System.out.println("Message from " + username + " to " + owner + " " + message);
                Links.handleServerMessage("cody", "Cody", "Response");
            }

            @Override
            public long getRole(String username) {
                return 0;
            }

            @Override
            public long getOwnerId() {
                return 170473662095425536L;
            }

            @Override
            public long getGuildId() {
                return 458182076290695196L;
            }

            @Override
            public int getPlayersOnline() {
                return 10;
            }

            @Override
            public int getLevel(String username, int skill) {
                return username.equals("cody") ? 13 : 0;
            }

            @Override
            public double getXp(String username, int skill) {
                return 1028540316;
            }

            @Override
            public String getEquip(String username, int index) {
                return "Nothing";
            }
        });
        instance.load();
    }

    public long getId() {
        return jda.getSelfUser().getIdLong();
    }

    public static Gson buildGson() {
        return new GsonBuilder().serializeNulls().setVersion(1.0).disableHtmlEscaping().setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE).create();
    }

    public void loadProperties() {
        File file = new File("./props.json");
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line;
            StringBuilder json = new StringBuilder();
            while ((line = reader.readLine()) != null) json.append(line);
            properties = getGson().fromJson(json.toString(), Properties.class);
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveProperties() {
        File file = new File("./props.json");
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            writer.write(getGson().toJson(properties));
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
