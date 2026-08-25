package com.chenwei666.netserial.config;

import com.chenwei666.netserial.device.Vendor;

import org.junit.Test;

import static org.junit.Assert.*;

public class ConfigurationBackupEngineTest {
    @Test public void createsRedactedContentAddressedSnapshot() {
        ConfigurationBackupEngine engine = new ConfigurationBackupEngine();
        ConfigSnapshot snapshot = engine.capture("edge-01", "hostname edge-01\npassword=plain-text\n", 1000);
        assertTrue(snapshot.getNormalizedText().contains("[REDACTED]"));
        assertFalse(snapshot.getNormalizedText().contains("plain-text"));
        assertEquals(64, snapshot.getSha256().length());
    }

    @Test public void vendorPlanUsesReadOnlyCapture() {
        ConfigBackupPlan plan = new ConfigurationBackupEngine().plan(Vendor.H3C_COMWARE);
        assertEquals("display current-configuration", plan.getCommands().get(1));
    }
}
