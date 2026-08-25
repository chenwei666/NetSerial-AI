package com.chenwei666.netserial.terminal;

import java.util.regex.Pattern;

/** Removes ANSI/VT escape sequences before text is sent to AI, logs, or search. */
public final class AnsiTextSanitizer {
    private static final Pattern ANSI = Pattern.compile(
            "(?:\\u001B\\][^\\u0007]*(?:\\u0007|\\u001B\\\\))|(?:\\u001B(?:\\[[0-?]*[ -/]*[@-~]|[@-_]))"
    );
    private static final Pattern CONTROLS = Pattern.compile("[\\x00-\\x08\\x0B\\x0C\\x0E-\\x1F\\x7F]");

    public String sanitize(String value) {
        if (value == null || value.isEmpty()) {
            return "";
        }
        return CONTROLS.matcher(ANSI.matcher(value).replaceAll("")).replaceAll("");
    }
}
