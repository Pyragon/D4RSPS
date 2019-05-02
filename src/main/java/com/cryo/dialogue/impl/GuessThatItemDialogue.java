package com.cryo.dialogue.impl;

import com.cryo.DiscordBot;
import com.cryo.db.impl.MiscConnection;
import com.cryo.entities.Dialogue;
import com.cryo.entities.Item;
import com.cryo.games.impl.GuessThatItemGame;
import com.cryo.utils.Utilities;
import com.mysql.jdbc.StringUtils;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.PrivateChannel;

public class GuessThatItemDialogue extends Dialogue {

    private String itemName;
    private String itemPicUrl;
    private String hint;

    public GuessThatItemDialogue(long id) {
        super(id);
    }

    @Override
    public String getName() {
        return "guess-that-item";
    }

    @Override
    public void start(String[] parameters) {
        sendMessage("Hello, and welcome to the setup dialogue for the 'Guess That Item' game.");
        sendMessage("This is quite a simple game, so all we need to do is add some items to it.");
        sendMessage("You can type 'end' at any point to end this dialogue.");
        long channelId = MiscConnection.getLong("guess-that-item-channel");
        if (channelId == 0L) {
            stage = -1;
            sendMessage("I have detected that a channel has not been set up yet. Please enter the ID of the channel you would like this game to play in. (Can be found with .channel-id command in desired channel.");
            return;
        }
        run(null, null);
    }

    @Override
    public void run(PrivateChannel channel, Message message, String response, String[] res) {
        if (response != null && response.equalsIgnoreCase("end")) {
            sendMessage("Ending dialogue. Goodbye.");
            end();
            return;
        }
        if (stage == -1) {
            try {
                long channelId = Long.parseLong(response);
                if (channelId == 0L) {
                    sendMessage("Error parsing channel ID. Please try again.");
                    return;
                }
                if (DiscordBot.getInstance().getJda().getTextChannelById(channelId) == null) {
                    sendMessage("Error parsing channel ID. Please try again.");
                    return;
                }
                MiscConnection.setLong("guess-that-item-channel", channelId);
                stage++;
                run(channel, message);
                return;
            } catch (Exception e) {
                e.printStackTrace();
                sendMessage("Error parsing channel ID. Please try again.");
                return;
            }
        } else if (stage == 0)
            sendMessage("Please enter the name for this new item: (Case insensitive, but spelling and spaces must be exact)");
        else if (stage == 1) {
            itemName = response;
            if (StringUtils.isEmptyOrWhitespaceOnly(itemName)) {
                sendMessage("Name is invalid. Please try again. To end this dialogue, respond with 'end'.");
                return;
            }
            String itemPicUrl = Utilities.getItemPicture(itemName);
            sendMessage("Item name is: " + itemName + ". Respond with 'rename' to change the name. Otherwise, please respond with a URL to a picture of the item.");
            if (itemPicUrl != null) sendMessage("Suggested picture: " + itemPicUrl);
        } else if (stage == 2) {
            if (!Utilities.isValidURL(response)) {
                sendMessage("Invalid URL provided. Please try again. To end this dialogue, respond with 'end'.");
                return;
            }
            itemPicUrl = response;
            sendMessage("URL saved. Would you like to add a hint for this item? (yes/y or no/n)");
        } else if (stage == 3) {
            response = response.toLowerCase();
            if (response.equals("y") || response.equals("yes")) {
                sendMessage("Okay. What would you like the hint to be?");
            } else if (response.equals("n") || response.equals("no")) {
                stage = 6;
                run(channel, message);
                return;
            } else {
                sendMessage("Unable to process response. Please try again. Would you like to add a hint for this item? (yes/y or no/n)");
                sendMessage("To end this dialogue, respond with 'end'.");
                return;
            }
        } else if (stage == 4) {
            hint = response;
            sendMessage("Hint is saved as follows. Is this okay? (yes/y or no/n)");
            sendMessage(hint);
        } else if (stage == 5) {
            response = response.toLowerCase();
            if (response.equals("y") || response.equals("yes")) {
                stage = 6;
                run(channel, message);
                return;
            } else if (response.equals("n") || response.equals("no")) {
                sendMessage("Okay. What would you like the hint to be?");
                stage = 4;
                return;
            } else {
                sendMessage("Unable to process response. Please try again. Would you like to add a hint for this item? (yes/y or no/n)");
                sendMessage("To end this dialogue, respond with 'end'.");
                stage = 4;
                return;
            }
        } else if (stage == 6) {
            sendMessage("Okay, here's what your item would look like.");
            sendMessage(GuessThatItemGame.buildEmbedMessage(new Item(-1, itemName, itemPicUrl, hint, null)));
            sendMessage("Is this okay? yes/y to save, no/n to cancel.");
        } else if (stage == 7) {
            response = response.toLowerCase();
            if (response.equals("y") || response.equals("yes")) {
                GuessThatItemGame.addGuessItem(new Item(-1, itemName, itemPicUrl, hint, null));
                sendMessage("Item successfully saved. Would you like to add another?");
            } else if (response.equals("n") || response.equals("no")) {
                sendMessage("Okay. We're going to have to start over.");
                stage = 0;
                run(channel, message);
                return;
            } else {
                sendMessage("Unable to process response. Please try again.");
                sendMessage("To end this dialogue, respond with 'end'.");
                return;
            }
        } else if (stage == 8) {
            response = response.toLowerCase();
            if (response.equals("y") || response.equals("yes")) {
                stage = 0;
                run(channel, message);
                return;
            } else if (response.equals("n") || response.equals("no")) {
                sendMessage("Goodbye.");
                end();
                return;
            } else {
                sendMessage("Unable to process response. Please try again.");
                sendMessage("To end this dialogue, respond with 'end'.");
                return;
            }
        }
        stage++;
    }
}
