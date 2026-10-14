package com.chenwei666.netserial.network;

import org.junit.Test;
import static org.junit.Assert.*;

public class OfflineValidationTest {
    @Test public void invalidAddressesAreNotExtracted() {
        String output = new NetworkIdentifierExtractor().extract("bad 999.1.1.1 1.2.3.4/99 good 192.168.1.1/24");
        assertFalse(output.contains("999.1.1.1"));
        assertFalse(output.contains("1.2.3.4"));
        assertTrue(output.contains("192.168.1.1/24"));
    }
    @Test public void ipv6IsExtractedWithoutTreatingMacAsIpv6() {
        String output = new NetworkIdentifierExtractor().extract("2001:db8::1/64 aa:bb:cc:dd:ee:ff ::1");
        assertTrue(output.contains("2001:db8::1/64"));
        assertTrue(output.contains("::1"));
        assertEquals(3, output.split("\n").length);
    }
    @Test public void ipv4RejectsSignedOctetsAndPrefixes() {
        for (String input : new String[]{"+1.2.3.4/24", "1.2.3.4/+24", "01.2.3.4/24"}) {
            try { new Ipv4Calculator().calculate(input); fail(input); } catch (IllegalArgumentException expected) { }
        }
    }
}
