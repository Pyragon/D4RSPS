package com.cryo.commands.impl;

import com.cryo.Links;
import com.cryo.db.impl.FriendsChatConnection;
import com.cryo.entities.Command;
import com.mysql.jdbc.StringUtils;
import net.dv8tion.jda.core.entities.Message;

public class LinkFriendsChatCommand implements Command {

    @Override
    public int getPermissionsReq(String command) {
        return 2;
    }

    @Override
    public String[] getAliases() {
        return new String[]{"link-chat", "unlink-chat"};
    }

    @Override
    public void handleCommand(Message message, String command, String[] cmd) {
        if (cmd[0].equals("unlink-chat")) {
            long channelId = message.getChannel().getIdLong();
            Object[] data = FriendsChatConnection.connection().handleRequest("unlink", channelId);
            if (data == null)
                message.getChannel().sendMessage("This channel is not linked with any in-game friends chat.").queue();
            else {
                String chatName = (String) data[0];
                message.getChannel().sendMessage("This channel is now unlinked from the in-game friends chat " + chatName).queue();
            }
            return;
        }
        if (cmd.length == 1) {
            message.getChannel().sendMessage("Invalid syntax. Correct usage: .link-chat (friends_chat)").queue();
            return;
        }
        String name = command.substring(10);
        System.out.println(name);
        if (StringUtils.isEmptyOrWhitespaceOnly(name)) {
            message.getChannel().sendMessage("Invalid syntax. Correct usage: .link-chat (friends_chat)").queue();
            return;
        }
        Object obj = Links.linkFriendsChat(name, message.getChannel().getIdLong());
        if (obj instanceof Boolean) {
            if (!(boolean) obj) {
                message.getChannel().sendMessage("Error occurred while linking. Please try again later.").queue();
                return;
            }
            message.getChannel().sendMessage("Successfully linked this channel with the in-game friends chat: " + name).queue();
            return;
        }

        String response = (String) obj;
        message.getChannel().sendMessage(response).queue();
    }
}
