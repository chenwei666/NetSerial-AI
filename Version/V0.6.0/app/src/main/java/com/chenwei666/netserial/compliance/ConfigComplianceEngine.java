package com.chenwei666.netserial.compliance;

import com.chenwei666.netserial.device.Vendor;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/** Offline heuristic checks. Findings are evidence prompts, not proof of a violation. */
public final class ConfigComplianceEngine {
    public ComplianceReport analyze(Vendor vendor, String configuration) {
        Objects.requireNonNull(vendor, "vendor");
        String value = Objects.requireNonNull(configuration, "configuration");
        if (value.length() > 500_000) throw new IllegalArgumentException("configuration too large");
        String lower = value.toLowerCase(Locale.ROOT);
        List<ComplianceFinding> findings = new ArrayList<>();
        if (enabled(lower, "telnet server enable", "telnet-server enable", "enable service telnet-server")) {
            add(findings, "TELNET_ENABLED", ComplianceSeverity.HIGH,
                    "Telnet appears enabled.", "Prefer SSH and disable Telnet after confirming dependencies.");
        }
        boolean http = enabled(lower, "ip http enable", "http server enable", "ip http server",
                "enable service web-server http");
        boolean https = enabled(lower, "ip https enable", "http secure-server enable",
                "ip http secure-server", "enable service web-server https");
        if (http && !https) add(findings, "HTTP_WITHOUT_HTTPS", ComplianceSeverity.HIGH,
                "Plain HTTP appears enabled without HTTPS evidence.", "Enable HTTPS and restrict or disable HTTP.");
        if (lower.contains("snmp-server community public") || lower.contains("snmp-server community private")
                || lower.contains("snmp-agent community read public")) {
            add(findings, "DEFAULT_SNMP_COMMUNITY", ComplianceSeverity.HIGH,
                    "A default SNMP community name appears present.", "Migrate to SNMPv3 and rotate the community.");
        }
        if (lower.matches("(?s).*password\\s+(simple|0)\\s+\\S+.*")) {
            add(findings, "PLAIN_PASSWORD_SYNTAX", ComplianceSeverity.HIGH,
                    "Plain-password configuration syntax appears present.", "Use the vendor's irreversible/secret form.");
        }
        if (!lower.contains("ntp") && !lower.contains("clock source")) {
            add(findings, "TIME_SYNC_NOT_FOUND", ComplianceSeverity.WARNING,
                    "No time synchronization configuration was found.", "Verify NTP and timezone configuration.");
        }
        if (!lower.contains("ssh") && !lower.contains("stelnet")) {
            add(findings, "SSH_NOT_FOUND", ComplianceSeverity.WARNING,
                    "No SSH management configuration was found.", "Verify secure remote management is enabled.");
        }
        if (findings.isEmpty()) add(findings, "NO_HEURISTIC_FINDINGS", ComplianceSeverity.INFO,
                "No configured heuristic triggered.", "Perform model-specific review before production use.");
        return new ComplianceReport(findings);
    }

    private static boolean enabled(String value, String... markers) {
        for (String marker : markers) {
            if (value.contains(marker) && !value.contains("undo " + marker)
                    && !value.contains("no " + marker)) return true;
        }
        return false;
    }

    private static void add(List<ComplianceFinding> findings, String id, ComplianceSeverity severity,
                            String message, String recommendation) {
        findings.add(new ComplianceFinding(id, severity, message, recommendation));
    }
}
