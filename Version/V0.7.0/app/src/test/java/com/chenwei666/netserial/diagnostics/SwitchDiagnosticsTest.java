package com.chenwei666.netserial.diagnostics;

import com.chenwei666.netserial.device.Vendor;
import com.chenwei666.netserial.safety.RiskLevel;

import org.junit.Test;

import static org.junit.Assert.*;

public class SwitchDiagnosticsTest {
    @Test public void healthPlanIsVendorAwareAndReadOnly() {
        HealthCheckPlan plan = new SwitchHealthEngine().plan(Vendor.HUAWEI_VRP);
        assertEquals(RiskLevel.R0_INFORMATIONAL, plan.getRisk());
        assertTrue(plan.commandBatch().contains("display cpu-usage"));
        assertFalse(plan.commandBatch().toLowerCase().contains("save"));
    }

    @Test public void detectsCriticalAndWarningEvidence() {
        HealthReport report = new SwitchHealthEngine().analyze(
                "CPU usage 97%\nMemory usage 84%\nTemperature 88 C\n1500 input errors\nfan failed");
        assertTrue(report.hasCriticalFinding());
        assertTrue(report.getFindings().size() >= 4);
    }

    @Test public void portWorkflowIsReadOnlyAndRejectsInjection() {
        PortTroubleshootingPlan plan = new PortTroubleshootingEngine().plan(
                Vendor.CISCO_IOS, PortLookupType.IP, "10.0.0.10");
        assertEquals(RiskLevel.R0_INFORMATIONAL, plan.getRisk());
        assertEquals(8, plan.getSteps().size());
        assertTrue(plan.getSteps().get(0).getCommand().startsWith("show ip arp"));
        try {
            new PortTroubleshootingEngine().plan(Vendor.CISCO_IOS, PortLookupType.IP, "10.0.0.1;reload");
            fail("injection should be rejected");
        } catch (IllegalArgumentException expected) { }
        try {
            new PortTroubleshootingEngine().plan(Vendor.CISCO_IOS, null, "Gi1/0/1");
            fail("null lookup type should be rejected");
        } catch (IllegalArgumentException expected) { }
    }
}
