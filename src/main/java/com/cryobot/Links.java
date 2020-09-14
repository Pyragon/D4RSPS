package com.cryobot;

import com.cryobot.db.impl.AccountConnection;
import com.cryobot.db.impl.FriendsChatConnection;
import com.cryobot.db.impl.GamesConnection;
import com.cryobot.dialogue.impl.GuessThatPlaceDialogue;
import com.cryobot.entities.Dialogue;
import com.cryobot.entities.Game;
import com.cryobot.games.impl.GuessThatPlaceGame;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.PrivateChannel;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.requests.RestAction;

import java.util.ArrayList;

public class Links {

    public static void sendWorldNews(String news) {
        ArrayList<Long> channelIds = DiscordBot.getInstance().getWorldNewsChannels();
        channelIds.forEach(id -> DiscordBot.getInstance().getJda().getTextChannelById(id).sendMessage("[World News]" + news).queue());
    }

    public static void sendMessage(long channelId, String message) {
        TextChannel channel = DiscordBot.getInstance().getJda().getTextChannelById(channelId);
        if(channel == null) return;
        channel.sendMessage(message).queue();
    }

    public static void sendPrivateMessage(long id, String message) {
        PrivateChannel channel = DiscordBot.getInstance().getJda().getUserById(id).openPrivateChannel().complete();
        if(channel == null) return;
        channel.sendMessage(message).queue();
    }

    public static long getDiscordId(String username) {
        Object[] data = AccountConnection.connection().handleRequest("get-discord-id", username);
        if(data == null) return -1;
        return (long) data[0];
    }

    public static String getUsername(long discordId) {
        Object[] data = AccountConnection.connection().handleRequest("get-username", discordId);
        if(data == null) return null;
        return (String) data[0];
    }

    public static int getPoints(String username) {
        Object[] data = AccountConnection.connection().handleRequest("get-discord-id", username);
        return data == null ? 0 : GamesConnection.getPoints((long) data[0]);
    }

    public static void recheckAllRoles() {
        DiscordBot.getInstance().getRoleManager().recheckAllRoles();
    }

    public static Object linkFriendsChat(String owner, long discordId) {
        Object[] data = FriendsChatConnection.connection().handleRequest("get-friends-chat", discordId);
        Object[] data2 = FriendsChatConnection.connection().handleRequest("get-discord-channel", owner);
        if (data != null) return "This discord is already linked with an in-game friends chat.";
        if (data2 != null) return "That friends chat is already linked with a discord channel";
        data = FriendsChatConnection.connection().handleRequest("link-friends-chat", owner, discordId);
        if (data == null) return false;
        return true;
    }

    //Returns false if there was an issue checking coordinates
    //i.e. No guess game started and not using setup dialogue, account not linked, etc
    public static String handleInGamePlaceCommand(String username, int x, int y, int plane) {
        Object[] data = AccountConnection.connection().handleRequest("get-discord-id", username);
        if (data == null) return "Your in-game account needs to be linked to a Discord account to play this game.";
        long id = (long) data[0];
        Dialogue dialogue = DiscordBot.getInstance().getDialogueManager().getConversation(id);
        if (dialogue != null && (dialogue instanceof GuessThatPlaceDialogue)) {
            ((GuessThatPlaceDialogue) dialogue).enteredCoordinates(x, y, plane);
            return "Coordinates saved. Check the dialogue for further instructions.";
        }
        Game game = DiscordBot.getInstance().getGameManager().getCurrentGame();
        if (game == null || !(game instanceof GuessThatPlaceGame)) return "Guess That Place game not active!";
        return GuessThatPlaceGame.handleFindCommand(username, id, x, y, plane);
    }

    public static boolean linkDiscordAccount(String username, String randomString) {
        Object[] data = AccountConnection.connection().handleRequest("verify", username, randomString);
        boolean linked = data != null;
        if (!linked) return false;
        long discordId = (long) data[0];
        DiscordBot.getInstance().getRoleManager().recheckRoles(DiscordBot.getInstance().getJda().getUserById(discordId));
        return true;
    }

    public static String beginDiscordLinkFromServer(String username) {
        if(getDiscordId(username) != -1) return null;
        Object[] data = AccountConnection.connection().handleRequest("add-server-verification", username);
        if(data == null) return null;
        return (String) data[0];
    }

    public static boolean linkDiscordAccountFromServer(long discordId, String randomString) {
        Object[] data = AccountConnection.connection().handleRequest("verify-server", discordId, randomString);
        boolean linked = data != null;
        if (!linked) return false;
        DiscordBot.getInstance().getRoleManager().recheckRoles(DiscordBot.getInstance().getJda().getUserById(discordId));
        return true;
    }

    public static void handleServerMessage(String owner, String displayName, String message) {
        Object[] data = FriendsChatConnection.connection().handleRequest("get-discord-channel", owner);
        if (data == null) return;
        long discordId = (long) data[0];
        DiscordBot.sendMessage(discordId, "**" + displayName + "**: " + message);
    }

    public static void handleDiscordMessage(Message message) {
        if (message.getAuthor().getIdLong() == DiscordBot.getInstance().getJda().getSelfUser().getIdLong()) return;
        Object[] data = FriendsChatConnection.connection().handleRequest("get-friends-chat", message.getChannel().getIdLong());
        if (data == null) return;
        String owner = (String) data[0];
        data = AccountConnection.connection().handleRequest("get-username", message.getAuthor().getIdLong());
        if (data == null) {
            message.delete().queue();
            RestAction<PrivateChannel> action = message.getAuthor().openPrivateChannel();
            action.queue(privateChannel -> privateChannel.sendMessage("You must have your in-game account linked in order to type in a friends chat linked channel.").queue());
            return;
        }
        String username = (String) data[0];
        message.delete().queue();
        String displayName = DiscordBot.getInstance().getHelper().getDisplayName(username);
        message.getChannel().sendMessage("**" + displayName + "**: " + message.getContentRaw()).queue();
        DiscordBot.getInstance().getHelper().sendFriendsChatMessage(owner, username, message.getContentRaw());
    }
}
