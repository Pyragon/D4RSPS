package com.cryo.games.impl;

import com.cryo.DiscordBot;
import com.cryo.db.impl.GamesConnection;
import com.cryo.db.impl.MiscConnection;
import com.cryo.entities.Game;
import com.cryo.entities.Item;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageEmbed;

import java.util.ArrayList;
import java.util.Collections;

public class GuessThatItemGame extends Game {

    public static ArrayList<Item> items;
    private Item currentItem;

    static {
        items = getGuessItems();
    }

    @Override
    public String getName() {
        return "Guess That Item";
    }

    @Override
    public void processGuessCommand(Message message, String command, String[] cmd) {
        boolean success = command.substring(6).equalsIgnoreCase(currentItem.getItemName());
        if (success) {
            GamesConnection.addPoints(message.getAuthor().getIdLong(), 2);
            Object[] data = GamesConnection.connection().handleRequest("get-points", message.getAuthor().getIdLong());
            int points = 0;
            if (data != null)
                points = (int) data[0];
            sendMessage("Correct " + message.getAuthor().getAsMention() + "! You have been awarded 2 points! You now have " + points + " points.");
        } else sendMessage("Incorrect " + message.getAuthor().getAsMention() + "! Please try again.");
    }

    @Override
    public boolean startGame() {
        long channelId = MiscConnection.getLong("guess-that-item-channel");
        if (channelId == 0L) return false;
        ArrayList<Item> items = (ArrayList<Item>) this.items.clone();
        Collections.shuffle(items);
        currentItem = items.get(0);
        DiscordBot.getInstance().getJda().getTextChannelById(channelId).sendMessage(buildEmbedMessage(currentItem)).queue();
        return true;
    }

    @Override
    public int getPoints() {
        return 2;
    }

    public void sendMessage(String message) {
        long channelId = MiscConnection.getLong("guess-that-item-channel");
        if (channelId == 0L) return;
        DiscordBot.getInstance().getJda().getTextChannelById(channelId).sendMessage(message).queue();
    }

    public static MessageEmbed buildEmbedMessage(Item item) {
        try {
            EmbedBuilder builder = new EmbedBuilder();
            builder.setTitle("Guess that Item!");
            builder.setDescription("Use .guess (item name) to guess the item in the thumbnail and try to win some internet points!");
            builder.setThumbnail(item.getItemPicUrl());
            if (item.getHint() != null)
                builder.addField("Hint", item.getHint(), false);
            return builder.build();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static ArrayList<Item> getGuessItems() {
        Object[] data = GamesConnection.connection().handleRequest("get-guess-items");
        return (ArrayList<Item>) data[0];
    }

    public static void addGuessItem(Item item) {
        items.add(item);
        GamesConnection.connection().handleRequest("add-guess-item", item);
    }
}
