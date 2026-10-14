package com.chenwei666.netserial.guided.network;

import com.chenwei666.netserial.guided.DiagnosticFinding;
import com.chenwei666.netserial.guided.DiagnosticFinding.Status;
import com.chenwei666.netserial.guided.GuidedScenario;

import org.junit.Test;

import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.*;

public class NetworkDiagnosisRunnerTest {
    @Test public void probesOnlyFirstResolvedAddressAndSelectedPort() throws Exception {
        FakeProbe probe = new FakeProbe();
        List<Integer> progress = new ArrayList<>();
        List<DiagnosticFinding> result = new NetworkDiagnosisRunner(probe).run(
                GuidedScenario.NO_INTERNET, "example.test", "443",
                (completed, total, message) -> { assertEquals(5, total); progress.add(completed); });
        assertEquals(1, probe.resolveCalls);
        assertEquals(3, probe.pingCalls);
        assertEquals(1, probe.tcpCalls);
        assertEquals(443, probe.port);
        for (InetAddress address : probe.targets) assertEquals("192.0.2.1", address.getHostAddress());
        assertEquals(Integer.valueOf(0), progress.get(0));
        assertEquals(Integer.valueOf(5), progress.get(progress.size() - 1));
        assertEquals(Status.PASS, result.get(0).getStatus());
        assertEquals(Status.PASS, result.get(1).getStatus());
        assertEquals(Status.PASS, result.get(2).getStatus());
        assertEquals(Status.UNKNOWN, result.get(3).getStatus());
        assertTrue(result.get(3).getExplanation().get(true).contains("not the switch"));
    }

    @Test public void numericIpv4SkipsDnsAndWarnsDnsScenarioCannotBeTested() throws Exception {
        FakeProbe probe = new FakeProbe();
        List<DiagnosticFinding> result = run(probe, GuidedScenario.DNS_FAILURE, "192.0.2.2", "53");
        assertEquals(0, probe.resolveCalls);
        assertEquals(Status.WARNING, result.get(0).getStatus());
        assertTrue(result.get(0).getExplanation().get(true).contains("cannot assess DNS"));
    }

    @Test public void numericIpv6SkipsDns() throws Exception {
        FakeProbe probe = new FakeProbe();
        run(probe, GuidedScenario.NO_INTERNET, "2001:db8::1", "443");
        assertEquals(0, probe.resolveCalls);
        assertEquals(3, probe.pingCalls);
        assertEquals(16, probe.targets.get(0).getAddress().length);
    }

    @Test public void noIcmpReplyDoesNotDeclareDeviceOfflineWhenTcpWorks() throws Exception {
        FakeProbe probe = new FakeProbe();
        probe.replies = new boolean[] {false, false, false};
        List<DiagnosticFinding> result = run(probe, GuidedScenario.NO_INTERNET, "192.0.2.2", "443");
        assertEquals(Status.WARNING, result.get(1).getStatus());
        assertTrue(result.get(1).getExplanation().get(true).contains("does not prove"));
        assertEquals(Status.PASS, result.get(2).getStatus());
    }

    @Test public void mixedRepliesDoNotReportLongTermPacketLossRate() throws Exception {
        FakeProbe probe = new FakeProbe();
        probe.replies = new boolean[] {true, false, true};
        List<DiagnosticFinding> result = run(probe, GuidedScenario.SLOW_NETWORK, "192.0.2.2", "443");
        assertEquals(Status.WARNING, result.get(1).getStatus());
        assertTrue(result.get(1).getEvidence().contains("replies=2/3"));
        assertTrue(result.get(1).getExplanation().get(true).contains("cannot establish long-term"));
        assertEquals(Status.UNKNOWN, result.get(3).getStatus());
        assertTrue(result.get(3).getExplanation().get(true).contains("do not measure bandwidth"));
        assertFalse(result.get(3).getEvidence().contains("latency_ms"));
    }

