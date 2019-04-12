package com.cryo.dialogue.impl;

import com.cryo.DiscordBot;
import com.cryo.db.impl.MiscConnection;
import com.cryo.entities.Dialogue;

import java.util.ArrayList;

public class SetupDialogue extends Dialogue {

    public SetupDialogue(long id) {
        super(id);
    }

    @Override
    public String getName() {
        return "setup";
    }

    @Override
    public void start(String[] parameters) {
        sendMessage("Hello, and thank you for using D4RSPS. Setup the bot using the following options. To skip any option and keep the current value, type 'next'. Type 'end' at any point to end the dialogue.");
        sendMessage("Enter Guild ID. Current: (" + MiscConnection.getLong("guild-id") + ") Guild ID can be found by using command .guild-id in any channel within the guild.");
    }

    @Override
    public void run(String response, String[] res) {
        if (response != null && response.equalsIgnoreCase("end")) {
            end();
            return;
        }
        if (stage == 0) {
            if (parseNext(response)) return;
            long guildId;
            try {
                guildId = Long.parseLong(response);
            } catch (Exception e) {
                sendMessage("Error parsing guild id. Please try again.");
                return;
            }
            MiscConnection.connection().handleRequest("set-value", "guild-id", guildId);
            sendMessage("Guild ID set to " + guildId + ".");
            stage++;
            run();
        } else if (stage == 1) {
            if (MiscConnection.getLong("guild-id") == 0) {
                sendMessage("A guild ID must be set to set this setting. Please set a guild id later and try again to use this setting.");
                sendMessage("Moving on...");
                stage = 3;
                run();
            }
            sendMessage("Enter channels to display world news in: (separate channel ids by a comma. You can get channel ids by typing .channel-id in the desired channel)");
            stage++;
        } else if (stage == 2) {
            if (parseNext(response)) return;
            boolean error = false;
            String[] idStrings = response.split(", ?");
            ArrayList<Long> ids = new ArrayList<>();
            for (String idString : idStrings) {
                try {
                    long id = Long.parseLong(idString);
                    ids.add(id);
                } catch (Exception e) {
                    error = true;
                    continue;
                }
            }
            if (error)
                sendMessage("Not all channel ids were valid. This could be because the bot cannot find the channel.");
            ids.forEach(id -> DiscordBot.getInstance().addNewsChannel(id));
            sendMessage("World news channels set. View these by using command .list-news-chats any in channel in the guild.");
            stage++;
            run();
        } else if (stage == 3) {
            sendMessage("Here are all games loaded by the bot and their statuses. Enter the index of the game to toggle it's status. Separate indices by a comma to toggle more than one.");

        }
    }

    public boolean parseNext(String response) {
        if (!response.equalsIgnoreCase("next")) return false;
        stage++;
        run();
        return true;
    }
}
