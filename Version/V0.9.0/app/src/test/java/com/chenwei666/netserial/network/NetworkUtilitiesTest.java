package com.chenwei666.netserial.network;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.net.UnknownHostException;

public class NetworkUtilitiesTest {
    @Test public void cancelledBatchStopsBeforeConnecting() throws Exception {
        Thread.currentThread().interrupt();
        try {
            new NetworkProbeService().tcpBatch("127.0.0.1", "22,23", 1_000);
        } catch (InterruptedException expected) {
            return;
        } finally {
            Thread.interrupted();
        }
        throw new AssertionError("cancelled probe should stop before connecting");
    }

    @Test(expected = UnknownHostException.class)
    public void unresolvedBatchTargetIsNotReportedAsClosedPorts() throws Exception {
        new NetworkProbeService().tcpBatch("256.256.256.256", "22,23", 1_000);
    }

    @Test public void addressSummaryReturnsStructuredFlags() throws Exception {
        AddressProbeResult result = new NetworkProbeService().addressSummary("127.0.0.1");

        assertEquals("127.0.0.1", result.getHost());
        assertEquals(1, result.getAddresses().size());
        assertTrue(result.getAddresses().get(0).isLoopback());
    }

    @Test public void calculatesIpv6PrefixWithoutBroadcast() {
        Ipv6Network network = new Ipv6Calculator().calculate("2001:db8:116::1234/64");
        assertEquals("2001:db8:116:0:0:0:0:0/64", network.getNetworkPrefix());
        assertEquals("18446744073709551616", network.getTotalAddresses());
    }

    @Test public void normalizesCiscoMacAndFindsBundledOui() {
        MacAddressInfo info = new MacOuiLookup().lookup("0000.0c12.3456");
        assertEquals("00:00:0C:12:34:56", info.getNormalized());
        assertEquals("Cisco", info.getVendor());
        assertFalse(info.isLocallyAdministered());
    }

    @Test public void detectsLocallyAdministeredMac() {
        assertTrue(new MacOuiLookup().lookup("02:00:00:00:00:01").isLocallyAdministered());
    }

    @Test public void extractorDeduplicatesAndPreservesFirstAppearance() {
        String result = new NetworkIdentifierExtractor().extract(
                "GE1/0/1 192.168.1.1 aa:bb:cc:dd:ee:ff then 192.168.1.1");
        assertEquals("192.168.1.1\naa:bb:cc:dd:ee:ff\nGE1/0/1", result);
    }

    @Test public void portCatalogIncludesNetconf() {
        boolean found = false;
        for (PortReference entry : new CommonPortCatalog().list()) {
            if (entry.getPort() == 830) found = true;
        }
        assertTrue(found);
    }
}
