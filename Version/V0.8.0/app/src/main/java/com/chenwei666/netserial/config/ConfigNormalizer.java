package com.chenwei666.netserial.config;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public final class ConfigNormalizer {
    private static final Pattern DYNAMIC_LINE = Pattern.compile(
            "^.*(uptime|current time|last change|last updated|generated at|ntp clock-period|"
                    + "packet[s]? input|packet[s]? output|bytes input|bytes output).*$",
            Pattern.CASE_INSENSITIVE);

    public String normalize(String raw) {
        if (raw == null) return "";
        String normalized = raw.replace("\r\n", "\n").replace('\r', '\n');
        List<String> lines = new ArrayList<>();
        for (String source : normalized.split("\n", -1)) {
            String line = trimTrailing(source.replace("\u0000", ""));
            if (DYNAMIC_LINE.matcher(line.trim()).matches()) continue;
            lines.add(line);
        }
        while (!lines.isEmpty() && lines.get(lines.size() - 1).isEmpty()) lines.remove(lines.size() - 1);
        return String.join("\n", lines);
    }

    private static String trimTrailing(String value) {
        int end = value.length();
        while (end > 0 && Character.isWhitespace(value.charAt(end - 1))) end--;
        return value.substring(0, end);
    }
}
