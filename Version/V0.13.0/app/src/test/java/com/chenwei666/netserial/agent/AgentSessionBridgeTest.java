package com.chenwei666.netserial.agent;

import com.chenwei666.netserial.ai.RequestCancellation;
import org.junit.Test;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import static org.junit.Assert.*;

public class AgentSessionBridgeTest {
    private AgentSessionBridge ready(AgentSessionBridge.Sender sender) {
        AgentSessionBridge b = new AgentSessionBridge(180, 5);
        b.register("device", "10.0.0.1", sender);
        b.receive("device", "login banner\r\n<SW1>");
        return b;
    }
    @Test public void readsRealOutputAndSerializesCommands() throws Exception {
        AtomicReference<AgentSessionBridge> ref = new AtomicReference<>();
        AtomicInteger sends = new AtomicInteger();
        AgentSessionBridge b = ready(command -> {
            sends.incrementAndGet();
            new Thread(() -> ref.get().receive("device", command + "\r\nComware 7\r\n<SW1>")).start();
        }); ref.set(b);
        try (AgentSessionBridge.Lease lease = b.acquire(b.sessionToken("device"))) {
            assertEquals("10.0.0.1", lease.getTarget());
            assertTrue(lease.execute("display version", new RequestCancellation()).contains("Comware 7"));
            assertTrue(lease.execute("display clock", new RequestCancellation()).contains("display clock"));
            assertEquals(2, sends.get());
        }
    }
    @Test public void reconnectInvalidatesIntentAndExistingLease() throws Exception {
        AgentSessionBridge b = ready(c -> fail("Must not send"));
        String old = b.sessionToken("device");
        AgentSessionBridge.Lease lease = b.acquire(old);
        b.register("device", "10.0.0.2", c -> fail("Must not redirect"));
        assertFalse(lease.isValid());
        try { b.acquire(old); fail(); } catch (IOException expected) { }
        try { lease.execute("show version", new RequestCancellation()); fail(); } catch (IOException expected) { }
        lease.close();
    }
    @Test public void refusesImplicitSelection() throws Exception {
        AgentSessionBridge b = ready(c -> fail());
        try { b.acquire("device"); fail(); } catch (IOException expected) { }
        try { b.acquire(null); fail(); } catch (IOException expected) { }
    }
    @Test public void exclusiveLeaseBlocksManualAndOtherLease() throws Exception {
        AgentSessionBridge b = ready(c -> fail());
        try (AgentSessionBridge.Lease lease = b.acquire(b.sessionToken("device"))) {
            try { b.acquire(b.sessionToken("device")); fail(); } catch (IOException expected) { }
            try { b.manualWrite("device", () -> fail()); fail(); } catch (IOException expected) { }
            try { b.reserveManual("device"); fail(); } catch (IOException expected) { }
        }
        b.manualWrite("device", () -> {});
    }
    @Test public void queuedManualPreventsTaskAcquisitionAndCannotCrossReconnect() throws Exception {
        AgentSessionBridge b = ready(c -> fail());
        AgentSessionBridge.ManualPermit permit = b.reserveManual("device");
        try { b.acquire(b.sessionToken("device")); fail(); } catch (IOException expected) { }
        b.register("device", "other", c -> fail());
        try { permit.write(() -> fail()); fail(); } catch (IOException expected) { }
        permit.close();
    }
    @Test public void noPromptMeansNoCommand() throws Exception {
        AgentSessionBridge b = new AgentSessionBridge(100, 5);
        b.register("device", "target", c -> fail());
        b.receive("device", "Password:");
        try (AgentSessionBridge.Lease lease = b.acquire(b.sessionToken("device"))) {
            try { lease.execute("show version", new RequestCancellation()); fail(); } catch (IOException expected) { }
        }
    }
    @Test public void timeoutTaintsAndLateOutputCannotAuthorizeAnotherTask() throws Exception {
        AgentSessionBridge b = ready(c -> {});
        String token = b.sessionToken("device");
        try (AgentSessionBridge.Lease lease = b.acquire(token)) {
            try { lease.execute("display version", new RequestCancellation()); fail(); } catch (IOException expected) { assertTrue(expected.getMessage().contains("Timed out")); }
        }
        b.receive("device", "late result\r\n<SW1>");
        try { b.acquire(token); fail(); } catch (IOException expected) { }
    }
    @Test public void interactivePagingAndErrorsStopAndTaint() throws Exception {
        for (String response : new String[] {"--More--", "[Y/N]", "Password:", "% Invalid input\r\nSW#", "Error: wrong parameter\r\n<SW1>"}) {
            AtomicReference<AgentSessionBridge> ref = new AtomicReference<>();
            AgentSessionBridge b = ready(c -> ref.get().receive("device", response)); ref.set(b);
            try (AgentSessionBridge.Lease lease = b.acquire(b.sessionToken("device"))) {
                try { lease.execute("display version", new RequestCancellation()); fail(response); } catch (IOException expected) { }
                assertFalse(lease.isValid());
            }
        }
    }
    @Test public void cancellationAndCloseStopNextWrites() throws Exception {
        AtomicInteger writes = new AtomicInteger();
        AgentSessionBridge b = ready(c -> writes.incrementAndGet());
        AgentSessionBridge.Lease lease = b.acquire(b.sessionToken("device"));
        RequestCancellation cancel = new RequestCancellation(); cancel.cancel();
        try { lease.execute("show version", cancel); fail(); } catch (IOException expected) { }
        assertEquals(0, writes.get());
        lease.close();
        try { lease.execute("show version", new RequestCancellation()); fail(); } catch (IOException expected) { }
    }
    @Test public void closeDuringExecuteTaintsSession() throws Exception {
        AtomicInteger writes = new AtomicInteger();
        AgentSessionBridge b = ready(c -> writes.incrementAndGet());
        String token = b.sessionToken("device");
        AgentSessionBridge.Lease lease = b.acquire(token);
        AtomicReference<Throwable> failure = new AtomicReference<>();
        Thread worker = new Thread(() -> {
            try { lease.execute("display version", new RequestCancellation()); }
            catch (Throwable e) { failure.set(e); }
        }); worker.start();
        long until = System.currentTimeMillis() + 1000;
        while (writes.get() == 0 && System.currentTimeMillis() < until) Thread.sleep(1);
        lease.close(); worker.join(1000);
        assertFalse(worker.isAlive()); assertNotNull(failure.get()); assertEquals(1, writes.get());
        try { b.acquire(token); fail(); } catch (IOException expected) { }
    }
    @Test public void outputIsBoundedAndRedacted() throws Exception {
        AtomicReference<AgentSessionBridge> ref = new AtomicReference<>();
        AgentSessionBridge b = ready(c -> ref.get().receive("device", "password simple fixture-secret\r\n<SW1>")); ref.set(b);
        try (AgentSessionBridge.Lease lease = b.acquire(b.sessionToken("device"))) {
            assertFalse(lease.execute("display current-configuration", new RequestCancellation()).contains("fixture-secret"));
        }
        AgentSessionBridge huge = ready(c -> {});
        huge.receive("device", new String(new char[100000]).replace('\0', 'x'));
        try (AgentSessionBridge.Lease lease = huge.acquire(huge.sessionToken("device"))) { assertTrue(lease.initialOutput().length() <= 65536); }
    }
    @Test public void rejectsMultilineAndControlBytes() throws Exception {
        AgentSessionBridge b = ready(c -> fail());
        try (AgentSessionBridge.Lease lease = b.acquire(b.sessionToken("device"))) {
            for (String command : new String[] {"show version\nreload", "\u0003", ""}) {
                try { lease.execute(command, new RequestCancellation()); fail(); } catch (IOException expected) { }
            }
        }
    }
    @Test public void supportsVendorPromptsWithoutAnsweringQuestions() throws Exception {
        for (String prompt : new String[] {"<SW1>", "[SW1]", "[*SW1-GigabitEthernet0/0/1]", "[~SW1]", "SW1#", "SW1(config-if)#"}) {
            AtomicReference<AgentSessionBridge> ref = new AtomicReference<>();
            AgentSessionBridge b = ready(c -> ref.get().receive("device", "\r\n" + prompt)); ref.set(b);
            try (AgentSessionBridge.Lease lease = b.acquire(b.sessionToken("device"))) {
                assertTrue(lease.execute("display clock", new RequestCancellation()).contains(prompt));
            }
        }
    }
    @Test public void overflowTaintsInsteadOfTruncatingSuccess() throws Exception {
        AtomicReference<AgentSessionBridge> ref = new AtomicReference<>();
        AgentSessionBridge b = ready(c -> ref.get().receive("device", new String(new char[65537]).replace('\0', 'x') + "\r\n<SW1>")); ref.set(b);
        try (AgentSessionBridge.Lease lease = b.acquire(b.sessionToken("device"))) {
            try { lease.execute("display current-configuration", new RequestCancellation()); fail(); } catch (IOException expected) { }
            assertFalse(lease.isValid());
        }
    }
    @Test public void cancelDuringResponseDoesNotPermitAnotherCommand() throws Exception {
        RequestCancellation cancellation = new RequestCancellation();
        AtomicInteger writes = new AtomicInteger();
        AgentSessionBridge b = ready(c -> { writes.incrementAndGet(); cancellation.cancel(); });
        try (AgentSessionBridge.Lease lease = b.acquire(b.sessionToken("device"))) {
            try { lease.execute("display version", cancellation); fail(); } catch (IOException expected) { }
            try { lease.execute("display clock", new RequestCancellation()); fail(); } catch (IOException expected) { }
            assertEquals(1, writes.get());
        }
    }
    @Test public void staleTransportReadCannotFeedReplacementSession() throws Exception {
        AgentSessionBridge b = ready(c -> {});
        String old = b.sessionToken("device");
        b.register("device", "replacement", c -> {});
        b.receiveToken(old, "old device result\r\n<OLD>");
        try (AgentSessionBridge.Lease lease = b.acquire(b.sessionToken("device"))) {
            assertEquals("", lease.initialOutput());
        }
    }
    @Test public void closeDuringIdleQuietWaitPreventsSend() throws Exception {
        AtomicInteger writes = new AtomicInteger();
        AgentSessionBridge b = new AgentSessionBridge(300, 100);
        b.register("device", "target", c -> writes.incrementAndGet());
        b.receive("device", "<SW1>");
        AgentSessionBridge.Lease lease = b.acquire(b.sessionToken("device"));
        Thread worker = new Thread(() -> {
            try { lease.execute("display version", new RequestCancellation()); }
            catch (Exception expected) { }
        }); worker.start();
        Thread.sleep(10);
        lease.close(); worker.join(1000);
        assertFalse(worker.isAlive()); assertEquals(0, writes.get());
    }
    @Test public void sensitiveSplitEchoNeverReachesUiHistoryOrModelAndStaysSuppressed() throws Exception {
        AtomicReference<AgentSessionBridge> ref = new AtomicReference<>();
        StringBuilder visible = new StringBuilder();
        char[] secret = "fixture-Pass-913".toCharArray();
        AgentSessionBridge b = ready(command -> {
            String token = ref.get().sessionToken("device");
            for (String chunk : new String[] {"password simple fixture-", "Pass-", "913\r\n", "[SW1-user]"}) {
                ref.get().receiveToken(token, chunk);
                visible.append(ref.get().maskIncomingToken(token, chunk));
            }
        }); ref.set(b);
        try (AgentSessionBridge.Lease lease = b.acquire(b.sessionToken("device"))) {
            String result = lease.executeSensitive("password simple fixture-Pass-913", "password simple [REDACTED]", secret, new RequestCancellation());
            assertEquals(AgentSessionBridge.SENSITIVE_OUTPUT_WITHHELD, result);
            assertTrue(lease.isOutputSuppressed());
            assertFalse(visible.toString().contains("fixture-"));
            assertFalse(visible.toString().contains("Pass-"));
            assertFalse(visible.toString().contains("913"));
            assertFalse(lease.initialOutput().contains("fixture-"));
            String later = lease.execute("display clock", new RequestCancellation());
            assertEquals(AgentSessionBridge.SENSITIVE_OUTPUT_WITHHELD, later);
            assertFalse(lease.initialOutput().contains("Pass-"));
        } finally { java.util.Arrays.fill(secret, '\0'); }
    }
    @Test public void sensitiveLateEchoAfterCompletionIsDroppedAndRequiresReconnect() throws Exception {
        AtomicReference<AgentSessionBridge> ref = new AtomicReference<>();
        AgentSessionBridge b = ready(c -> ref.get().receive("device", "\r\n<SW1>")); ref.set(b);
        String token = b.sessionToken("device");
        try (AgentSessionBridge.Lease lease = b.acquire(token)) {
            lease.executeSensitive("password simple test-abc-12", "password simple [REDACTED]", "test-abc-12".toCharArray(), new RequestCancellation());
            for (String chunk : new String[] {"test-", "abc-", "12"}) {
                b.receiveToken(token, chunk);
                assertFalse(b.maskIncomingToken(token, chunk).contains(chunk));
            }
            assertFalse(lease.initialOutput().contains("abc-"));
            try { lease.execute("display version", new RequestCancellation()); fail(); } catch (IOException expected) { }
        }
        b.register("device", "replacement", c -> {});
        assertEquals("", b.maskIncomingToken(token, "test-abc-12"));
        b.receiveToken(token, "test-abc-12\r\n<OLD>");
        try (AgentSessionBridge.Lease fresh = b.acquire(b.sessionToken("device"))) {
            assertFalse(fresh.isOutputSuppressed()); assertEquals("", fresh.initialOutput());
        }
    }
    @Test public void sensitiveTransportExceptionHasNoRawMessageOrCause() throws Exception {
        AgentSessionBridge b = ready(c -> { throw new IOException("Device echo: sensitive-fixture-55"); });
        try (AgentSessionBridge.Lease lease = b.acquire(b.sessionToken("device"))) {
            try {
                lease.executeSensitive("password simple sensitive-fixture-55", "password simple [REDACTED]", "sensitive-fixture-55".toCharArray(), new RequestCancellation()); fail();
            } catch (IOException expected) {
                assertFalse(expected.toString().contains("sensitive-fixture-55")); assertNull(expected.getCause());
            }
            assertFalse(lease.isValid()); assertTrue(lease.isOutputSuppressed());
            assertFalse(lease.initialOutput().contains("sensitive-fixture-55"));
        }
    }
    @Test public void sensitiveCancellationTaintsAndMasksEvenAfterLeaseClose() throws Exception {
        RequestCancellation cancel = new RequestCancellation();
        AgentSessionBridge b = ready(c -> cancel.cancel());
        String token = b.sessionToken("device");
        try (AgentSessionBridge.Lease lease = b.acquire(token)) {
            try { lease.executeSensitive("password simple secret-33", "password simple [REDACTED]", "secret-33".toCharArray(), cancel); fail(); }
            catch (IOException expected) { assertFalse(expected.toString().contains("secret-33")); }
            assertFalse(lease.isValid());
        }
        b.receiveToken(token, "secret-33");
        assertFalse(b.maskIncomingToken(token, "secret-33").contains("secret-33"));
        assertNull(b.sessionToken("device"));
    }
    @Test public void registryIsBoundedAndNeverEvictsLiveLease() throws Exception {
        AgentSessionBridge b = ready(c -> {});
        String first = b.sessionToken("device");
        try (AgentSessionBridge.Lease lease = b.acquire(first)) {
            for (int i = 0; i < 30; i++) b.register("device-" + i, "target", c -> {});
            assertTrue(lease.isValid());
            assertNull(b.sessionToken("device-0"));
            assertNotNull(b.sessionToken("device-29"));
        }
    }
}
