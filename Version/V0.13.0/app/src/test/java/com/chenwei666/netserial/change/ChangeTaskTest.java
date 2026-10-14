package com.chenwei666.netserial.change;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.ArrayList;

public class ChangeTaskTest {
    private ChangeTask draft(long start, long end) {
        return new ChangeTask("id", "CHG-001", "DC1", "SW-CORE-01", "chenwei666",
                "Add VLAN", "display current-configuration", "vlan 116",
                "display vlan 116", "undo vlan 116", start, end,
                ChangeTaskStatus.DRAFT, new ArrayList<>());
    }

    @Test public void activeTaskAuthorizesOnlyMatchingDeviceAndWindow() {
        ChangeTask task = draft(1_000, 10_000).start(1_100);
        assertTrue(task.isAuthorizedAt(5_000, "sw-core-01"));
        assertFalse(task.isAuthorizedAt(11_000, "SW-CORE-01"));
        assertFalse(task.isAuthorizedAt(5_000, "SW-EDGE-01"));
    }

    @Test public void appendAndCompletePreserveEvidence() {
        ChangeTask task = draft(1_000, 10_000).start(1_100)
                .append(new ChangeEvent(2_000, ChangeEventType.COMMAND_SENT,
                        "SW-CORE-01", "display vlan 116"))
                .complete(3_000);
        assertEquals(ChangeTaskStatus.COMPLETED, task.getStatus());
        assertEquals(3, task.getEvents().size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void rollbackPlanIsRequired() {
        new ChangeTask("id", "CHG-001", "", "SW", "op", "goal", "", "", "", "",
                1_000, 2_000, ChangeTaskStatus.DRAFT, new ArrayList<>());
    }

    @Test public void formatterRedactsCredentialsFromTaskFieldsAndEvents() {
        ChangeTask task = new ChangeTask("id", "CHG-001", "DC1", "SW-CORE-01", "chenwei666",
                "rotate password: oldSecret", "display current", "username admin secret 5 hashValue",
                "display users", "snmp-server community private RO", 1_000, 2_000,
                ChangeTaskStatus.DRAFT, new ArrayList<>()).start(1_100).append(new ChangeEvent(1_500,
                ChangeEventType.OUTPUT_CAPTURED, "SW-CORE-01", "token=abcdef123456"));
        String markdown = new ChangeEvidenceFormatter().toMarkdown(task);
        assertFalse(markdown.contains("oldSecret"));
        assertFalse(markdown.contains("hashValue"));
        assertFalse(markdown.contains("private"));
        assertFalse(markdown.contains("abcdef123456"));
    }
}
