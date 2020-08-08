package com.cryobot.commands.impl;

import com.cryobot.DiscordBot;
import com.cryobot.Links;
import com.cryobot.db.impl.GamesConnection;
import com.cryobot.entities.Command;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.entities.Message;

import java.util.HashMap;

public class DeveloperCommands implements Command {

    @Override
    public int getPermissionsReq(String command) {
        return 2;
    }

    @Override
    public String[] getAliases() {
        return new String[]{"add-news-channel", "remove-news-channel", "recheck-roles", "list-roles", "set-points", "set-total-points", "start-game", "list-games", "default"};
    }

    @Override
    public void handleCommand(Message message, String command, String[] cmd) {
        switch (cmd[0].toLowerCase()) {
            case "default":
                break;
            case "list-games":
                EmbedBuilder builder = new EmbedBuilder().setTitle("Currently managed games");
                HashMap<String, com.cryobot.entities.Game> games = DiscordBot.getInstance().getGameManager().getGames();
                for(String name : games.keySet())
                    builder.addField(name, null, false);
                message.delete().queue();
                message.getAuthor().openPrivateChannel().complete().sendMessage(builder.build()).queue();
                break;
            case "start-game":
                DiscordBot.getInstance().getGameManager().startNewGame(command.substring(11));
                break;
            case "set-points":
            case "set-total-points":
                int points;
                try {
                    points = Integer.parseInt(message.getContentRaw().substring(message.getContentRaw().indexOf(" ") + 1));
                } catch (Exception e) {
                    message.delete().queue();
                    message.getChannel().sendMessage("Unable to parse # of points. Usage: .set-points/.set-total-points #");
                    break;
                }
                GamesConnection.setPoints(message.getAuthor().getIdLong(), points, cmd[0].toLowerCase().contains("total"));
                message.delete().queue();
                message.getChannel().sendMessage("Points set to: " + points + ".").queue();
                break;
            case "add-news-channel":
                long channelId = message.getChannel().getIdLong();
                DiscordBot.getInstance().addNewsChannel(channelId);
                break;
            case "remove-news-channel":
                channelId = message.getChannel().getIdLong();
                DiscordBot.getInstance().removeNewsChannel(channelId);
                break;
            case "recheck-roles":
                message.delete().queue();
                message.getChannel().sendMessage("Rechecking all roles...").queue();
                DiscordBot.getInstance().getRoleManager().recheckAllRoles();
                break;
            case "list-roles":
                message.delete().queue();
                message.getChannel().sendMessage(DiscordBot.getInstance().getRoleManager().getRolesEmbed()).queue();
                break;
            case "change-status":
                String status = message.getContentRaw().substring(message.getContentRaw().indexOf(" ")+1);
                message.delete().queue();
                DiscordBot.getInstance().getJda().getPresence().setGame(Game.playing(status));
                break;
        }
    }
}
