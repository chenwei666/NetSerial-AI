package com.chenwei666.netserial.terminal;

import java.util.regex.Pattern;

/** Conservative local redaction for exported transcripts and AI context. */
public final class SensitiveTextRedactor {
    private static final Pattern ASSIGNMENT = Pattern.compile(
            "(?i)(api[_ -]?key|token|password|passwd|secret|community)\\s*[:=]\\s*([^\\s,;]+)"
    );
    private static final Pattern BEARER = Pattern.compile("(?i)bearer\\s+[A-Za-z0-9._~+/-]{8,}");
    private static final Pattern PRIVATE_KEY = Pattern.compile(
            "(?s)-----BEGIN [^-]*PRIVATE KEY-----.*?-----END [^-]*PRIVATE KEY-----"
    );

    public String redact(String value) {
        if (value == null || value.isEmpty()) {
            return "";
        }
        String result = PRIVATE_KEY.matcher(value).replaceAll("[REDACTED_PRIVATE_KEY]");
        result = BEARER.matcher(result).replaceAll("Bearer [REDACTED]");
        return ASSIGNMENT.matcher(result).replaceAll("$1=[REDACTED]");
    }

    public boolean containsSensitiveMaterial(String value) {
        if (value == null) {
            return false;
        }
        return !redact(value).equals(value);
    }
}
