package com.chenwei666.netserial.guided;

/** A small immutable bilingual text value for offline diagnostic explanations. */
public final class GuideText {
    private final String chinese;
    private final String english;

    public GuideText(String chinese, String english) {
        if (chinese == null || english == null) throw new IllegalArgumentException("Text required");
        this.chinese = chinese;
        this.english = english;
    }

    public String get(boolean english) { return english ? this.english : chinese; }
}
