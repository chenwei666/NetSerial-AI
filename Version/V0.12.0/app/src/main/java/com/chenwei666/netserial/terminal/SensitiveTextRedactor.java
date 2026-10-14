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
    private static final Pattern USER_CREDENTIAL = Pattern.compile(
            "(?im)^(\\s*username\\s+\\S+\\s+(?:password|secret)(?:\\s+\\d+)?\\s+)\\S+.*$"
    );
    private static final Pattern ENABLE_CREDENTIAL = Pattern.compile(
            "(?im)^(\\s*enable\\s+(?:password|secret)(?:\\s+\\d+)?\\s+)\\S+.*$"
    );
    private static final Pattern SNMP_COMMUNITY = Pattern.compile(
            "(?im)^(\\s*snmp-server\\s+community\\s+)\\S+(.*)$"
    );
    private static final Pattern SNMP_AGENT_COMMUNITY = Pattern.compile(
            "(?im)^(\\s*snmp-agent\\s+community\\s+(?:read|write)(?:\\s+(?:simple|cipher))?\\s+)\\S+.*$"
    );
    private static final Pattern LOCAL_USER_PASSWORD = Pattern.compile(
            "(?im)^(\\s*password\\s+(?:simple|cipher|hash)\\s+)\\S+.*$"
    );
    private static final Pattern LOCAL_USER_INLINE_PASSWORD = Pattern.compile(
            "(?im)^(\\s*local-user\\s+\\S+\\s+password\\s+"
                    + "(?:(?:simple|cipher|hash|irreversible-cipher)\\s+)?)\\S+.*$"
    );

    public String redact(String value) {
        if (value == null || value.isEmpty()) {
            return "";
        }
        String result = PRIVATE_KEY.matcher(value).replaceAll("[REDACTED_PRIVATE_KEY]");
        result = BEARER.matcher(result).replaceAll("Bearer [REDACTED]");
        result = ASSIGNMENT.matcher(result).replaceAll("$1=[REDACTED]");
        result = USER_CREDENTIAL.matcher(result).replaceAll("$1[REDACTED]");
        result = ENABLE_CREDENTIAL.matcher(result).replaceAll("$1[REDACTED]");
        result = SNMP_COMMUNITY.matcher(result).replaceAll("$1[REDACTED]$2");
        result = SNMP_AGENT_COMMUNITY.matcher(result).replaceAll("$1[REDACTED]");
        result = LOCAL_USER_PASSWORD.matcher(result).replaceAll("$1[REDACTED]");
        return LOCAL_USER_INLINE_PASSWORD.matcher(result).replaceAll("$1[REDACTED]");
    }

    public boolean containsSensitiveMaterial(String value) {
        if (value == null) {
            return false;
        }
        return !redact(value).equals(value);
    }
}
