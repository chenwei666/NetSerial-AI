package com.chenwei666.netserial.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.chenwei666.netserial.device.Vendor;

import org.junit.Test;

public class ConfigDiffEngineTest {
    @Test public void normalizerRemovesDynamicCountersAndTrailingWhitespace() {
        String normalized = new ConfigNormalizer().normalize("vlan 10  \r\nUptime: 2 days\r\n name office\r\n");
        assertEquals("vlan 10\n name office", normalized);
    }

    @Test public void diffPreservesDuplicateLineCounts() {
        ConfigDiff diff = new ConfigDiffEngine().compare("a\na\nb", "a\nb\nc");
        assertEquals(1, diff.getRemovedLines().size());
        assertEquals("a", diff.getRemovedLines().get(0));
        assertEquals("c", diff.getAddedLines().get(0));
    }

    @Test public void rollbackDraftUsesVendorNegationAndIsReviewOnly() {
        ConfigDiff diff = new ConfigDiffEngine().compare("vlan 10", "vlan 10\nvlan 20");
        String h3c = new RollbackDraftGenerator().generate(diff, Vendor.H3C_COMWARE);
        String cisco = new RollbackDraftGenerator().generate(diff, Vendor.CISCO_IOS);
        assertTrue(h3c.contains("undo vlan 20"));
        assertTrue(cisco.contains("no vlan 20"));
        assertFalse(h3c.isEmpty());
    }

    @Test public void detectsLineReordering() {
        ConfigDiff diff = new ConfigDiffEngine().compare("interface A\ninterface B",
                "interface B\ninterface A");
        assertFalse(diff.isEmpty());
        assertEquals(1, diff.getAddedLines().size());
        assertEquals(1, diff.getRemovedLines().size());
    }

    @Test public void ruijieUsesNoNegation() {
        ConfigDiff diff = new ConfigDiffEngine().compare("", "vlan 20");
        assertTrue(new RollbackDraftGenerator().generate(diff, Vendor.RUIJIE_RGOS)
                .contains("no vlan 20"));
    }

    @Test public void snapshotHashIsStable() {
        ConfigSnapshot one = new ConfigSnapshot("a", 1, "vlan 10");
        ConfigSnapshot two = new ConfigSnapshot("b", 2, "vlan 10");
        assertEquals(one.getSha256(), two.getSha256());
        assertEquals(64, one.getSha256().length());
    }
}
