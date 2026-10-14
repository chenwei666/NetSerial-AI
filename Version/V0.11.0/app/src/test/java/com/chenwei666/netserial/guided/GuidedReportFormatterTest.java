package com.chenwei666.netserial.guided;

import com.chenwei666.netserial.guided.DiagnosticFinding.Status;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;

public class GuidedReportFormatterTest {
    private final GuidedReportFormatter formatter = new GuidedReportFormatter();

    @Test public void nullAndEmptyEvidenceBecomeEmptyText() {
        assertEquals("", formatter.sanitize(null));
        assertEquals("", formatter.sanitize(""));
    }

    @Test public void captureUsesNewestOutputAndRedactsBeforeTailTruncation() {
        StringBuilder output = new StringBuilder("old version banner\n");
        for (int i = 0; i < 4000; i++) output.append("old interface data\n");
        output.append("-----BEGIN PRIVATE KEY-----\nPRIVATE_SECRET\n-----END PRIVATE KEY-----\n");
        output.append("LATEST GigabitEthernet1/0/1 DOWN\n");
        String tail = formatter.captureTail(output.toString());
        assertTrue(tail.endsWith("LATEST GigabitEthernet1/0/1 DOWN\n"));
        assertFalse(tail.contains("old version banner"));
        assertFalse(tail.contains("PRIVATE_SECRET"));
        assertTrue(tail.length() <= GuidedReportFormatter.MAX_EVIDENCE);
    }

    @Test public void sanitizingBoundsLongEvidence() {
        StringBuilder raw = new StringBuilder();
        for (int i = 0; i < 20_000; i++) raw.append("Interface status: UP\n");
        String sanitized = formatter.sanitize(raw.toString());
        assertTrue(sanitized.length() <= GuidedReportFormatter.MAX_EVIDENCE);
        assertTrue(sanitized.startsWith("Interface status: UP"));
    }

    @Test public void removesCompleteAndIncompletePrivateKeyBlocks() {
        String complete = "Before\n-----BEGIN RSA PRIVATE KEY-----\nPRIVATE_SECRET_A\n"
                + "-----END RSA PRIVATE KEY-----\nAfter";
        String partial = "Before\n-----BEGIN OPENSSH PRIVATE KEY-----\nPRIVATE_SECRET_B";
        assertFalse(formatter.sanitize(complete).contains("PRIVATE_SECRET_A"));
        assertTrue(formatter.sanitize(complete).contains("After"));
        assertFalse(formatter.sanitize(partial).contains("PRIVATE_SECRET_B"));
        assertTrue(formatter.sanitize(partial).contains("Before"));
    }

    @Test public void redactsTruncatedPrivateKeyAtInputBound() {
        StringBuilder raw = new StringBuilder("-----BEGIN PRIVATE KEY-----\n");
        for (int i = 0; i < 20_000; i++) raw.append("PRIVATE_SECRET\n");
        assertFalse(formatter.sanitize(raw.toString()).contains("PRIVATE_SECRET"));
    }

    @Test public void removesAnsiSequencesButPreservesReadableLines() {
        assertEquals("Status: UP\nPort\t1\n",
                formatter.sanitize("\u001b[32mStatus: UP\u001b[0m\nPort\t1\n\u0000"));
    }

    @Test public void removesCommonSwitchCredentialCommands() {
        List<String> commands = Arrays.asList(
                "username admin password 7 TEST_SECRET_A",
                "username admin privilege 15 secret 9 TEST_SECRET_A",
                "enable secret 5 TEST_SECRET_A",
                "local-user admin password irreversible-cipher TEST_SECRET_A",
                "  password cipher TEST_SECRET_A",
                "snmp-server community TEST_SECRET_A ro",
                "snmp-agent community read cipher TEST_SECRET_A",
                "snmp-agent usm-user v3 admin group authentication-mode sha TEST_SECRET_A privacy-mode aes128 TEST_SECRET_B",
                "snmp-server user admin group v3 auth sha TEST_SECRET_A priv aes 128 TEST_SECRET_B",
                "radius-server key 7 TEST_SECRET_A",
                "tacacs-server key 7 TEST_SECRET_A",
                "key authentication cipher TEST_SECRET_A");
        for (String command : commands) {
            String safe = formatter.sanitize(command);
            assertFalse("Credential remains for: " + command.split(" ")[0], safe.contains("TEST_SECRET_A"));
            assertFalse(safe.contains("TEST_SECRET_B"));
        }
    }

    @Test public void redactsCredentialsEchoedAfterTerminalPrompts() {
        for (String command : new String[]{
                "SW1(config)#username admin privilege 15 secret 9 TEST_SECRET",
                "[SW1-luser-admin]password irreversible-cipher TEST_SECRET",
                "<SW1>snmp-agent usm-user v3 admin group authentication-mode sha TEST_SECRET"}) {
            assertFalse(formatter.sanitize(command).contains("TEST_SECRET"));
        }
    }

