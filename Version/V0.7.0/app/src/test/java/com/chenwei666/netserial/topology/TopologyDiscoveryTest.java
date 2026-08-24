package com.chenwei666.netserial.topology;

import org.junit.Test;

import static org.junit.Assert.*;

public class TopologyDiscoveryTest {
    @Test public void parsesAndDeduplicatesLldpNeighbors() {
        String block = "Local Interface: GigabitEthernet1/0/1\nSystem Name: access-01\nPort ID: GigabitEthernet1/0/48";
        TopologyGraph graph = new TopologyParser().parse("core-01", block + "\n\n" + block);
        assertEquals(2, graph.getNodes().size());
        assertEquals(1, graph.getLinks().size());
        assertEquals("access-01", graph.getLinks().get(0).getRemoteNode());
    }

    @Test public void snmpV3PlanIsBoundedAndContainsNoSecrets() {
        SnmpV3QueryPlan plan = new SnmpV3DiscoveryPlanner().plan(
                "192.168.1.0/24", "netops", "SHA-256", "AES-256");
        assertEquals(5, plan.getObjectIdentifiers().size());
        assertFalse(plan.storesSecrets());
        try {
            new SnmpV3DiscoveryPlanner().plan("10.0.0.0/16", "netops", "SHA", "AES-128");
            fail("large scopes must be rejected");
        } catch (IllegalArgumentException expected) { }
    }
}
