package com.cryobot.tasks;

import com.cryobot.DiscordBot;
import com.cryobot.entities.Game;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;

import java.util.TimerTask;

public class StatusTask extends TimerTask {

    @Override
    public void run() {
        String status = DiscordBot.getInstance().getHelper().getStatus();
        DiscordBot.getInstance().getJda().getPresence().setPresence(OnlineStatus.ONLINE, Activity.of(Activity.ActivityType.DEFAULT, status));
    }
}
