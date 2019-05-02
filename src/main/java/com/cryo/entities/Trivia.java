package com.cryo.entities;

import com.cryo.DiscordBot;
import lombok.Data;

import java.sql.Timestamp;

@Data
public class Trivia {

    private final int id;
    private final String question;
    private final String[] answers;
    private final int correct;
    private final Timestamp added;
    private final Timestamp updated;

    public Object[] data() {
        return new Object[]{"DEFAULT", question, DiscordBot.getInstance().getGson().toJson(answers), correct, "DEFAULT", "DEFAULT"};
    }
}
