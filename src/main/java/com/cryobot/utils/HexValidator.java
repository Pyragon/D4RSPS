package com.cryobot.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HexValidator {

    private Pattern pattern;
    private Matcher matcher;

    private static final String HEX_PATTERN = "^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$";

    public HexValidator() {
        pattern = Pattern.compile(HEX_PATTERN);
    }

    /**
     * Validate hex with regular expression
     *
     * @param hex hex for validation
     * @return true valid hex, false invalid hex
     */
    public boolean validate(final String hex) {

        matcher = pattern.matcher(hex);
        return matcher.matches();

    }
}
