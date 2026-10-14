package com.chenwei666.netserial.network.toolbox;

import org.junit.Test;
import static org.junit.Assert.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicBoolean;

public class ToolboxUtilitiesTest {
    @Test public void masksRequireContiguousBits() {
        assertEquals(24, OfflineTools.prefixFromMask("255.255.255.0"));
        assertEquals(0, OfflineTools.prefixFromMask("0.0.0.0"));
        assertEquals(32, OfflineTools.prefixFromMask("/32"));
        for (String value : new String[]{"255.0.255.0", "33", "-1", "255.255.256.0"}) {
            try { OfflineTools.prefixFromMask(value); fail(value); } catch (IllegalArgumentException expected) { }
        }
    }
    @Test public void discoveryBoundedPrivateOnly() {
        assertEquals(62, ConnectionProbes.discoveryTargets("192.168.1.0/26").size());
        assertEquals(1, ConnectionProbes.discoveryTargets("10.0.0.1/32").size());
        assertEquals(2, ConnectionProbes.discoveryTargets("172.16.0.0/31").size());
        for (String input : new String[]{"192.168.1.0/24", "1.1.1.0/26", "172.32.0.0/26", "127.0.0.1/32", "10.0.0.1;id/32"}) {
            try { ConnectionProbes.discoveryTargets(input); fail(input); } catch (IllegalArgumentException expected) { }
        }
    }
    @Test public void cancellationClosesRegisteredResourcesAndPreventsReuse() throws Exception {
        ProbeSession session = new ProbeSession(1000, null);
        AtomicBoolean closed = new AtomicBoolean();
        session.track(() -> closed.set(true)); session.cancel();
        assertTrue(closed.get());
        try { session.check(); fail(); } catch (InterruptedException expected) { }
        AtomicBoolean late = new AtomicBoolean();
        try { session.track(() -> late.set(true)); fail(); } catch (InterruptedException expected) { }
        assertTrue(late.get());
    }
    @Test public void loopbackConnectIsOpenThenRefusedNotTimeout() throws Exception {
        int port;
        InetAddress local = InetAddress.getByName("127.0.0.1");
        try (ServerSocket server = new ServerSocket(0, 1, local)) {
            port = server.getLocalPort();
            assertEquals(ConnectionProbes.State.OPEN, ConnectionProbes.connect(local, port, 1000, new ProbeSession(2000, null)).state);
        }
        assertEquals(ConnectionProbes.State.REFUSED, ConnectionProbes.connect(local, port, 1000, new ProbeSession(2000, null)).state);
    }
    @Test public void exportsKeepStatusAndFacts() {
        ToolResult result = new ToolResult(ToolResult.Status.PARTIAL, "Ping", "No response",
                Collections.singletonList(new ToolResult.Row("Loss", "100%")), "raw");
        assertTrue(result.export().contains("PARTIAL"));
        assertTrue(result.export().contains("Loss: 100%"));
        try { result.rows.clear(); fail(); } catch (UnsupportedOperationException expected) { }
    }
    @Test public void subnetCountsIncludePointToPoint() {
        ToolRequest request = new ToolRequest("10.0.0.0/31", "", 1000, 4, true);
        assertTrue(new OfflineTools().cidr(request).export().contains("可用地址数: 2"));
    }
    @Test public void locallyAdministeredMacIsNotMisrepresented() {
        ToolRequest request = new ToolRequest("02:00:00:00:00:01", "", 1000, 4, false);
        assertTrue(new OfflineTools().mac(request).export().contains("Locally administered"));
    }
}
