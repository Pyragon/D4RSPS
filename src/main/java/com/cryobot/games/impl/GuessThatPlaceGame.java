package com.cryobot.games.impl;

import com.cryobot.DiscordBot;
import com.cryobot.db.impl.GamesConnection;
import com.cryobot.db.impl.MiscConnection;
import com.cryobot.entities.Game;
import com.cryobot.entities.Place;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.entities.User;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GuessThatPlaceGame extends Game {

    private static ArrayList<Place> places;

    static {
        loadPlaces();
    }

    private Place currentPlace;
    private long messageId;

    @Override
    public String getName() {
        return "guess-that-place";
    }

    public static String handleFindCommand(String username, long id, int x, int y, int plane) {
        GuessThatPlaceGame game = DiscordBot.getInstance().getGameManager().getGame("guess-that-place");
        if (game.currentPlace == null)
            return "There is no game happening right now. Perhaps someone has already won?";
        int diffX = Math.abs(game.currentPlace.getX() - x);
        int diffY = Math.abs(game.currentPlace.getY() - y);
        if (diffX > 15 || diffY > 15 || plane != game.currentPlace.getPlane())
            return "Unfortunately this is not the correct place. Please make sure you're within 15 tiles of the picture.";
        if (diffX < 15 && diffY < 15 && plane == game.currentPlace.getPlane()) {
            game.end(username, Long.toString(id));
            return "Congratulations. You've got the correct place. Check discord for your reward.";
        }
        return "Unfortunately this is not the correct place. Please make sure you're within 15 tiles of the picture.";
    }

    public static void addPlace(Place place) {
        places.add(place);
        GamesConnection.connection().handleRequest("add-place", place);
    }

    @Override
    public boolean startGame() {
        long channelId = MiscConnection.getLong("guess-that-place-channel");
        if (channelId == 0L) return false;
        if (places.size() == 0) return false;
        ArrayList<Place> places = (ArrayList<Place>) this.places.clone();
        Collections.shuffle(places);
        Place place = places.get(0);
        if (place == null) return false;
        messageId = sendMessage(buildQuestion(place));
        currentPlace = place;
        return true;
    }

    @Override
    public int getPoints() {
        return 2;
    }

    @Override
    public void end(String... params) {
        if (messageId != 0L) {
            long channelId = MiscConnection.getLong("guess-that-place-channel");
            if (channelId == 0L) return;
            long guildId = MiscConnection.getLong("guild-id");
            Message message = DiscordBot.getInstance()
                    .getJda()
                    .getGuildById(guildId)
                    .getTextChannelById(channelId)
                    .getMessageById(messageId)
                    .complete();
            if (message == null) return;
            String winner = params.length > 0 ? params[0] : null;
            if (winner != null)
                winner = DiscordBot.getInstance().getHelper().getDisplayName(winner);
            message.editMessage(buildWinningMessage(winner)).queue();
            if (params.length > 0) {
                long id = Long.parseLong(params[1]);
                User user = DiscordBot.getInstance().getJda().getUserById(id);
                GamesConnection.addPoints(id, getPoints());
                Object[] data = GamesConnection.connection().handleRequest("get-points", id);
                int points = 0;
                if (data != null)
                    points = (int) data[0];
                sendMessage(user.getAsMention() + " has won the guess that place game! " + user.getAsMention() + " now has " + points + " points!");
            }
        }
    }

    public static boolean hasPage(int page) {
        int start = (page - 1) * 10;
        return places.size() >= start;
    }

    public long sendMessage(MessageEmbed message) {
        long channelId = MiscConnection.getLong("guess-that-place-channel");
        if (channelId == 0L) return 0L;
        Message m = DiscordBot.getInstance().getJda().getTextChannelById(channelId).sendMessage(message).complete();
        if (m == null) return 0L;
        return m.getIdLong();
    }

    public long sendMessage(String text) {
        long channelId = MiscConnection.getLong("guess-that-place-channel");
        if (channelId == 0L) return 0L;
        Message message = DiscordBot.getInstance().getJda().getTextChannelById(channelId).sendMessage(text).complete();
        if (message == null) return 0L;
        return message.getIdLong();
    }

    public static MessageEmbed buildWinningMessage(String winner) {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle("Guess That Place!");
        builder.setDescription("Attempt to find the place in the picture in-game. Use ::find within 15x15 squares to guess.");
        builder.addField("Game Over!", winner == null ? "Game ended. No one answered correctly in time." : "Correct answer was guessed by: " + winner, false);
        return builder.build();
    }

    public static MessageEmbed buildQuestion(Place place) {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle("Guess That Place!");
        builder.setDescription("Attempt to find the place in the picture in-game. Use ::find within 15x15 squares to guess.");
        builder.setThumbnail(place.getImage());
        builder.addField("Image URL", place.getImage(), false);
        builder.addField("Hint", place.getHint(), false);
        return builder.build();
    }

    public static MessageEmbed buildPlacesList(int page) {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle("Trivia Questions");
        builder.setThumbnail("https://static.thenounproject.com/png/21610-200.png");
        builder.setDescription("Here is a list of all trivia questions. Respond with 'next' and 'prev' to change pages.");
        if (places.size() == 0) {
            builder.addField("None added!", "It doesn't look live you've added any places. Respond with 'add' to add some!", false);
            return builder.build();
        }
        if (page < 1 || !hasPage(page)) page = 1;
        int end = ((page - 1) * 10) + 9;
        if (end > places.size()) end = places.size();
        List<Place> places = GuessThatPlaceGame.places.subList((page - 1) * 10, end);
        places.forEach(place -> builder.addField(place.getHint(), place.getImage(), false));
        return builder.build();
    }

    private static void loadPlaces() {
        places = new ArrayList<>();
        Object[] data = GamesConnection.connection().handleRequest("get-places");
        if (data == null) return;
        places = (ArrayList<Place>) data[0];
    }
}
