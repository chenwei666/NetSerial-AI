package com.chenwei666.netserial.settings;

public enum AppearanceMode {
    SYSTEM,
    LIGHT,
    DARK;

    public static AppearanceMode fromStoredValue(String value) {
        if (value == null) return SYSTEM;
        try {
            return valueOf(value);
        } catch (IllegalArgumentException exception) {
            return SYSTEM;
        }
    }
}
