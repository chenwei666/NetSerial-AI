package com.chenwei666.netserial.network;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class Ipv4CalculatorTest {
    @Test public void calculatesStandardSubnet() {
        Ipv4Network network = new Ipv4Calculator().calculate("192.168.116.10/24");
        assertEquals("192.168.116.0", network.getNetwork());
        assertEquals("255.255.255.0", network.getNetmask());
        assertEquals("192.168.116.255", network.getBroadcast());
        assertEquals("192.168.116.1", network.getFirstUsable());
        assertEquals("192.168.116.254", network.getLastUsable());
        assertEquals(256, network.getTotalAddresses());
    }

    @Test public void thirtyOnePrefixKeepsBothPointToPointAddresses() {
        Ipv4Network network = new Ipv4Calculator().calculate("10.0.0.4/31");
        assertEquals("10.0.0.4", network.getFirstUsable());
        assertEquals("10.0.0.5", network.getLastUsable());
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsAmbiguousLeadingZeroAddress() {
        new Ipv4Calculator().calculate("010.0.0.1/24");
    }
}
