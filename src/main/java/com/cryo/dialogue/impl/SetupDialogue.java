package com.cryo.dialogue.impl;

import com.cryo.DiscordBot;
import com.cryo.db.impl.MiscConnection;
import com.cryo.entities.Dialogue;
import com.cryo.entities.Role;
import com.cryo.utils.HexValidator;
import com.mysql.jdbc.StringUtils;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.PrivateChannel;

import java.util.ArrayList;

public class SetupDialogue extends Dialogue {

    public SetupDialogue(long id) {
        super(id);
    }

    private String roleName;
    private boolean hasColour;
    private String colour;
    private boolean displaySeparately;
    private boolean mentionedByAnyone;

    private boolean giveOnPointsRequirement;
    private int pointsRequirement;

    private boolean giveOnCurrentPointsRequirement;
    private int currentPointsRequirement;

    private boolean giveToPointsLeader;
    private boolean giveToInGamestatus;
    private String inGameStatus;

    private boolean checkOld;
    private boolean overwrite;

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
    public void run(PrivateChannel channel, Message message, String response, String[] res) {
        if (response != null && response.equalsIgnoreCase("end")) {
            sendMessage("Goodbye...");
            end();
            return;
        }
        if (stage == 0) {
            if (parseNext(channel, message, response)) return;
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
            run(channel, message);
            return;
        } else if (stage == 1) {
            if (MiscConnection.getLong("guild-id") == 0) {
                sendMessage("A guild ID must be set to set this setting. Please set a guild id later and try again to use this setting.");
                sendMessage("Moving on...");
                stage = 3;
                run(channel, message);
            }
            sendMessage("Enter channels to display world news in: (separate channel ids by a comma. You can get channel ids by typing .channel-id in the desired channel)");
        } else if (stage == 2) {
            if (parseNext(channel, message, response)) return;
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
            run(channel, message);
            return;
        } else if (stage == 3) {
            sendMessage("Now we're going to setup some roles for users. The following roles are already active:");
            sendMessage(DiscordBot.getInstance().getRoleManager().getRolesEmbed());
            sendMessage("Would you like to add another role? (The following dialogue cannot be skipped using 'next'.)");
        } else if (stage == 4) {
            response = response.toLowerCase();
            if (response.equals("y") || response.equals("yes")) {
                sendMessage("Okay. What would you like this role's name to be?");
            } else if (response.equals("n") || response.equals("no")) {
                sendMessage("Okay. That's the end of setup. Goodbye.");
                end();
                return;
            } else {
                sendMessage("Unable to process response. Please try again. Would you like to add another role? (yes/y or no/n)");
                sendMessage("To end this dialogue, respond with 'end'.");
                return;
            }
        } else if (stage == 5) {
            roleName = response;
            if (StringUtils.isEmptyOrWhitespaceOnly(roleName)) {
                sendMessage("Role name cannot be blank! Please try again. To end this dialogue, respond with 'end'.");
                return;
            }
            if (DiscordBot.getInstance().getRoleManager().roleNameExists(roleName)) {
                sendMessage("Role name already exists. Would you like to overwrite this role?");
                stage = 30;
                return;
            }
            sendMessage("This role's name will be " + roleName + ". Is that okay?");
        } else if (stage == 6) {
            response = response.toLowerCase();
            if (response.equals("y") || response.equals("yes")) {
                sendMessage("Okay. What colour would you like this role to be? Respond with 'none' to use the default colour.");
            } else if (response.equals("n") || response.equals("no")) {
                sendMessage("Okay. Let's try again. What would you like this role's name to be?");
                stage = 5;
                return;
            } else {
                sendMessage("Unable to process response. Please try again. What would you like this role's name to be? (yes/y or no/n)");
                sendMessage("To end this dialogue, respond with 'end'.");
                stage = 5;
                return;
            }
        } else if (stage == 7) {
            if (response.equalsIgnoreCase("none")) {
                hasColour = false;
                sendMessage("Using the default colour. Is that okay?");
                stage = 8;
                return;
            }
            boolean valid = new HexValidator().validate(response);
            if (!valid) {
                sendMessage("Unable to parse hex colour. Please try again.");
                return;
            }
            colour = response;
            hasColour = true;
            sendMessage("The new colour is going to be: " + colour + ". Is that okay?");
        } else if (stage == 8) {
            response = response.toLowerCase();
            if (response.equals("y") || response.equals("yes")) {
                sendMessage("Alrighty. And would you like this role to display separately from others?");
            } else if (response.equals("n") || response.equals("no")) {
                sendMessage("Okay. Let's try again. What colour would you like this role to be? Respond with 'none' to use the default colour.");
                stage = 7;
                return;
            } else {
                sendMessage("Unable to process response. Please try again. Would colour would you like this role to be?");
                sendMessage("To end this dialogue, respond with 'end'.");
                stage = 7;
                return;
            }
        } else if (stage == 9) {
            response = response.toLowerCase();
            if (response.equals("y") || response.equals("yes"))
                displaySeparately = true;
            else if (response.equals("n") || response.equals("no"))
                displaySeparately = false;
            else {
                sendMessage("Unable to process response. Please try again. Would you like to add another role? (yes/y or no/n)");
                sendMessage("To end this dialogue, respond with 'end'.");
                return;
            }
            sendMessage("Perfect. This role will" + (!displaySeparately ? " not" : "") + " display separately from others. Is this okay?");
        } else if (stage == 10) {
            response = response.toLowerCase();
            if (response.equals("y") || response.equals("yes")) {
                sendMessage("And can this role be mentioned by anyone?");
            } else if (response.equals("n") || response.equals("no")) {
                sendMessage("Okay. Let's try again. Should this role display separately from others?");
                stage = 9;
                return;
            } else {
                sendMessage("Unable to process response. Please try again. Should this role display separately from others?");
                sendMessage("To end this dialogue, respond with 'end'.");
                stage = 9;
                return;
            }
        } else if (stage == 11) {
            response = response.toLowerCase();
            if (response.equals("y") || response.equals("yes")) {
                mentionedByAnyone = true;
            } else if (response.equals("n") || response.equals("no")) {
                mentionedByAnyone = false;
            } else {
                sendMessage("Unable to process response. Please try again. Can this role be mentioned by anyone?");
                sendMessage("To end this dialogue, respond with 'end'.");
                return;
            }
            sendMessage("Sounds good. So this role can" + (!mentionedByAnyone ? " not" : "") + " be mentioned by anyone. Is that okay?");
        } else if (stage == 12) {
            response = response.toLowerCase();
            if (response.equals("y") || response.equals("yes")) {
                sendMessage("And finally. When would you like this role to be given? Please select from the following options. And respond with the option #");
                sendMessage(DiscordBot.getInstance().getRoleManager().getRoleGiveOptionsEmbed());
            } else if (response.equals("n") || response.equals("no")) {
                sendMessage("Okay. Let's try again. Can this role be mentioned by anyone?");
                stage = 11;
                return;
            } else {
                sendMessage("Unable to process response. Please try again. Can this role be mentioned by anyone?");
                sendMessage("To end this dialogue, respond with 'end'.");
                stage = 11;
                return;
            }
        } else if (stage == 13) {
            int option;
            try {
                option = Integer.parseInt(response);
            } catch (Exception e) {
                sendMessage("Unable to parse option #. Please try again. Only respond with the # given before the option name.");
                return;
            }
            switch (option) {
                case 1:
                    stage = 14;
                    run(channel, message);
                    return;
                case 2:
                    stage = 18;
                    run(channel, message);
                    return;
                case 3:
                    stage = 22;
                    run(channel, message);
                    return;
                case 4:
                    stage = 26;
                    run(channel, message);
                    return;
                default:
                    sendMessage("Invalid option specified. Please try again.");
                    return;
            }
        } else if (stage == 14)
            sendMessage("Given on in-game promotion selected. Is this right?");
        else if (stage == 15) {
            response = response.toLowerCase();
            if (response.equals("y") || response.equals("yes")) {
                giveToInGamestatus = true;
                sendMessage("Sounds good. Now, enter the name of the in-game rank that you would like this role to be given to.");
                sendMessage("Full instructions can be found here: TODO");
            } else if (response.equals("n") || response.equals("no")) {
                sendMessage("Okay. Let's try again.");
                sendMessage("When would you like this role to be given? Please select from the following options. And respond with the option #");
                sendMessage(DiscordBot.getInstance().getRoleManager().getRoleGiveOptionsEmbed());
                stage = 13;
                return;
            } else {
                sendMessage("Unable to process response. Please try again.");
                sendMessage("To end this dialogue, respond with 'end'.");
                sendMessage("When would you like this role to be given? Please select from the following options. And respond with the option #");
                sendMessage(DiscordBot.getInstance().getRoleManager().getRoleGiveOptionsEmbed());
                stage = 13;
                return;
            }
        } else if (stage == 16) {
            if (StringUtils.isEmptyOrWhitespaceOnly(response)) {
                sendMessage("Unable to read status. Please try again.");
                return;
            }
            inGameStatus = response;
            sendMessage("This role will be added to users with the in-game " + inGameStatus + " rank. Is this right?");
        } else if (stage == 17) {
            response = response.toLowerCase();
            if (response.equals("y") || response.equals("yes")) {
                sendMessage("Requirement set.");
                stage = 28;
                run(channel, message);
                return;
            } else if (response.equals("n") || response.equals("no")) {
                sendMessage("Okay. Let's try again.");
                sendMessage("Enter the name of the in-game rank that you would like this role to be given to.");
                sendMessage("Full instructions can be found here: TODO");
                stage = 16;
                return;
            } else {
                sendMessage("Unable to process response. Please try again.");
                sendMessage("To end this dialogue, respond with 'end'.");
                sendMessage("Enter the name of the in-game rank that you would like this role to be given to.");
                sendMessage("Full instructions can be found here: TODO");
                stage = 16;
                return;
            }
        } else if (stage == 18) {
            sendMessage("Given on total # of points earned. Is this okay?");
        } else if (stage == 19) {
            response = response.toLowerCase();
            if (response.equals("y") || response.equals("yes")) {
                giveOnPointsRequirement = true;
                sendMessage("Alrighty. Enter the # of points you would like this role to be given on.");
                sendMessage("This role will be given to those who have earned this # of points all together. Regardless of spent points.");
            } else if (response.equals("n") || response.equals("no")) {
                sendMessage("Okay. Let's try again.");
                sendMessage("When would you like this role to be given? Please select from the following options. And respond with the option #");
                sendMessage(DiscordBot.getInstance().getRoleManager().getRoleGiveOptionsEmbed());
                stage = 13;
                return;
            } else {
                sendMessage("Unable to process response. Please try again.");
                sendMessage("To end this dialogue, respond with 'end'.");
                sendMessage("When would you like this role to be given? Please select from the following options. And respond with the option #");
                sendMessage(DiscordBot.getInstance().getRoleManager().getRoleGiveOptionsEmbed());
                stage = 13;
                return;
            }
        } else if (stage == 20) {
            int points;
            try {
                points = Integer.parseInt(response);
            } catch (Exception e) {
                sendMessage("Unable to parse # of points. Let's try again. Please make sure to enter a number.");
                sendMessage("You may end the dialogue by responding with 'end'.");
                return;
            }
            pointsRequirement = points;
            sendMessage("The requirement for this role is " + pointsRequirement + " points. Is this okay?");
        } else if (stage == 21) {
            response = response.toLowerCase();
            if (response.equals("y") || response.equals("yes")) {
                sendMessage("Requirement set.");
                stage = 28;
                run(channel, message);
                return;
            } else if (response.equals("n") || response.equals("no")) {
                sendMessage("Okay. Let's try again.");
                sendMessage("Enter the # of points you would like this role to be given on.");
                sendMessage("This role will be given to those who have earned this # of points all together. Regardless of spent points.");
                stage = 19;
                return;
            } else {
                sendMessage("Unable to process response. Please try again.");
                sendMessage("To end this dialogue, respond with 'end'.");
                sendMessage("Enter the # of points you would like this role to be given on.");
                sendMessage("This role will be given to those who have earned this # of points all together. Regardless of spent points.");
                stage = 19;
                return;
            }
        } else if (stage == 22) {
            sendMessage("Given on current points requirement. Is this right?");
        } else if (stage == 23) {
            response = response.toLowerCase();
            if (response.equals("y") || response.equals("yes")) {
                giveOnCurrentPointsRequirement = true;
                sendMessage("Sounds good. Enter the # of points you would like this requirement to be set at.");
            } else if (response.equals("n") || response.equals("no")) {
                sendMessage("Okay. Let's try again.");
                sendMessage("When would you like this role to be given? Please select from the following options. And respond with the option #");
                sendMessage(DiscordBot.getInstance().getRoleManager().getRoleGiveOptionsEmbed());
                stage = 13;
                return;
            } else {
                sendMessage("Unable to process response. Please try again.");
                sendMessage("To end this dialogue, respond with 'end'.");
                sendMessage("When would you like this role to be given? Please select from the following options. And respond with the option #");
                sendMessage(DiscordBot.getInstance().getRoleManager().getRoleGiveOptionsEmbed());
                stage = 13;
                return;
            }
        } else if (stage == 24) {
            int points;
            try {
                points = Integer.parseInt(response);
            } catch (Exception e) {
                sendMessage("Unable to parse # of points. Let's try again. Please make sure to enter a number.");
                sendMessage("You may end the dialogue by responding with 'end'.");
                return;
            }
            currentPointsRequirement = points;
            sendMessage("Requirement set at " + points + " points. Is this right?");
        } else if (stage == 25) {
            response = response.toLowerCase();
            if (response.equals("y") || response.equals("yes")) {
                sendMessage("Requirement set.");
                stage = 28;
                run(channel, message);
                return;
            } else if (response.equals("n") || response.equals("no")) {
                sendMessage("Okay. Let's try again.");
                sendMessage("Enter the # of points you would like this requirement to be set at.");
                sendMessage("This role will be given to those who currently have more points than the requirement. It will be removed from users should they fall below this requirement.");
                stage = 23;
                return;
            } else {
                sendMessage("Unable to process response. Please try again.");
                sendMessage("To end this dialogue, respond with 'end'.");
                sendMessage("Enter the # of points you would like this role to be given on.");
                sendMessage("This role will be given to those who currently have more points than the requirement. It will be removed from users should they fall below this requirement.");
                stage = 23;
                return;
            }
        } else if (stage == 26) {
            sendMessage("Alright. This role will be given to the current points leader(s). It will be removed from all users that have less than the highest # of points. Is this okay?");
        } else if (stage == 27) {
            response = response.toLowerCase();
            if (response.equals("y") || response.equals("yes")) {
                giveToPointsLeader = true;
                sendMessage("Requirement set.");
                stage = 28;
                run(channel, message);
                return;
            } else if (response.equals("n") || response.equals("no")) {
                sendMessage("Okay. Let's try again.");
                sendMessage("When would you like this role to be given? Please select from the following options. And respond with the option #");
                sendMessage(DiscordBot.getInstance().getRoleManager().getRoleGiveOptionsEmbed());
                stage = 13;
                return;
            } else {
                sendMessage("Unable to process response. Please try again.");
                sendMessage("To end this dialogue, respond with 'end'.");
                sendMessage("When would you like this role to be given? Please select from the following options. And respond with the option #");
                sendMessage(DiscordBot.getInstance().getRoleManager().getRoleGiveOptionsEmbed());
                stage = 13;
                return;
            }
        } else if (stage == 28) {
            sendMessage("Perfect. Everything is looking good. Last question.");
            sendMessage("Would you like to go through all of the currently linked accounts and assign this role to those that meet the requirement?");
        } else if (stage == 29) {
            response = response.toLowerCase();
            if (response.equals("y") || response.equals("yes")) {
                checkOld = true;
            } else if (response.equals("n") || response.equals("no")) {
                checkOld = false;
            } else {
                sendMessage("Unable to process response. Please try again.");
                sendMessage("To end this dialogue, respond with 'end'.");
                sendMessage("Would you like to go through all of the currently linked accounts and assign this role to those that meet the requirement?");
                return;
            }
            sendMessage("Everything is in order. Adding role" + (checkOld ? " and giving to users who meet requirement" : "."));
            Role role = new Role(-1, 0L, roleName, hasColour, colour == null ? "" : colour, displaySeparately, mentionedByAnyone, giveOnPointsRequirement, pointsRequirement, giveOnCurrentPointsRequirement, currentPointsRequirement, giveToPointsLeader, giveToInGamestatus, inGameStatus, null);
            DiscordBot.getInstance().getRoleManager().saveRole(role, overwrite);
            if (checkOld)
                DiscordBot.getInstance().getRoleManager().recheckAllRoles();
            sendMessage("Would you like to add another role?");
            stage = 3;
        } else if (stage == 30) {
            response = response.toLowerCase();
            if (response.equals("y") || response.equals("yes")) {
                overwrite = true;
                sendMessage("Okay. What colour would you like this role to be? Respond with 'none' to use the default colour.");
                stage = 7;
                return;
            } else if (response.equals("n") || response.equals("no")) {
                sendMessage("Okay. What would you like this role's name to be?");
                stage = 5;
                return;
            } else {
                sendMessage("Unable to process response. Please try again. Would you like to overwrite this role? (yes/y or no/n)");
                sendMessage("To end this dialogue, respond with 'end'.");
                stage = 5;
                return;
            }
        }
        stage++;
    }

    public boolean parseNext(PrivateChannel channel, Message message, String response) {
        if (!response.equalsIgnoreCase("next")) return false;
        stage++;
        run(channel, message);
        return true;
    }
}
