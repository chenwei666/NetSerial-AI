package com.chenwei666.netserial.compliance;

import com.chenwei666.netserial.device.Vendor;
import org.junit.Test;

import static org.junit.Assert.*;

public class ConfigComplianceEngineTest {
    @Test public void flagsPlainManagementAndDefaultCommunity() {
        ComplianceReport report = new ConfigComplianceEngine().analyze(Vendor.CISCO_IOS,
                "ip http server\nno ip http secure-server\n"
                        + "snmp-server community public ro\npassword 0 weak\n");
        assertTrue(report.hasHighRisk());
        assertTrue(has(report, "HTTP_WITHOUT_HTTPS"));
        assertTrue(has(report, "DEFAULT_SNMP_COMMUNITY"));
        assertTrue(has(report, "PLAIN_PASSWORD_SYNTAX"));
    }

    @Test public void emptyConfigProducesEvidencePromptsNotPassClaim() {
        ComplianceReport report = new ConfigComplianceEngine().analyze(Vendor.H3C_COMWARE, "#");
        assertTrue(has(report, "TIME_SYNC_NOT_FOUND"));
        assertTrue(has(report, "SSH_NOT_FOUND"));
    }

    private static boolean has(ComplianceReport report, String id) {
        for (ComplianceFinding finding : report.getFindings()) if (id.equals(finding.getRuleId())) return true;
        return false;
    }
}
