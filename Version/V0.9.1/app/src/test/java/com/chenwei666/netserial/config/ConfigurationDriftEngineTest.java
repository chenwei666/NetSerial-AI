package com.chenwei666.netserial.config;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class ConfigurationDriftEngineTest {
    @Test public void detectsNoDriftAfterNormalization() {
        ConfigDriftAssessment result = new ConfigurationDriftEngine().assess(
                "interface GigabitEthernet1/0/1\n description uplink\n",
                "interface GigabitEthernet1/0/1\r\n description uplink\r\n");
        assertEquals(ConfigDriftSeverity.NONE, result.getSeverity());
        assertEquals(0, result.getTotalChanges());
    }

    @Test public void classifiesSensitiveManagementChangeAsHigh() {
        ConfigDriftAssessment result = new ConfigurationDriftEngine().assess(
                "ssh server enable\n", "telnet server enable\n");
        assertEquals(ConfigDriftSeverity.HIGH, result.getSeverity());
        assertEquals(2, result.getSensitiveChanges());
    }
}
