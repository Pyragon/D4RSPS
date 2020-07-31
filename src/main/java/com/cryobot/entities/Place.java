package com.cryobot.entities;

import lombok.Data;

import java.sql.Timestamp;

@Data
public class Place {

    private final int id;
    private final int x;
    private final int y;
    private final int plane;
    private final String hint;
    private final String image;
    private final Timestamp added;

    public Object[] data() {
        return new Object[]{"DEFAULT", x, y, plane, hint, image, "DEFAULT"};
    }

}
