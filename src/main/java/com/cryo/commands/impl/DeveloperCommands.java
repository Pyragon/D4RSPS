package com.cryo.commands.impl;

import com.cryo.DiscordBot;
import com.cryo.Links;
import com.cryo.db.impl.GamesConnection;
import com.cryo.entities.Command;
import net.dv8tion.jda.core.entities.Message;

public class DeveloperCommands implements Command {

    @Override
    public int getPermissionsReq(String command) {
        return 2;
    }

    @Override
    public String[] getAliases() {
        return new String[]{"set-points", "set-total-points", "start-game", "default"};
    }

    @Override
    public void handleCommand(Message message, String command, String[] cmd) {
        switch (cmd[0].toLowerCase()) {
            case "default":
                int x = 3343;
                int y = 3322;
                String response = Links.handleInGamePlaceCommand("cody", x, y, 0);
                message.delete().queue();
                message.getChannel().sendMessage(response).queue();
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
        }
    }
}
