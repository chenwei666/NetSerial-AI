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

    /** Task protocol contains benign credential words (local password dialogs), not credential values. */
    String sanitizeTaskConversationText(String content) {
        String safe = new com.chenwei666.netserial.terminal.SensitiveTextRedactor().redact(
                new com.chenwei666.netserial.terminal.AnsiTextSanitizer().sanitize(content));
        // Device output still uses conservative whole-line secret filtering. Benign task goals
        // mentioning a local password dialog must not make device credentials permissible.
        java.util.regex.Matcher output = Pattern.compile("(?s)(<untrusted-device-output>)(.*?)(</untrusted-device-output>)").matcher(safe);
        StringBuffer guarded = new StringBuffer();
        while (output.find()) output.appendReplacement(guarded, java.util.regex.Matcher.quoteReplacement(
                output.group(1) + sanitizeTerminalOutput(output.group(2)) + output.group(3)));
        output.appendTail(guarded);
        safe = guarded.toString();
        StringBuilder result = new StringBuilder();
        for (String line : safe.replace('\r', '\n').split("\\n", -1)) {
            if (result.length() > 0) result.append('\n');
            result.append(PROMPT_INJECTION_LINE.matcher(line).matches() ? "[UNTRUSTED INSTRUCTION REMOVED]" : line);
        }
        return result.length() <= MAX_CONVERSATION_CHARACTERS ? result.toString()
                : result.substring(0, MAX_CONVERSATION_CHARACTERS) + "\n[TRUNCATED]";
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
