package com.cryobot.commands;

import com.cryobot.DiscordBot;
import com.cryobot.commands.impl.*;
import com.cryobot.db.impl.AccountConnection;
import com.cryobot.entities.Command;
import com.cryobot.entities.Game;
import com.cryobot.utils.Utilities;
import net.dv8tion.jda.core.entities.Message;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.stream.Stream;

public class CommandManager {

    private HashMap<String, Command> commands;

    private static Class[] COMMANDS = {DeveloperCommands.class, DialogueCommands.class, FinishLinkCommand.class, InGameStatsCommands.class, LinkAccountCommand.class, LinkFriendsChatCommand.class, UtilityCommands.class};

    public void loadCommands() {
        try {
            commands = new HashMap<>();
            for (Class<?> c : COMMANDS) {
                if (!Command.class.isAssignableFrom(c)) continue;
                Object o = c.newInstance();
                if (!(o instanceof Command)) continue;
                Command command = (Command) o;
                Stream.of(command.getAliases()).forEach(c2 -> commands.put(c2, command));
            }
            ArrayList<Command> extra = DiscordBot.getInstance().getHelper().getExtraCommands();
            if(extra == null || extra.size() == 0) return;
            for(Command command : extra) {
                for(String com : command.getAliases()) {
                    if(commands.containsKey(com)) {
                        System.err.println("[CommandManager]Duplicate command exists: "+com);
                        continue;
                    }
                    commands.put(com, command);
                }
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }
    }

    public boolean processCommand(Message message) {
        String prefix = DiscordBot.getInstance().getProperties().getProperty("prefix");
        if (!message.getContentRaw().startsWith(prefix)) return false;
        String command = message.getContentRaw();
        command = command.replace(prefix, "");
        String[] cmd = command.split(" ");
        String opcode = cmd[0];
        if (commands.containsKey(opcode)) {
            Command commandObj = commands.get(opcode);
            int rights = AccountConnection.getRights(message.getAuthor().getIdLong());
            if(message.getGuild().getOwner().getUser().getIdLong() == message.getAuthor().getIdLong()) rights = 2;
            if (commandObj.getPermissionsReq(command) > rights) return false;
            commandObj.handleCommand(message, command, cmd);
            return true;
        }
        message.getChannel().sendMessage("Unable to recognize that command.");
        return false;
    }
}
