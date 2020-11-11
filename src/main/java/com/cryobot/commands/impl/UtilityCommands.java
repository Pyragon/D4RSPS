package com.cryobot.commands.impl;

import com.cryobot.DiscordBot;
import com.cryobot.db.impl.AccountConnection;
import com.cryobot.entities.Command;
import com.cryobot.entities.Game;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class UtilityCommands implements Command {

    @Override
    public int getPermissionsReq(String command) {
        switch (command) {
            case "purge":
            case "guild-id":
            case "my-id":
            case "channel-id":
                return 1;
            default:
                return 0;
        }
    }

    @Override
    public String[] getAliases() {
        return new String[]{"purge", "guild-id", "my-id", "channel-id", "guess", "help"};
    }

    @Override
    public void handleCommand(Message message, String command, String[] cmd) {
        switch (cmd[0]) {
            case "purge":
                if (cmd.length == 1) {
                    message.getChannel().sendMessage("Incorrect syntax. Correct usage: .purge (num)").queue();
                    return;
                }
                long channelId = message.getChannel().getIdLong();
                TextChannel channel = DiscordBot.getInstance().getJda().getTextChannelById(channelId);
                if (cmd[1].equals("all")) {
                    while (true) {
                        List<Message> messages = channel.getHistory().retrievePast(50).complete();
                        OffsetDateTime twoWeeksAgo = OffsetDateTime.now().minus(2, ChronoUnit.WEEKS);
                        messages.removeIf(m -> m.getTimeCreated().isBefore(twoWeeksAgo));
                        if (messages.isEmpty() || messages.size() < 2) break;
                        channel.deleteMessages(messages).complete();
                    }
                } else {
                    int length;
                    try {
                        length = Integer.parseInt(cmd[1]) + 1;
                    } catch (Exception e) {
                        message.getChannel().sendMessage("Incorrect syntax. Correct usage: .purge (num)").queue();
                        return;
                    }
                    channel.getHistory().retrievePast(length).queue(l -> l.forEach(m -> m.delete().queue()));
                }
                break;
            case "guess":
                Game game = DiscordBot.getInstance().getGameManager().getCurrentGame();
                if (game == null) break;
                game.processGuessCommand(message, command, cmd);
                break;
            case "my-id":
                message.getChannel().sendMessage("Your Discord ID: " + message.getAuthor().getIdLong()).queue();
                break;
            case "channel-id":
                message.getChannel().sendMessage("This channel's ID is " + message.getChannel().getIdLong()).queue();
                break;
            case "guild-id":
                message.getChannel().sendMessage("Guild ID: " + message.getGuild().getIdLong()).queue();
                break;
            case "help":
                EmbedBuilder builder = new EmbedBuilder().setTitle("CryoBot Help")
                        .addField(".get-levels (name)", "Returns skill levels for (name)", true)
                        .addField(".get-level (skill_id) (name)", "Returns level of (skill_id) for (name)", true)
                        .addField(".get-equip (name)", "Returns equipment for (name)", true)
                        .addField(".get-xp (name)", "Returns xp for (name)", true)
                        .addField(".guess (guess)", "Processes a guess for the currently ongoing discord game", true)
                        .addField(".link", "Begins the process to link your in-game account with your discord account", true)
                        .addField(".unlink", "Unlinks your in-game account from your discord account", true);
                int rights = AccountConnection.getRights(message.getAuthor().getIdLong());
                if(rights > 0) {
                    builder = builder.addField(".purge (num)", "Purges (num) messages from channel. .purge all will purge all messages older than 2 weeks", true)
                            .addField(".guild-id", "Responds with the id of the guild you are currently in.", true)
                            .addField(".my-id", "Responds with your discord id.", true)
                            .addField(".channel-id", "Responds with the id of the channel you are currently in", true);
                }
                if(rights > 1) {
                    builder = builder.addField(".link-chat (owner)", "Links the in-game friends chat belonging to (owner) with the channel you are currently in.", true)
                            .addField(".unlink-chat", "Unlinks the friends chats associated with the channel you are currently in.", true)
                            .addField(".finish-link (random) (in-game name)", "Developer command to finish the process linking (in-game name) with the discord account that did .link and got (random)", true)
                            .addField(".setup", "Begins the dialogue to change core settings for the bot", true)
                            .addField(".setup-item-guess", "Begins the dialogue to setup the item guessing game", true)
                            .addField(".setup-place-guess", "Begins the dialogue to setup the place guessing game", true)
                            .addField(".setup-trivia", "Begins the dialogue to setup the trivia game", true)
                            .addField(".add-news-channel", "Adds the current channel into the list of channels messaged with news", true)
                            .addField(".remove-news-channel", "Removes the current channel from the list of channels messaged with news", true)
                            .addField(".recheck-roles", "Forces a recheck of everyone's discords roles", true)
                            .addField(".list-roles", "Lists the roles currently managed by the bot", true)
                            .addField(".set-points (points)", "Sets your current # of points to (points)", true)
                            .addField(".set-total-points (points)", "Sets your total # of points to (points)", true)
                            .addField(".list-games", "Lists all the games currently managed by the bot.", true)
                            .addField(".start-game (name)", "Manually starts a new game of (name), use .list-games for game names", true);
                }
                message.getAuthor()
                        .openPrivateChannel()
                        .complete()
                        .sendMessage(builder.build()).queue();
                message.getChannel().sendMessage("Help is on the way, "+message.getAuthor().getAsMention()+"!").queue();
                message.delete().queue();
                break;
        }
    }
}
