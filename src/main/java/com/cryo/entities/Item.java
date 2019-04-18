package com.cryo.entities;

import lombok.Data;

import java.sql.Timestamp;

@Data
public class Item {

    private final int id;
    private final String itemName;
    private final String itemPicUrl;
    private final String hint;
    private final Timestamp added;

    public Object[] data() {
        return new Object[]{"DEFAULT", itemName, itemPicUrl, hint, "DEFAULT"};
    }

}
