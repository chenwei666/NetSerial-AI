package com.chenwei666.netserial.ai;

import java.util.regex.Pattern;

final class TerminalContextSanitizer {
    private static final int MAX_INTENT_CHARACTERS = 2_000;
    private static final int MAX_TERMINAL_CHARACTERS = 12_000;
    private static final Pattern SENSITIVE_LINE = Pattern.compile(
            ".*(api[ _-]?key|password|passwd|secret|token|community|"
                    + "private[ _-]?key|shared[ _-]?key).*",
            Pattern.CASE_INSENSITIVE
    );

    String sanitizeIntent(String intent) {
        return sanitize(intent, MAX_INTENT_CHARACTERS);
    }

    String sanitizeTerminalOutput(String terminalOutput) {
        return sanitize(terminalOutput, MAX_TERMINAL_CHARACTERS);
    }

    private static String sanitize(String value, int maximumCharacters) {
        String[] lines = value.replace('\r', '\n').split("\\n", -1);
        StringBuilder sanitized = new StringBuilder();
        for (String line : lines) {
            if (sanitized.length() > 0) {
                sanitized.append('\n');
            }
            sanitized.append(SENSITIVE_LINE.matcher(line).matches() ? "[REDACTED]" : line);
        }
        if (sanitized.length() <= maximumCharacters) {
            return sanitized.toString();
        }
        return "[TRUNCATED]\n" + sanitized.substring(sanitized.length() - maximumCharacters);
    }
}
