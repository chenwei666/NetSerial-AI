package com.chenwei666.netserial.guided.switches;

import com.chenwei666.netserial.device.CliMode;
import com.chenwei666.netserial.device.Vendor;
import com.chenwei666.netserial.guided.DiagnosticFinding;
import com.chenwei666.netserial.guided.DiagnosticFinding.Status;
import com.chenwei666.netserial.guided.GuidedScenario;
import com.chenwei666.netserial.safety.CommandEvaluationRequest;
import com.chenwei666.netserial.safety.GuardDecision;
import com.chenwei666.netserial.safety.RiskLevel;
import com.chenwei666.netserial.safety.RuleBasedExecutionGuard;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class SwitchGuideEngineTest {
    private final SwitchGuideEngine engine = new SwitchGuideEngine();

    @Test public void emptyCaptureIsUnknownAcrossSwitchScenarios() {
        for (GuidedScenario scenario : switchScenarios()) {
            List<DiagnosticFinding> result = engine.analyze(scenario, Vendor.CISCO_IOS, "", "");
            assertEquals(1, result.size());
            assertEquals(Status.UNKNOWN, result.get(0).getStatus());
        }
    }

    @Test public void genericVendorNeverEmitsVendorSpecificCommands() {
        for (GuidedScenario scenario : switchScenarios()) {
            assertTrue(engine.commands(scenario, Vendor.GENERIC, "").isEmpty());
            assertTrue(engine.commands(scenario, null, "").isEmpty());
        }
        assertNoPass(engine.analyze(GuidedScenario.PORT_LINK, Vendor.GENERIC, "", "Gi1/0/1 up up"));
    }

    @Test public void allDraftsAreReadOnlyAndManualUnderExistingGuard() {
        for (Vendor vendor : Vendor.values()) {
            if (vendor == Vendor.GENERIC) continue;
            for (GuidedScenario scenario : switchScenarios()) {
                List<GuideCommand> commands = engine.commands(scenario, vendor, "");
                assertFalse(commands.isEmpty());
                assertTrue(commands.size() <= 4);
                for (GuideCommand command : commands) {
                    assertTrue(command.getCommand().startsWith("show ") || command.getCommand().startsWith("display "));
                    assertFalse(command.getCommand().contains("\n"));
                    assertFalse(command.getCommand().contains("|"));
                    GuardDecision decision = RuleBasedExecutionGuard.createDefault().evaluate(
                            new CommandEvaluationRequest(vendor, CliMode.USER_VIEW, command.getCommand(), RiskLevel.R0_INFORMATIONAL));
                    assertEquals(RiskLevel.R1_READ_ONLY, decision.getEffectiveRisk());
                    assertFalse(decision.isAutomaticExecutionAllowed());
                    assertFalse(command.getPurpose().get(false).isEmpty());
                    assertFalse(command.getPurpose().get(true).isEmpty());
                }
            }
        }
    }

    @Test public void safeParametersAppearInBoundedReadOnlyCommands() {
        assertEquals("display interface GigabitEthernet1/0/1",
                engine.commands(GuidedScenario.PORT_LINK, Vendor.H3C_COMWARE, "GigabitEthernet1/0/1").get(0).getCommand());
        assertEquals("show vlan id 10", engine.commands(GuidedScenario.VLAN_GATEWAY,
                Vendor.CISCO_IOS, "0010").get(0).getCommand());
    }

    @Test public void invalidAndMultilineParametersCannotBecomeCommands() {
        String[] invalidPorts = {"Gi1/0/1\nreload", "Gi1/0/1\n", "Gi1/0/1\r", "Gi1/0/1;reload",
                "Gi1/0/1 | include secret", "Gi1/0/1\u2028", "brief", "Gi1/0/1\t", "\n"};
        for (String target : invalidPorts) {
            assertRejected(GuidedScenario.PORT_LINK, target);
            assertNoPass(engine.analyze(GuidedScenario.PORT_LINK, Vendor.CISCO_IOS, target, "Gi1/0/1 up up"));
        }
        for (String target : new String[]{"0", "4095", "-1", "10;reload", "10\n", "10 20", "9999999999999"}) {
            assertRejected(GuidedScenario.VLAN_GATEWAY, target);
        }
    }

    @Test public void unknownAndConflictingBannersAreNotSuccess() {
        assertNoPass(engine.analyze(GuidedScenario.IDENTIFY_DEVICE, Vendor.GENERIC, "", "Welcome to Switch"));
        assertNoPass(engine.analyze(GuidedScenario.IDENTIFY_DEVICE, Vendor.GENERIC, "", "Huawei H3C"));
        assertNoPass(engine.analyze(GuidedScenario.PORT_LINK, Vendor.H3C_COMWARE, "", "Cisco IOS\nGi1/0/1 up up"));
    }

    @Test public void explicitFingerprintOnlyConfirmsVendor() {
        DiagnosticFinding finding = engine.analyze(GuidedScenario.IDENTIFY_DEVICE, Vendor.GENERIC,
                "", "H3C Comware Software, Version 7").get(0);
        assertEquals(Status.PASS, finding.getStatus());
        assertTrue(finding.getExplanation().get(true).contains("not a device-health assessment"));
    }

    @Test public void commandErrorsInvalidateOtherwisePassingOutput() {
        String[] errors = {"% Invalid input detected at '^' marker.", "Error: Unrecognized command found at '^' position.",
                "Permission denied", "% Incomplete command.", "Unknown command", "Authorization failed"};
        for (String error : errors) {
            List<DiagnosticFinding> findings = engine.analyze(GuidedScenario.PORT_LINK, Vendor.CISCO_IOS, "",
                    "Gi1/0/1 is up, line protocol is up\n" + error);
            assertNoPass(findings);
            assertEquals(Status.WARNING, findings.get(0).getStatus());
        }
    }

    @Test public void idleDescriptionAndBareCommandAreNotHealthEvidence() {
        for (String capture : new String[]{"show interfaces status", "description all ports up", "hostname healthy-switch", "show version"}) {
            assertNoPass(engine.analyze(GuidedScenario.PORT_LINK, Vendor.CISCO_IOS, "", capture));
            assertNoPass(engine.analyze(GuidedScenario.SWITCH_HEALTH, Vendor.CISCO_IOS, "", capture));
        }
    }

    @Test public void administrativeDownIsNotDiagnosedAsBadCable() {
        DiagnosticFinding finding = engine.analyze(GuidedScenario.PORT_LINK, Vendor.CISCO_IOS, "Gi1/0/1",
                "GigabitEthernet1/0/1 is administratively down, line protocol is down").get(0);
        assertEquals(Status.WARNING, finding.getStatus());
        assertTrue(finding.getTitle().get(true).contains("administratively"));
        assertTrue(finding.getExplanation().get(true).contains("not a broken cable"));
    }

    @Test public void physicalDownHasConcreteCableChecksWithoutDeviceFailureClaim() {
        DiagnosticFinding finding = engine.analyze(GuidedScenario.PORT_LINK, Vendor.H3C_COMWARE, "",
                "GigabitEthernet1/0/1 current state : DOWN").get(0);
        assertEquals(Status.WARNING, finding.getStatus());
        assertTrue(finding.getExplanation().get(true).contains("Unused ports"));
        assertTrue(finding.getNextStep().get(true).contains("cable"));
    }

    @Test public void upLinkDoesNotProveInternetConnectivity() {
        DiagnosticFinding finding = engine.analyze(GuidedScenario.PORT_LINK, Vendor.CISCO_IOS, "",
                "GigabitEthernet1/0/1 is up, line protocol is up").get(0);
        assertEquals(Status.PASS, finding.getStatus());
        assertTrue(finding.getExplanation().get(true).contains("not DHCP"));
    }

    @Test public void matchingDisplayPhysicalAndProtocolStatesAreRecognized() {
        DiagnosticFinding up = engine.analyze(GuidedScenario.PORT_LINK, Vendor.HUAWEI_VRP, "",
                "GigabitEthernet0/0/1 current state : UP\nLine protocol current state : UP").get(0);
        assertEquals(Status.PASS, up.getStatus());
        assertNoPass(engine.analyze(GuidedScenario.PORT_LINK, Vendor.HUAWEI_VRP, "",
                "GigabitEthernet0/0/1 current state : UP\nLine protocol current state : DOWN"));
    }

    @Test public void unrelatedInterfaceCannotPassRequestedPort() {
        assertNoPass(engine.analyze(GuidedScenario.PORT_LINK, Vendor.CISCO_IOS, "Gi1/0/2",
                "GigabitEthernet1/0/1 is up, line protocol is up"));
    }

    @Test public void missingProtocolOrPartialUpOutputIsUnknown() {
        assertNoPass(engine.analyze(GuidedScenario.PORT_LINK, Vendor.H3C_COMWARE, "",
                "GigabitEthernet1/0/1 current state : UP"));
        assertNoPass(engine.analyze(GuidedScenario.PORT_LINK, Vendor.H3C_COMWARE, "", "GE1/0/1 UP --"));
    }

    @Test public void errorDisabledPortNeedsCauseBeforeRestoration() {
        DiagnosticFinding finding = engine.analyze(GuidedScenario.PORT_LINK, Vendor.CISCO_IOS, "",
                "Gi1/0/1 desk err-disabled 10 auto auto 10/100/1000BaseTX").get(0);
        assertEquals(Status.FAIL, finding.getStatus());
        assertTrue(finding.getNextStep().get(true).contains("Resolve the cause"));
    }

    @Test public void vlanInterfaceStateDoesNotConfirmDhcpOrGateway() {
        List<DiagnosticFinding> findings = engine.analyze(GuidedScenario.VLAN_GATEWAY, Vendor.CISCO_IOS, "10",
                "Vlan10 192.0.2.1 YES manual up up\nVlan20 192.0.2.2 YES manual down down");
        assertEquals(Status.PASS, findings.get(0).getStatus());
        assertTrue(findings.get(0).getTitle().get(true).startsWith("Vlan10"));
        assertEquals(Status.UNKNOWN, findings.get(findings.size() - 1).getStatus());
        assertTrue(findings.get(findings.size() - 1).getNextStep().get(true).contains("DHCP"));
    }

    @Test public void healthCaptureCanWarnButCannotCertifyWholeDeviceHealthy() {
        List<DiagnosticFinding> findings = engine.analyze(GuidedScenario.SWITCH_HEALTH, Vendor.CISCO_IOS, "",
                "CPU usage: 97%\nMemory used: 91%\n12 input errors\nfan failed");
        assertNoPass(findings);
        assertTrue(findings.size() >= 4);
        assertEquals(Status.UNKNOWN, findings.get(findings.size() - 1).getStatus());
        for (DiagnosticFinding finding : findings) {
            assertFalse(finding.getExplanation().get(false).isEmpty());
            assertFalse(finding.getNextStep().get(true).isEmpty());
        }
    }

    @Test public void freeMemoryAndIdleCpuAreNotUsageAlarms() {
        List<DiagnosticFinding> findings = engine.analyze(GuidedScenario.SWITCH_HEALTH, Vendor.CISCO_IOS, "",
                "CPU idle: 99%\nMemory free: 90%\nNo fan fault\n0 input errors");
        assertEquals(1, findings.size());
        assertEquals(Status.UNKNOWN, findings.get(0).getStatus());
    }

    @Test public void configCaptureDoesNotClaimSavedBackup() {
        DiagnosticFinding finding = engine.analyze(GuidedScenario.CONFIG_BACKUP, Vendor.H3C_COMWARE, "",
                "sysname Switch\ninterface GigabitEthernet1/0/1\n#\nreturn").get(0);
        assertEquals(Status.UNKNOWN, finding.getStatus());
        assertTrue(finding.getTitle().get(true).contains("has not been saved"));
        assertTrue(finding.getNextStep().get(true).contains("explicitly save"));
    }

    @Test public void diagnosticEvidenceRedactsCredentialsAndAnsi() {
        DiagnosticFinding finding = engine.analyze(GuidedScenario.PORT_LINK, Vendor.H3C_COMWARE, "",
                "\u001b[31mError: unsupported command\u001b[0m\nlocal-user admin password cipher TOP_SECRET\n"
                        + "username admin privilege 15 secret 9 SECOND_SECRET\napi_key=API_SECRET\n"
                        + "-----BEGIN PRIVATE KEY-----\nPRIVATE_SECRET\n-----END PRIVATE KEY-----").get(0);
        assertFalse(finding.getEvidence().contains("TOP_SECRET"));
        assertFalse(finding.getEvidence().contains("SECOND_SECRET"));
        assertFalse(finding.getEvidence().contains("API_SECRET"));
        assertFalse(finding.getEvidence().contains("PRIVATE_SECRET"));
        assertFalse(finding.getEvidence().contains("\u001b"));
    }

    @Test public void redactionCannotHideCommandFailureAndProducePassingState() {
        List<DiagnosticFinding> findings = engine.analyze(GuidedScenario.PORT_LINK, Vendor.CISCO_IOS, "",
                "Gi1/0/1 is up, line protocol is up\nError: password authorization failed secret SECRET_VALUE");
        assertNoPass(findings);
        assertFalse(findings.get(0).getEvidence().contains("SECRET_VALUE"));
    }

    @Test public void oversizedCaptureCannotLeakPartialCredentialAndIsBounded() {
        StringBuilder input = new StringBuilder("CPU usage: 90%\n");
        while (input.length() < 31_990) input.append("irrelevant line\n");
        input.append("password cipher VERY_SECRET_ENDING");
        List<DiagnosticFinding> findings = engine.analyze(GuidedScenario.SWITCH_HEALTH, Vendor.H3C_COMWARE, "", input.toString());
        for (DiagnosticFinding finding : findings) {
            assertFalse(finding.getEvidence().contains("VERY_SECRET"));
            assertTrue(finding.getEvidence().length() <= 8000);
        }
        assertTrue(findings.get(findings.size() - 1).getTitle().get(true).contains("part of"));
    }

    @Test public void repeatedAlarmsHaveBoundedResults() {
        StringBuilder input = new StringBuilder();
        for (int i = 0; i < 500; i++) input.append("CPU usage: 99%\n");
        assertTrue(engine.analyze(GuidedScenario.SWITCH_HEALTH, Vendor.CISCO_IOS, "", input.toString()).size() <= 17);
    }

    private void assertRejected(GuidedScenario scenario, String target) {
        try {
            engine.commands(scenario, Vendor.H3C_COMWARE, target);
            fail("Expected invalid target to be rejected");
        } catch (IllegalArgumentException expected) {
            // Invalid text must never reach a command preview.
        }
    }

    private static void assertNoPass(List<DiagnosticFinding> findings) {
        assertFalse(findings.isEmpty());
        for (DiagnosticFinding finding : findings) assertNotEquals(Status.PASS, finding.getStatus());
    }

    private static GuidedScenario[] switchScenarios() {
        return new GuidedScenario[]{GuidedScenario.IDENTIFY_DEVICE, GuidedScenario.PORT_LINK,
                GuidedScenario.VLAN_GATEWAY, GuidedScenario.SWITCH_HEALTH, GuidedScenario.CONFIG_BACKUP};
    }
}
