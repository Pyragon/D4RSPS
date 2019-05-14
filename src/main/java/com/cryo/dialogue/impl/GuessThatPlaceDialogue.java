package com.cryo.dialogue.impl;

import com.cryo.DiscordBot;
import com.cryo.db.impl.MiscConnection;
import com.cryo.entities.Dialogue;
import com.cryo.entities.Place;
import com.cryo.games.impl.GuessThatPlaceGame;
import com.cryo.utils.Utilities;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.PrivateChannel;
import net.dv8tion.jda.core.entities.TextChannel;

public class GuessThatPlaceDialogue extends Dialogue {

    private int page = 1;
    private long messageId;

    private int x, y, plane;

    private String hint;
    private String image;

    private Place place;

    public GuessThatPlaceDialogue(long id) {
        super(id);
    }

    @Override
    public String getName() {
        return "setup-place-guess";
    }

    @Override
    public void start(String[] parameters) {
        sendMessage("Hello, and welcome to the Guess That Place game setup dialogue. Let's get started.");
        long channelId = MiscConnection.getLong("guess-that-place-channel");
        if (channelId == 0L) {
            sendMessage("I have detected that you have not yet set a channel ID for this game to run in. Please enter it now.");
            sendMessage("You can find this by going to said channel, and using the command .channel-id");
            stage = -1;
            return;
        }
        sendMessage("Channel ID: " + channelId + ". Would you like to change this?");
        stage = 0;
    }

