package com.cryobot.games.impl;

import com.cryobot.DiscordBot;
import com.cryobot.db.impl.GamesConnection;
import com.cryobot.db.impl.MiscConnection;
import com.cryobot.entities.Game;
import com.cryobot.entities.Item;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageEmbed;

import java.util.ArrayList;
import java.util.Collections;

public class GuessThatItemGame extends Game {

    public static ArrayList<Item> items;
    private Item currentItem;

    private long messageId;

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
            end(message.getAuthor().getName());
            DiscordBot.getInstance().getGameManager().endGame();
        } else sendMessage("Incorrect " + message.getAuthor().getAsMention() + "! Please try again.");
    }

    @Override
    public void end(String... params) {
        if (messageId != 0L) {
            long channelId = MiscConnection.getLong("guess-that-item-channel");
            if (channelId == 0L) return;
            long guildId = MiscConnection.getLong("guild-id");
            Message message = DiscordBot.getInstance().getJda().getGuildById(guildId).getTextChannelById(channelId).getMessageById(messageId).complete();
            if (message == null) return;
            String winner = params.length > 0 ? params[0] : null;
            message.editMessage(buildWinningMessage(winner)).queue();
            currentItem = null;
        }
    }

    @Override
    public boolean startGame() {
        long channelId = MiscConnection.getLong("guess-that-item-channel");
        if (channelId == 0L) return false;
        ArrayList<Item> items = (ArrayList<Item>) this.items.clone();
        Collections.shuffle(items);
        currentItem = items.get(0);
        Message message = DiscordBot.getInstance().getJda().getTextChannelById(channelId).sendMessage(buildEmbedMessage(currentItem)).complete();
        if (message == null) return false;
        messageId = message.getIdLong();
        return true;
    }

    @Override
    public int getPoints() {
        return 2;
    }

    public long sendMessage(String text) {
        long channelId = MiscConnection.getLong("guess-that-item-channel");
        if (channelId == 0L) return 0L;
        Message message = DiscordBot.getInstance().getJda().getTextChannelById(channelId).sendMessage(text).complete();
        if (message == null) return 0L;
        return message.getIdLong();
    }

    public MessageEmbed buildWinningMessage(String winner) {
        try {
            EmbedBuilder builder = new EmbedBuilder();
            builder.setTitle("Guess that Item! - " + (winner == null ? "LOST" : "WON"));
            builder.setDescription("Use .guess (item name) to guess the item in the thumbnail and try to win some internet points!");
            builder.setThumbnail(currentItem.getItemPicUrl());
            builder.addField("Game Over!", winner == null ? "Game ended. No one answered correctly in time." : "Correct item was guessed by: " + winner, false);
            if (winner != null)
                builder.addField("Correct Answer", "Correct answer was: " + currentItem.getItemName(), false);
            return builder.build();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
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
