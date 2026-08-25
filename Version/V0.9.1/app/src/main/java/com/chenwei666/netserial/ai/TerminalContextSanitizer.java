package com.chenwei666.netserial.ai;

import java.util.regex.Pattern;

final class TerminalContextSanitizer {
    private static final int MAX_INTENT_CHARACTERS = 2_000;
    private static final int MAX_CONVERSATION_CHARACTERS = 8_000;
    private static final int MAX_TERMINAL_CHARACTERS = 12_000;
    private static final Pattern SENSITIVE_LINE = Pattern.compile(
            ".*(api[ _-]?key|password|passwd|secret|token|community|"
                    + "private[ _-]?key|shared[ _-]?key).*",
            Pattern.CASE_INSENSITIVE
    );
    private static final Pattern PROMPT_INJECTION_LINE = Pattern.compile(
            ".*(ignore|disregard|override).*(previous|system|developer|instruction|prompt).*|"
                    + ".*(you are now|act as|assistant:|system:).*",
            Pattern.CASE_INSENSITIVE);

    String sanitizeIntent(String intent) {
        return sanitize(intent, MAX_INTENT_CHARACTERS);
    }

    String sanitizeTerminalOutput(String terminalOutput) {
        return sanitize(terminalOutput, MAX_TERMINAL_CHARACTERS);
    }

    String sanitizeConversationText(String content) {
        return sanitize(content, MAX_CONVERSATION_CHARACTERS);
    }

    private static String sanitize(String value, int maximumCharacters) {
        String[] lines = value.replace('\r', '\n').split("\\n", -1);
        StringBuilder sanitized = new StringBuilder();
        for (String line : lines) {
            if (sanitized.length() > 0) {
                sanitized.append('\n');
            }
            if (SENSITIVE_LINE.matcher(line).matches()) sanitized.append("[REDACTED]");
            else if (PROMPT_INJECTION_LINE.matcher(line).matches()) sanitized.append("[UNTRUSTED INSTRUCTION REMOVED]");
            else sanitized.append(line);
        }
        if (sanitized.length() <= maximumCharacters) {
            return sanitized.toString();
        }
        return "[TRUNCATED]\n" + sanitized.substring(sanitized.length() - maximumCharacters);
    }
}
