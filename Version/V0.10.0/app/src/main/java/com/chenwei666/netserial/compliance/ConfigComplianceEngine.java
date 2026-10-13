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
        if (enabled(lower, "snmp-server community", "snmp-agent community read",
                "snmp-agent community write") && !lower.contains("snmp-agent usm-user")
                && !lower.contains("snmp-server user")) {
            add(findings, "SNMP_LEGACY_ONLY", ComplianceSeverity.WARNING,
                    "SNMP community configuration was found without SNMPv3 user evidence.",
                    "Plan a migration to authenticated and encrypted SNMPv3 before removing legacy access.");
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
        if (containsAny(lower, "ssh version 1", "ip ssh version 1", "stelnet server enable version 1")) {
            add(findings, "SSH_V1_ENABLED", ComplianceSeverity.HIGH,
                    "SSH version 1 appears enabled.", "Require SSH version 2 and verify client compatibility.");
        }
        if (containsAny(lower, "3des", "des-cbc", "hmac-md5", "ssh-rsa", "diffie-hellman-group1")) {
            add(findings, "WEAK_SSH_ALGORITHM", ComplianceSeverity.HIGH,
                    "A legacy SSH algorithm appears configured.",
                    "Confirm platform support, then move to modern ciphers, MACs, host keys, and key exchange.");
        }
        if (!lower.contains("aaa") && !lower.contains("authentication login")) {
            add(findings, "CENTRAL_AAA_NOT_FOUND", ComplianceSeverity.WARNING,
                    "No centralized or explicit AAA policy was found.",
                    "Verify RADIUS/TACACS policy, local emergency access, and authorization accounting.");
        }
        if (!lower.contains("info-center loghost") && !lower.contains("logging host")
                && !lower.contains("syslog")) {
            add(findings, "REMOTE_LOGGING_NOT_FOUND", ComplianceSeverity.WARNING,
                    "No remote logging destination was found.",
                    "Send security and operations events to a protected central collector.");
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

    private static boolean containsAny(String value, String... markers) {
        for (String marker : markers) if (value.contains(marker)) return true;
        return false;
    }

    private static void add(List<ComplianceFinding> findings, String id, ComplianceSeverity severity,
                            String message, String recommendation) {
        findings.add(new ComplianceFinding(id, severity, message, recommendation));
    }
}