    @Test public void unavailablePingStillRunsTcpWithoutFalseFailure() throws Exception {
        FakeProbe probe = new FakeProbe();
        probe.pingError = new IOException("not permitted");
        List<DiagnosticFinding> result = run(probe, GuidedScenario.NO_INTERNET, "192.0.2.2", "443");
        assertEquals(Status.UNKNOWN, result.get(1).getStatus());
        assertEquals(Status.PASS, result.get(2).getStatus());
    }

    @Test public void failedTcpIsNotAuthenticationFailure() throws Exception {
        FakeProbe probe = new FakeProbe();
        probe.tcpError = new SocketTimeoutException();
        List<DiagnosticFinding> result = run(probe, GuidedScenario.SWITCH_LOGIN, "192.0.2.2", "22");
        assertEquals(Status.FAIL, result.get(2).getStatus());
        assertTrue(result.get(2).getExplanation().get(true).contains("not evidence of a wrong password"));
        assertTrue(result.get(2).getEvidence().contains("timeout"));
    }

    @Test public void successfulTcpDoesNotClaimCredentialsWereChecked() throws Exception {
        List<DiagnosticFinding> result = run(new FakeProbe(), GuidedScenario.SWITCH_LOGIN, "192.0.2.2", "22");
        assertTrue(result.get(2).getExplanation().get(true).contains("were not tested"));
        assertTrue(result.get(2).getNextStep().get(true).contains("authentication failures"));
    }

    @Test public void dnsFailureStopsWithoutProbingAnotherDestination() throws Exception {
        FakeProbe probe = new FakeProbe();
        probe.dnsError = new UnknownHostException("example.test");
        List<DiagnosticFinding> result = run(probe, GuidedScenario.DNS_FAILURE, "example.test", "443");
        assertEquals(1, result.size());
        assertEquals(Status.FAIL, result.get(0).getStatus());
        assertEquals(0, probe.pingCalls);
        assertEquals(0, probe.tcpCalls);
        assertTrue(result.get(0).getExplanation().get(true).contains("does not establish the root cause"));
    }

    @Test public void dnsTimeoutReportsUnknownNotBrokenInternet() throws Exception {
        FakeProbe probe = new FakeProbe();
        probe.dnsError = new SocketTimeoutException();
        List<DiagnosticFinding> result = run(probe, GuidedScenario.NO_INTERNET, "example.test", "443");
        assertEquals(Status.UNKNOWN, result.get(0).getStatus());
        assertEquals(0, probe.pingCalls);
        assertEquals(0, probe.tcpCalls);
    }

    @Test public void validatesTargetAndPortBeforeAnyProbe() throws Exception {
        String[] targets = {"https://example.test", "example.test;reboot", "-f", "192.0.2.256", "1.2.3", ":", "::", "224.0.0.1", "255.255.255.255"};
        for (String target : targets) {
            FakeProbe probe = new FakeProbe();
            try { run(probe, GuidedScenario.NO_INTERNET, target, "443"); fail(target); }
            catch (IllegalArgumentException expected) { }
            assertEquals(0, probe.resolveCalls + probe.pingCalls + probe.tcpCalls);
        }
        for (String port : new String[] {null, "0", "65536", "22,23", "443x"}) {
            FakeProbe probe = new FakeProbe();
            try { run(probe, GuidedScenario.NO_INTERNET, "example.test", port); fail(String.valueOf(port)); }
            catch (IllegalArgumentException expected) { }
            assertEquals(0, probe.resolveCalls + probe.pingCalls + probe.tcpCalls);
        }
    }

    @Test public void refusesNonNetworkScenario() throws Exception {
        FakeProbe probe = new FakeProbe();
        try { run(probe, GuidedScenario.PORT_LINK, "example.test", "443"); fail(); }
        catch (IllegalArgumentException expected) { }
        assertEquals(0, probe.resolveCalls);
    }