    @Override
    public void run(PrivateChannel channel, Message message, String response, String[] res) {
        if (stage == -1) {
            long channelId;
            try {
                channelId = Long.parseLong(response);
            } catch (Exception e) {
                sendMessage("Unable to parse channel id, please try again. Ensure you are entering a number.");
                return;
            }
            try {
                TextChannel textChannel = DiscordBot.getInstance().getJda().getTextChannelById(channelId);
                if (textChannel == null) throw new Exception("");
            } catch (Exception e) {
                sendMessage("Unable to see channel entered. Please try again and make sure it's a channel that is in your discord and the bot has access to it.");
                return;
            }
            MiscConnection.setLong("guess-that-place-channel", channelId);
            sendMessage("Awesome. Channel has been set. Moving on...");
            stage = 1;
            run(channel, message);
            return;
        } else if (stage == 0) {
            response = response.toLowerCase();
            if (response.equals("y") || response.equals("yes")) {
                sendMessage("What would you like the new channel id to be?");
                stage = -1;
                return;
            } else if (response.equals("n") || response.equals("no")) {
                sendMessage("Alright. Moving on...");
                stage = 1;
                run(channel, message);
                return;
            } else {
                sendMessage("Unable to process response. Please try again.");
                sendMessage("To end this dialogue, respond with 'end'.");
                return;
            }
        } else if (stage == 1) {
            if (response != null && (response.equalsIgnoreCase("next") || response.equalsIgnoreCase("prev")) && messageId != 0L) {
                int newPage = response.equalsIgnoreCase("next") ? 1 : -1;
                message.delete().queue();
                if (GuessThatPlaceGame.hasPage(newPage)) return;
                page = newPage;
                Message m = channel.getMessageById(messageId).complete();
                if (m == null) return;
                m.editMessage(GuessThatPlaceGame.buildPlacesList(page)).queue();
                return;
            }
            if (response != null && response.equalsIgnoreCase("add")) {
                sendMessage("Okay. Let's add some places.");
                stage = 2;
                run(channel, message);
                return;
            }
            sendMessage("The following is a list of currently added places. You can navigate this list by responding with 'prev' or 'next'.");
            sendMessage("To add a new place, respond with 'add' or respond with 'end' to end this dialogue.");
            messageId = sendMessage(GuessThatPlaceGame.buildPlacesList(page));
            return;
        } else if (stage == 2) {
            sendMessage("First, let's enter the coordinates. You can do this 2 ways. One is to manually respond with the coordinates in the following format: x, y, z (z is optional, default=0)");
            sendMessage("The next method is to go in-game, and type ::add-place-guess");
            sendMessage("This will automatically fill out the coordinates and progress this dialogue. Currently working on having it auto-screenshot as well");
        } else if (stage == 3) {
            String[] coords = response.split(", ");
            if (coords.length < 2 || coords.length > 3) {
                sendMessage("Invalid format. Please try again using: x, y, z (z is optional, default=0");
                sendMessage("You can end this dialogue by responding with 'end'");
                return;
            }
            x = Integer.parseInt(coords[0]);
            y = Integer.parseInt(coords[1]);
            if (coords.length > 2)
                plane = Integer.parseInt(coords[2]);
            stage = 4;
            run(channel, message);
            return;
        } else if (stage == 4) {
            sendMessage("Alright, let's check out these coordinates.");
            if (x < 0 || y < 0) {
                sendMessage("X nor Y can be below 0. Please try again");
                stage = 3;
                return;
            }
            if (DiscordBot.getInstance().getHelper().isDynamicRegion(x, y, plane)) {
                sendMessage("I have detected that you are currently standing in a dynamic region. Coordinates used here will not work for this game. Please try again in a different spot");
                stage = 3;
                return;
            }
            String nearest = determineLocation();
            sendMessage("Coordinates are (" + x + ", " + y + ", " + plane + ")");
            if (nearest == null)
                sendMessage("I was unable to determine a name for this area. This does not mean the coordinates are wrong.");
            else sendMessage("Determined area to be: " + nearest);
            sendMessage("Is this information correct?");
        } else if (stage == 5) {
            parseResponse(response, () -> {
                sendMessage("Sounds good. Moving on.");
                stage = 6;
                run(channel, message);
            }, () -> {
                stage = 4;
                sendMessage("Alright, let's enter the coordinates again. You can do this 2 ways. One is to manually respond with the coordinates in the following format: x, y, z (z is optional, default=0)");
                sendMessage("The next method is to go in-game, and type ::add-place-guess");
                sendMessage("This will automatically fill out the coordinates and progress this dialogue. Currently working on having it auto-screenshot as well");
            });
            return;
        } else if (stage == 6)
            sendMessage("Next, enter a url for the picture to show people where the place is. You can use any picture you want, but it should hopefully not give away the position too much.");
        else if (stage == 7) {
            if (!Utilities.isValidURL(response)) {
                sendMessage("I was unable to parse the image URL correctly. Please try again and make sure to include the full URL.");
                return;
            }
            sendMessage("The following image will be used. Is this correct?");
            sendMessage(response);
            image = response;
        } else if (stage == 8) {
            parseResponse(response, () -> sendMessage("Okay, almost done. What would you like the hint to be?"), () -> {
                this.stage = 6;
                sendMessage("Let's try again. What image would you like to use?");
            });
        } else if (stage == 9) {
            hint = response;
            sendMessage("The following hint will be used: " + hint);
            sendMessage("Is this correct?");
        } else if (stage == 10) {
            parseResponse(response, () -> {
                place = new Place(-1, x, y, plane, hint, image, null);
                sendMessage("That's it for information, last question. The following is what will be shown to players.");
                sendMessage(GuessThatPlaceGame.buildQuestion(place));
                sendMessage("Is this okay?");
            }, () -> {
                stage = 8;
                sendMessage("Okay, let's try again. What would you like the hint to be?");
            });
        } else if (stage == 11) {
            parseResponse(response, () -> {
                sendMessage("Perfect. Adding now.");
                GuessThatPlaceGame.addPlace(place);
                place = null;
                hint = null;
                image = null;
                x = 0;
                y = 0;
                plane = 0;
            }, () -> sendMessage("Okay. We'll have to start over then, unfortunately"));
            stage = 1;
            run(channel, message);
            return;
        }
        stage++;
    }

    public void enteredCoordinates(int x, int y, int plane) {
        if (stage != 3) return;
        this.x = x;
        this.y = y;
        this.plane = plane;
        this.stage = 4;
        run(null, null);
    }

    public String determineLocation() {
        //find and return map area name for location
        return null;
    }
}
