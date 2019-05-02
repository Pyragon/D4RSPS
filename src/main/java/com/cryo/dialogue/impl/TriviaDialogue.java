package com.cryo.dialogue.impl;

import com.cryo.DiscordBot;
import com.cryo.db.impl.GamesConnection;
import com.cryo.db.impl.MiscConnection;
import com.cryo.entities.Dialogue;
import com.cryo.entities.Trivia;
import com.cryo.games.impl.TriviaGame;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.PrivateChannel;

public class TriviaDialogue extends Dialogue {

    private int page = 1;
    private long messageId;

    private int answerStage;

    private String question;
    private String[] answers;
    private int correct;

    public TriviaDialogue(long id) {
        super(id);
        answers = new String[4];
    }

    @Override
    public String getName() {
        return "setup-trivia";
    }

    @Override
    public void start(String[] parameters) {
        sendMessage("Hello, and welcome to the trivia setup dialogue. Let's get started.");
        run(null, null);
    }

    @Override
    public void run(PrivateChannel channel, Message message, String response, String[] res) {
        if (response != null && response.equalsIgnoreCase("end")) {
            sendMessage("Goodbye...");
            end();
            return;
        }
        if (stage == 0) {
            long channelId = MiscConnection.getLong("trivia-game-channel");
            if (channelId == 0L)
                sendMessage("First things first, we need to setup which channel to play this game in. Please enter a channel id. This can be found by using the command .channel-id in the channel of your choice.");
            else sendMessage("Would you like to change the channel this game is played in? Currently: " + channelId);
        } else if (stage == 1) {
            long channelId;
            try {
                channelId = Long.parseLong(response);
                if (DiscordBot.getInstance().getJda().getTextChannelById(channelId) == null) {
                    sendMessage("Unable to verify text channel. Please try again.");
                    return;
                }
                MiscConnection.setLong("trivia-game-channel", channelId);
                sendMessage("Channel has been set. Moving on...");
                stage = 2;
                run(channel, message);
                return;
            } catch (Exception e) {
                response = response.toLowerCase();
                if (response.equals("y") || response.equals("yes")) {
                    sendMessage("Okay. Please enter a new channel id. You can get this by using the command .channel-id in the channel of your choice.");
                    return;
                } else if (response.equals("n") || response.equals("no")) {
                    sendMessage("Alright. Moving on...");
                    stage = 2;
                    run(channel, message);
                    return;
                } else {
                    sendMessage("Unable to process response. Please try again.");
                    sendMessage("To end this dialogue, respond with 'end'.");
                    return;
                }
            }
        } else if (stage == 2) {
            if (response != null && (response.equalsIgnoreCase("next") || response.equalsIgnoreCase("prev")) && messageId != 0L) {
                int newPage = response.equalsIgnoreCase("next") ? 1 : -1;
                message.delete().queue();
                if (TriviaGame.hasPage(page + newPage)) return;
                page += newPage;
                Message m = channel.getMessageById(messageId).complete();
                if (m == null) return;
                m.editMessage(TriviaGame.buildTriviaList(page)).queue();
                return;
            }
            if (response != null && response.equalsIgnoreCase("add")) {
                sendMessage("Okay. Let's add some questions.");
                stage = 3;
                run(channel, message);
                return;
            }
            sendMessage("Here is a list of all the added trivia. Respond with 'next' or 'prev' to change pages. Respond with 'add' to add a new question.");
            messageId = sendMessage(TriviaGame.buildTriviaList(page));
            return;
        } else if (stage == 3)
            sendMessage("Enter the question you would like to ask. Including punctuation.");
        else if (stage == 4) {
            question = response;
            sendMessage("Question will look like this: " + question);
            sendMessage("Is this okay?");
        } else if (stage == 5) {
            response = response.toLowerCase();
            if (response.equals("y") || response.equals("yes"))
                sendMessage("Okay. Let's start adding some answers. Please enter the first answer.");
            else if (response.equals("n") || response.equals("no")) {
                sendMessage("Alright. Let's try again. Enter the question you would like to ask. Including punctuation.");
                stage = 4;
                return;
            } else {
                sendMessage("Unable to process response. Please try again.");
                sendMessage("To end this dialogue, respond with 'end'.");
                return;
            }
        } else if (stage == 6) {
            answers[answerStage++] = response;
            if (answerStage == 4) {
                stage = 7;
                answerStage = 0;
                run(channel, message);
                return;
            }
            sendMessage("Saved. Please enter the next answer.");
            return;
        } else if (stage == 7)
            sendMessage("That's all the answers. Now enter the index for the correct answer. 0 for the first answer, 3 for the last.");
        else if (stage == 8) {
            try {
                correct = Integer.parseInt(response);
            } catch (Exception e) {
                sendMessage("Error parsing correct answer index. Please try again.");
                return;
            }
            if (correct < 0 || correct > 3) {
                sendMessage("Correct answer index must be between 0 and 3. Please try again.");
                return;
            }
            sendMessage("The correct answer will be " + answers[correct] + ". Is this right?");
        } else if (stage == 9) {
            response = response.toLowerCase();
            if (response.equals("y") || response.equals("yes")) {
                sendMessage("Alrighty. All done for this one. This is what it'll look like. Is this okay?");
                Trivia trivia = new Trivia(-1, question, answers, correct, null, null);
                sendMessage(TriviaGame.buildTriviaQuestion(trivia));
            } else if (response.equals("n") || response.equals("no")) {
                sendMessage("Alright. Let's try again. Enter the index for the correct answer. 0 being the first answer, 3 for the last.");
                stage = 8;
                return;
            } else {
                sendMessage("Unable to process response. Please try again.");
                sendMessage("To end this dialogue, respond with 'end'.");
                return;
            }
        } else if (stage == 10) {
            response = response.toLowerCase();
            if (response.equals("y") || response.equals("yes")) {
                Trivia trivia = new Trivia(-1, question, answers, correct, null, null);
                GamesConnection.connection().handleRequest("add-trivia", trivia);
                TriviaGame.addTrivia(trivia);
                sendMessage("Awesome. I've added that trivia question for you. Would you like to add another?");
            } else if (response.equals("n") || response.equals("no")) {
                sendMessage("Alright. We'll have to try again. ");
                stage = 3;
                run(channel, message);
                return;
            } else {
                sendMessage("Unable to process response. Please try again.");
                sendMessage("To end this dialogue, respond with 'end'.");
                return;
            }
        } else if (stage == 11) {
            response = response.toLowerCase();
            if (response.equals("y") || response.equals("yes")) {
                stage = 3;
                run(channel, message);
                return;
            } else if (response.equals("n") || response.equals("no")) {
                sendMessage("That's the end. Goodbye.");
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
