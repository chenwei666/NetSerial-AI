package com.chenwei666.netserial.network.toolbox;

import org.junit.Test;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import static org.junit.Assert.*;

public class CoreProbesTest {
    private static final String REPLY = "64 bytes from 192.0.2.1: icmp_seq=1 ttl=64 time=12.25 ms\n";
    private ToolRequest request(int count) { return new ToolRequest("router.example", "", 1000, count, false); }
    private ProbeSession session() { return new ProbeSession(120000, null); }
    private CoreProbes core(CoreProbes.Execution... outputs) {
        AtomicInteger index = new AtomicInteger();
        return new CoreProbes((args, timeout, session) -> outputs[Math.min(index.getAndIncrement(), outputs.length - 1)],
                (host, timeout, session) -> InetAddress.getByAddress(new byte[]{(byte)192,0,2,1}));
    }
    private CoreProbes.Execution output(String text) { return new CoreProbes.Execution(0, text, false); }

    @Test public void rttComesFromReplyNotProcessWallTime() throws Exception {
        ToolResult result = core(output(REPLY)).ping(request(2), session());
        assertEquals(ToolResult.Status.OK, result.status);
        assertEquals(Arrays.asList(12.25, 12.25), result.samples);
        assertTrue(result.export().contains("12.250"));
        assertTrue(result.export().contains("0.0%"));
    }
    @Test public void missingReplyIsNanAndIncludedInLoss() throws Exception {
        ToolResult result = core(output(REPLY), new CoreProbes.Execution(1, "1 packets transmitted, 0 received", false))
                .ping(request(2), session());
        assertEquals(ToolResult.Status.PARTIAL, result.status);
        assertTrue(Double.isNaN(result.samples.get(1)));
        assertTrue(result.export().contains("50.0%"));
    }
    @Test public void permissionErrorIsUnsupportedNotSuccess() throws Exception {
        ToolResult result = core(new CoreProbes.Execution(1, "ping: socket: Operation not permitted", false))
                .trace(request(4), session());
        assertEquals(ToolResult.Status.UNSUPPORTED, result.status);
        assertFalse(result.summary.contains("reached"));
    }
    @Test public void summaryIsNeverParsedAsPacket() {
        assertFalse(PingOutput.parse("rtt min/avg/max/mdev = 2.0/3.0/4.0/0.0 ms").reply);
        assertFalse(PingOutput.parse("round trip min/avg/max = 2/3/4 ms").reply);
    }
    @Test public void lessThanReplyStaysExplicit() throws Exception {
        ToolResult result = core(output(REPLY.replace("=12.25", "<1"))).ping(request(1), session());
        assertTrue(result.export().contains("<1.000 ms"));
        assertTrue(result.export().contains("upper bounds"));
    }
    @Test public void traceShowsOnlyObservedRoutersWithoutInventedRtt() throws Exception {
        ToolResult result = core(output("From 192.0.2.2 icmp_seq=1 Time to live exceeded\n"), output(REPLY))
                .trace(request(4), session());
        assertEquals(ToolResult.Status.OK, result.status);
        assertTrue(result.export().contains("192.0.2.2"));
        assertTrue(result.export().contains("RTT not provided"));
        assertEquals(2, result.rows.size());
    }
    @Test public void traceUnknownHopIsNotZeroMs() throws Exception {
        ToolResult result = core(output(""), output(REPLY)).trace(request(4), session());
        assertTrue(result.rows.get(0).value.contains("*"));
        assertFalse(result.rows.get(0).value.contains("0 ms"));
    }
    @Test public void parserHandlesParenthesizedRouterAndIpv6() {
        assertEquals("192.0.2.3", PingOutput.parse("From router (192.0.2.3) icmp_seq=1 Time exceeded").router);
        assertEquals("2001:db8::1", PingOutput.parse("From 2001:db8::1 icmp_seq=1 Time exceeded: Hop limit").router);
    }
    @Test public void mtuTimeoutNeverProducesAnExactValue() throws Exception {
        ToolResult result = core(output(REPLY), new CoreProbes.Execution(1, "", true)).mtu(request(4), session());
        assertEquals(ToolResult.Status.PARTIAL, result.status);
        assertFalse(result.export().contains("path_mtu_bytes="));
        assertTrue(result.summary.contains("does not prove"));
    }
    @Test public void mtuAllTimeoutIsFailedWithoutNumericalMtu() throws Exception {
        ToolResult result = core(new CoreProbes.Execution(1, "", true)).mtu(request(4), session());
        assertEquals(ToolResult.Status.FAILED, result.status);
        assertTrue(result.rows.isEmpty());
    }
    @Test public void mtuUnsupportedDfOptionIsClearlyUnsupported() throws Exception {
        ToolResult result = core(new CoreProbes.Execution(2, "ping: invalid option -- M", false)).mtu(request(4), session());
        assertEquals(ToolResult.Status.UNSUPPORTED, result.status);
    }
    @Test public void mtu1500SuccessIsLowerBoundNotExactPathMtu() throws Exception {
        ToolResult result = core(output(REPLY)).mtu(request(4), session());
        assertEquals(ToolResult.Status.OK, result.status);
        assertTrue(result.export().contains(">= 1500"));
    }
    @Test public void commandsUseLiteralResolvedTargetAndNoShell() throws Exception {
        List<List<String>> commands = new ArrayList<>();
        CoreProbes probes = new CoreProbes((args, timeout, session) -> { commands.add(args); return output(REPLY); },
                (host, timeout, session) -> InetAddress.getByAddress(new byte[]{(byte)192,0,2,1}));
        probes.ping(request(1), session());
        assertEquals("/system/bin/ping", commands.get(0).get(0));
        assertEquals("192.0.2.1", commands.get(0).get(commands.get(0).size() - 1));
        assertFalse(commands.get(0).contains("sh"));
    }
    @Test(expected = InterruptedException.class) public void cancelledSessionNeverStartsProbe() throws Exception {
        ProbeSession session = session(); session.cancel();
        core(output(REPLY)).ping(request(1), session);
    }
    @Test public void publishesPerPacketSnapshots() throws Exception {
        List<ToolResult> updates = new ArrayList<>();
        core(output(REPLY)).ping(request(3), new ProbeSession(120000, updates::add));
        assertEquals(3, updates.size());
        assertEquals(1, updates.get(0).samples.size());
        assertEquals(3, updates.get(2).samples.size());
    }
    @Test public void traceHonorsRequestedHopLimit() throws Exception {
        ToolResult result = core(new CoreProbes.Execution(1, "", true)).trace(request(3), session());
        assertEquals(3, result.rows.size());
        assertEquals(ToolResult.Status.PARTIAL, result.status);
        assertTrue(result.summary.contains("3 hops"));
    }
    @Test public void explicitFragmentationBoundAllowsVerifiedMtu() throws Exception {
        CoreProbes probes = new CoreProbes((args, timeout, session) -> {
            int payload = Integer.parseInt(args.get(args.indexOf("-s") + 1));
            return payload <= 1400 ? output(REPLY) : new CoreProbes.Execution(1, "From 192.0.2.2 Frag needed and DF set", false);
        }, (host, timeout, session) -> InetAddress.getByAddress(new byte[]{(byte)192,0,2,1}));
        ToolResult result = probes.mtu(request(4), session());
        assertEquals(ToolResult.Status.OK, result.status);
        assertEquals("1428 bytes", result.rows.get(0).value);
        assertTrue(result.export().contains("explicit fragmentation error"));
    }
    @Test public void ipv6MtuDoesNotApplyIpv4HeaderMath() throws Exception {
        CoreProbes probes = new CoreProbes((args, timeout, session) -> { fail("Must not run IPv4 DF for IPv6"); return null; },
                (host, timeout, session) -> InetAddress.getByAddress(new byte[16]));
        ToolResult result = probes.mtu(request(4), session());
        assertEquals(ToolResult.Status.UNSUPPORTED, result.status);
    }
    @Test(expected = IllegalArgumentException.class) public void shellMetacharactersRejectedBeforeResolver() throws Exception {
        CoreProbes probes = new CoreProbes((args, timeout, session) -> null, (host, timeout, session) -> {
            fail("Invalid target must not reach DNS"); return null;
        });
        probes.ping(new ToolRequest("127.0.0.1;id", "", 1000, 1, false), session());
    }
}
