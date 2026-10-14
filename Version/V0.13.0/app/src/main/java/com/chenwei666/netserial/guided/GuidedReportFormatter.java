package com.chenwei666.netserial.guided;

import com.chenwei666.netserial.terminal.SensitiveTextRedactor;
import com.chenwei666.netserial.terminal.AnsiTextSanitizer;
import java.util.List;
import java.util.regex.Pattern;

/** A bounded, plain-text handoff. Redaction reduces risk; it is not a secrecy guarantee. */
public final class GuidedReportFormatter {
    public static final int MAX_EVIDENCE = 32_000;
    private static final Pattern CREDENTIAL_LINE = Pattern.compile(
            "(?im)^\\s*(?:[^\\s\\r\\n]{1,120}[#>]\\s*|\\[[^\\]\\r\\n]{1,120}\\]\\s*)?"
                    + "(?:password(?!\\s+authentication\\s+failed\\b)|passwd|secret|enable\\s+(?:password|secret)"
                    + "|(?:username|local-user)\\s+\\S+\\s+[^\\r\\n]*(?:password|secret)"
                    + "|snmp-agent\\s+(?:community|usm-user)|snmp-server\\s+(?:community|user)"
                    + "|(?:radius-server|tacacs-server)\\s+key|key\\s+authentication)\\b[^\\r\\n]*$");
    private static final Pattern QUOTED_ASSIGNMENT = Pattern.compile(
            "(?i)([\"']?(?:api[_ -]?key|token|password|passwd|secret|community)[\"']?\\s*[:=]\\s*)"
                    + "(?:\"[^\"\\r\\n]*\"|'[^'\\r\\n]*'|[^\\s,;]+)");
    private static final Pattern KEY_BLOCK = Pattern.compile(
            "(?s)-----BEGIN [^-]*PRIVATE KEY-----.*?(?:-----END [^-]*PRIVATE KEY-----|\\z)");

    public String sanitize(String text) {
        String safe = clean(text);
        return safe.substring(0, Math.min(safe.length(), MAX_EVIDENCE));
    }

    /** Read the latest complete lines of an already bounded active-session buffer. */
    public String captureTail(String text) {
        String safe = clean(text);
        if (safe.length() <= MAX_EVIDENCE) return safe;
        int start = safe.length() - MAX_EVIDENCE;
        int newline = safe.indexOf('\n', start);
        return safe.substring(newline < 0 ? start : newline + 1);
    }

    private String clean(String text) {
        if (text == null) return "";
        // Bound before regex work; incomplete private key blocks are also removed.
        String bounded = text.substring(0, Math.min(text.length(), 128_000));
        String safe = new AnsiTextSanitizer().sanitize(bounded);
        safe = KEY_BLOCK.matcher(safe).replaceAll("[REDACTED_PRIVATE_KEY]");
        safe = QUOTED_ASSIGNMENT.matcher(safe).replaceAll("$1[REDACTED]");
        safe = new SensitiveTextRedactor().redact(safe);
        safe = CREDENTIAL_LINE.matcher(safe).replaceAll("[REDACTED_CREDENTIAL_LINE]");
        // Drop control characters but preserve line breaks and tabs.
        safe = safe.replaceAll("[\\p{Cntrl}&&[^\\n\\r\\t]]", "");
        return safe;
    }

    public String format(String title, String source, String time,
                         List<DiagnosticFinding> findings, boolean english) {
        StringBuilder out = new StringBuilder(title).append('\n')
                .append(english ? "Source: " : "来源：").append(source).append('\n')
                .append(english ? "Time: " : "时间：").append(time).append("\n\n");
        for (DiagnosticFinding finding : findings) {
            out.append('[').append(status(finding.getStatus(), english)).append("] ")
                    .append(finding.getTitle().get(english)).append('\n')
                    .append(finding.getExplanation().get(english)).append('\n')
                    .append(english ? "Next: " : "下一步：").append(finding.getNextStep().get(english)).append('\n');
            if (!finding.getEvidence().isEmpty()) out.append(finding.getEvidence()).append('\n');
            out.append('\n');
        }
        out.append(english ? "Only the supplied evidence and checks above were assessed. Review for sensitive data before sharing."
                : "仅对上述检查与提供的证据作判断；分享前请再次检查敏感信息。");
        return sanitize(out.toString());
    }

    public static String status(DiagnosticFinding.Status status, boolean english) {
        switch (status) {
            case PASS: return english ? "Check passed" : "本项通过";
            case WARNING: return english ? "Needs attention" : "需要留意";
            case FAIL: return english ? "Issue observed" : "观察到异常";
            default: return english ? "More evidence needed" : "还需确认";
        }
    }
}