    @Test public void redactsAssignmentsAndBearerTokensWithoutRemovingContext() {
        String value = "Request failed: api_key=TEST_SECRET_A\n"
                + "Authorization: Bearer TEST_SECRET_B\n"
                + "password: TEST_SECRET_C\n"
                + "token=TEST_SECRET_D\n";
        String safe = formatter.sanitize(value);
        assertFalse(safe.contains("TEST_SECRET_A"));
        assertFalse(safe.contains("TEST_SECRET_B"));
        assertFalse(safe.contains("TEST_SECRET_C"));
        assertFalse(safe.contains("TEST_SECRET_D"));
    }

    @Test public void preservesOrdinaryDiagnosticSentencesContainingCredentialWords() {
        String diagnostic = "The API key was rejected by the provider.\n"
                + "Password authentication failed. Check the account and try again.\n"
                + "No authentication token is configured.\n"
                + "Check the SNMP community permission on the switch.\n"
                + "The password may have expired; contact the administrator.\n";
        assertEquals(diagnostic, formatter.sanitize(diagnostic));
    }

    @Test public void quotedCredentialValuesDoNotLeaveSensitiveFragments() {
        for (String assignment : new String[]{
                "Request failed: password=\"SECRET_FIRST SECRET_LAST\"",
                "{\"api_key\": \"JSON_SECRET\"}",
                "{\"token\": \"JSON_SECRET\"}"}) {
            String safe = formatter.sanitize(assignment);
            assertFalse(safe.contains("SECRET_FIRST"));
            assertFalse(safe.contains("SECRET_LAST"));
            assertFalse(safe.contains("JSON_SECRET"));
        }
    }

    @Test public void formatterIncludesSelectedLanguageAndEvidence() {
        DiagnosticFinding finding = sample(Status.WARNING, "Gi1/0/1 down down");
        String chinese = formatter.format("端口检查", "当前连接", "2026-10-13", Arrays.asList(finding), false);
        String english = formatter.format("Port check", "Current session", "2026-10-13", Arrays.asList(finding), true);
        assertTrue(chinese.contains("中文标题"));
        assertTrue(chinese.contains("中文解释"));
        assertTrue(chinese.contains("中文下一步"));
        assertFalse(chinese.contains("English title"));
        assertTrue(english.contains("English title"));
        assertTrue(english.contains("English explanation"));
        assertTrue(english.contains("English next step"));
        assertFalse(english.contains("中文标题"));
        assertTrue(english.contains("Source: Current session"));
        assertTrue(english.contains("Time: 2026-10-13"));
        assertTrue(english.contains("Gi1/0/1 down down"));
        assertTrue(english.contains("Review for sensitive data"));
    }

    @Test public void finalReportRedactsAllUserControlledFields() {
        DiagnosticFinding finding = new DiagnosticFinding(Status.WARNING,
                new GuideText("password=TITLE_SECRET", "password=TITLE_SECRET"),
                new GuideText("token=EXPLANATION_SECRET", "token=EXPLANATION_SECRET"),
                new GuideText("api_key=NEXT_SECRET", "api_key=NEXT_SECRET"),
                "enable secret 5 EVIDENCE_SECRET");
        String report = formatter.format("password=HEADER_SECRET", "token=SOURCE_SECRET",
                "api_key=TIME_SECRET", Arrays.asList(finding), true);
        for (String secret : new String[]{"TITLE_SECRET", "EXPLANATION_SECRET", "NEXT_SECRET",
                "EVIDENCE_SECRET", "HEADER_SECRET", "SOURCE_SECRET", "TIME_SECRET"}) {
            assertFalse(report.contains(secret));
        }
    }

    @Test public void reportWithManyFindingsIsBounded() {
        List<DiagnosticFinding> findings = new ArrayList<>();
        for (int i = 0; i < 2_000; i++) findings.add(sample(Status.UNKNOWN, "Output excerpt " + i));
        String report = formatter.format("Report", "Current session", "2026-10-13", findings, true);
        assertTrue(report.length() <= GuidedReportFormatter.MAX_EVIDENCE);
        assertTrue(report.startsWith("Report"));
    }

    @Test public void allFourStatusesHaveDistinctBilingualLabels() {
        Set<String> english = new HashSet<>();
        Set<String> chinese = new HashSet<>();
        for (Status status : Status.values()) {
            String en = GuidedReportFormatter.status(status, true);
            String zh = GuidedReportFormatter.status(status, false);
            assertFalse(en.trim().isEmpty());
            assertFalse(zh.trim().isEmpty());
            assertNotEquals(en, zh);
            english.add(en);
            chinese.add(zh);
        }
        assertEquals(4, english.size());
        assertEquals(4, chinese.size());
        assertEquals("Check passed", GuidedReportFormatter.status(Status.PASS, true));
        assertEquals("Needs attention", GuidedReportFormatter.status(Status.WARNING, true));
        assertEquals("Issue observed", GuidedReportFormatter.status(Status.FAIL, true));
        assertEquals("More evidence needed", GuidedReportFormatter.status(Status.UNKNOWN, true));
    }

    private static DiagnosticFinding sample(Status status, String evidence) {
        return new DiagnosticFinding(status, new GuideText("中文标题", "English title"),
                new GuideText("中文解释", "English explanation"),
                new GuideText("中文下一步", "English next step"), evidence);
    }
}
