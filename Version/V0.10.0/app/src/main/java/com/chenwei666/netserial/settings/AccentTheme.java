package com.chenwei666.netserial.settings;

public enum AccentTheme {
    OCEAN,
    EMERALD,
    VIOLET,
    SUNSET;

    public static AccentTheme fromStoredValue(String value) {
        if (value == null) return OCEAN;
        try {
            return valueOf(value);
        } catch (IllegalArgumentException exception) {
            return OCEAN;
        }
    }
}
