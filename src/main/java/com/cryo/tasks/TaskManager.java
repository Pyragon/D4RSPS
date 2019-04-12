package com.cryo.tasks;

import java.util.Timer;

public class TaskManager {

    public void start() {
        Timer timer = new Timer();
        timer.schedule(new StatusTask(), 5000, 1000);
    }
}
