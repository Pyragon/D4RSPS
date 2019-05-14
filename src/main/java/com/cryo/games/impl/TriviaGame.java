package com.cryo.games.impl;

import com.cryo.DiscordBot;
import com.cryo.db.impl.GamesConnection;
import com.cryo.db.impl.MiscConnection;
import com.cryo.entities.Game;
import com.cryo.entities.Trivia;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageEmbed;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TriviaGame extends Game {

    private static ArrayList<Trivia> questions;

    private Trivia currentQuestion;

    private long messageId;

    //TODO - update to randomize answer positions shown in embed message
    //TODO - i.e. 1. a 2. b 3. c 4. d don't necessarily need to go in that order as long as 1 is defined as the correct answer

    static {
        Object[] data = GamesConnection.connection().handleRequest("get-trivia");
        if (data == null) questions = new ArrayList<>();
        else questions = (ArrayList<Trivia>) data[0];
    }

    @Override
    public String getName() {
        return "trivia";
    }

    @Override
    public void processGuessCommand(Message message, String command, String[] cmd) {
        try {
            int choice = Integer.parseInt(cmd[1]);
            if (currentQuestion.getCorrect() + 1 == choice) {
                GamesConnection.addPoints(message.getAuthor().getIdLong(), 2);
                Object[] data = GamesConnection.connection().handleRequest("get-points", message.getAuthor().getIdLong());
                int points = 0;
                if (data != null)
                    points = (int) data[0];
                sendMessage("Correct " + message.getAuthor().getAsMention() + "! You have been awarded 2 points! You now have " + points + " points.");
                end(message.getAuthor().getName());
                DiscordBot.getInstance().getGameManager().endGame();
            } else sendMessage("Incorrect " + message.getAuthor().getAsMention() + "! Please try again.");
        } catch (Exception e) {
            sendMessage("Incorrect " + message.getAuthor().getAsMention() + "! Please try again.");
        }
    }

    @Override
    public void end(String... params) {
        if (messageId != 0L) {
            long channelId = MiscConnection.getLong("trivia-game-channel");
            if (channelId == 0L) return;
            long guildId = MiscConnection.getLong("guild-id");
            Message message = DiscordBot.getInstance()
                    .getJda()
                    .getGuildById(guildId)
                    .getTextChannelById(channelId)
                    .getMessageById(messageId)
                    .complete();
            if (message == null) return;
            String winner = params.length > 0 ? params[0] : null;
            message.editMessage(buildWinningMessage(winner)).queue();
        }
    }

    public long sendMessage(MessageEmbed message) {
        long channelId = MiscConnection.getLong("trivia-game-channel");
        if (channelId == 0L) return 0L;
        Message m = DiscordBot.getInstance().getJda().getTextChannelById(channelId).sendMessage(message).complete();
        if (m == null) return 0L;
        return m.getIdLong();
    }

    public long sendMessage(String text) {
        long channelId = MiscConnection.getLong("trivia-game-channel");
        if (channelId == 0L) return 0L;
        Message message = DiscordBot.getInstance().getJda().getTextChannelById(channelId).sendMessage(text).complete();
        if (message == null) return 0L;
        return message.getIdLong();
    }

    @Override
    public boolean startGame() {
        long channelId = MiscConnection.getLong("trivia-game-channel");
        if (channelId == 0L) return false;
        if (questions.size() == 0) return false;
        ArrayList<Trivia> questions = (ArrayList<Trivia>) this.questions.clone();
        Collections.shuffle(questions);
        Trivia question = questions.get(0);
        if (question == null) return false;
        messageId = sendMessage(buildTriviaQuestion(question));
        currentQuestion = question;
        return true;
    }

    @Override
    public int getPoints() {
        return 2;
    }

    public static void addTrivia(Trivia trivia) {
        questions.add(trivia);
    }

    public static boolean hasPage(int page) {
        int start = (page - 1) * 10;
        return questions.size() >= start;
    }

    public static MessageEmbed buildTriviaList(int page) {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle("Trivia Questions");
        builder.setThumbnail("https://static.thenounproject.com/png/21610-200.png");
        builder.setDescription("Here is a list of all trivia questions. Respond with 'next' and 'prev' to change pages.");
        int end = ((page - 1) * 10) + 9;
        if (end > questions.size()) end = questions.size();
        List<Trivia> questions = TriviaGame.questions.subList((page - 1) * 10, end);
        questions.forEach(q -> {
            String value = "";
            value += "1. " + q.getAnswers()[0] + " 2. " + q.getAnswers()[1] + "\n3. " + q.getAnswers()[2] + " 4. " + q.getAnswers()[3];
            builder.addField(q.getQuestion(), value, false);
        });
        return builder.build();
    }

    public static MessageEmbed buildTriviaQuestion(Trivia trivia) {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle("Trivia!");
        builder.setDescription("Trivia game. Guess your answer to the following question using the .guess (answer) command");
        String value = "";
        value += "1. " + trivia.getAnswers()[0] + "\u0009\u00092. " + trivia.getAnswers()[1] + "\n3. " + trivia.getAnswers()[2] + "\u0009\u00094. " + trivia.getAnswers()[3];
        builder.addField(trivia.getQuestion(), value, false);
        builder.setThumbnail("https://static.thenounproject.com/png/21610-200.png");
        return builder.build();
    }

    public MessageEmbed buildWinningMessage(String winner) {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle("Trivia!");
        builder.setThumbnail("https://static.thenounproject.com/png/21610-200.png");
        builder.setDescription("Trivia game. Guess your answer to the following question using the .guess (answer) command");
        builder.addField("Game Over!", winner == null ? "Game ended. No one answered correctly in time." : "Correct answer was guessed by: " + winner, false);
        if (winner != null)
            builder.addField(currentQuestion.getQuestion(), "Correct answer was: " + currentQuestion.getAnswers()[currentQuestion.getCorrect()], false);
        return builder.build();
    }
}