    @Test public void cancellationDuringProbeStopsAllLaterProbesAndReleasesAdapter() throws Exception {
        FakeProbe probe = new FakeProbe();
        probe.blockPing = true;
        NetworkDiagnosisRunner runner = new NetworkDiagnosisRunner(probe);
        AtomicBoolean interrupted = new AtomicBoolean();
        Thread worker = new Thread(() -> {
            try { runner.run(GuidedScenario.NO_INTERNET, "192.0.2.2", "443", null); }
            catch (InterruptedException expected) { interrupted.set(true); }
        });
        worker.start();
        assertTrue(probe.pingStarted.await(2, TimeUnit.SECONDS));
        runner.cancel();
        worker.join(2000);
        assertFalse(worker.isAlive());
        assertTrue(interrupted.get());
        assertEquals(1, probe.pingCalls);
        assertEquals(0, probe.tcpCalls);
        assertTrue(probe.cancelCalls >= 1);
    }

    @Test public void cancellationFromProgressPreventsNextProbe() throws Exception {
        FakeProbe probe = new FakeProbe();
        NetworkDiagnosisRunner runner = new NetworkDiagnosisRunner(probe);
        try {
            runner.run(GuidedScenario.NO_INTERNET, "example.test", "443", (completed, total, message) -> {
                if (completed == 1) runner.cancel();
            });
            fail();
        } catch (InterruptedException expected) {
            assertEquals(0, probe.pingCalls);
            assertEquals(0, probe.tcpCalls);
        } finally {
            Thread.interrupted();
        }
    }

    @Test public void cancellationBeforeQueuedRunStartsPreventsEveryProbe() throws Exception {
        FakeProbe probe = new FakeProbe();
        NetworkDiagnosisRunner runner = new NetworkDiagnosisRunner(probe);
        runner.cancel();
        try {
            runner.run(GuidedScenario.NO_INTERNET, "example.test", "443", null);
            fail("A cancelled queued diagnosis must not start");
        } catch (InterruptedException expected) {
            assertEquals(0, probe.resolveCalls + probe.pingCalls + probe.tcpCalls);
        }
        assertFalse(Thread.currentThread().isInterrupted());
    }

    private static List<DiagnosticFinding> run(FakeProbe probe, GuidedScenario scenario, String host, String port)
            throws InterruptedException {
        return new NetworkDiagnosisRunner(probe).run(scenario, host, port, null);
    }

    private static final class FakeProbe implements NetworkDiagnosisRunner.ProbeAdapter {
        int resolveCalls, pingCalls, tcpCalls, port, cancelCalls;
        boolean[] replies = {true, true, true};
        IOException dnsError, pingError, tcpError;
        List<InetAddress> targets = new ArrayList<>();
        boolean blockPing;
        CountDownLatch pingStarted = new CountDownLatch(1);

        @Override public InetAddress[] resolve(String host, int timeoutMillis) throws IOException {
            resolveCalls++;
            assertTrue(timeoutMillis > 0 && timeoutMillis <= 3000);
            if (dnsError != null) throw dnsError;
            return new InetAddress[] {InetAddress.getByAddress(new byte[] {(byte) 192, 0, 2, 1}),
                    InetAddress.getByAddress(new byte[] {(byte) 192, 0, 2, 2})};
        }

        @Override public boolean reachable(InetAddress address, int timeoutMillis)
                throws IOException, InterruptedException {
            targets.add(address);
            pingCalls++;
            assertTrue(timeoutMillis > 0 && timeoutMillis <= 1500);
            pingStarted.countDown();
            if (blockPing) new CountDownLatch(1).await();
            if (pingError != null) throw pingError;
            return replies[pingCalls - 1];
        }

        @Override public boolean connect(InetAddress address, int port, int timeoutMillis) throws IOException {
            targets.add(address);
            tcpCalls++;
            this.port = port;
            assertTrue(timeoutMillis > 0 && timeoutMillis <= 2000);
            if (tcpError != null) throw tcpError;
            return true;
        }

        @Override public void cancel() { cancelCalls++; }
    }
}
