package com.chenwei666.netserial.settings;

import java.util.Locale;

public enum AppLanguage {
    SYSTEM(""),
    SIMPLIFIED_CHINESE("zh-CN"),
    ENGLISH("en");

    private final String languageTag;
    AppLanguage(String languageTag) { this.languageTag = languageTag; }
    public String getLanguageTag() { return languageTag; }

    public static AppLanguage fromTag(String tag) {
        if (tag == null || tag.trim().isEmpty()) return SYSTEM;
        String normalized = tag.toLowerCase(Locale.ROOT);
        if (normalized.startsWith("zh")) return SIMPLIFIED_CHINESE;
        if (normalized.startsWith("en")) return ENGLISH;
        return SYSTEM;
    }
}
