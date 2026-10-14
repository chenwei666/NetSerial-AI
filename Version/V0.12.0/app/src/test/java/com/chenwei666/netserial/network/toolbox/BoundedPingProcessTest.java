package com.chenwei666.netserial.network.toolbox;

import org.junit.Test;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import static org.junit.Assert.*;

/** Local child JVM only: tests process management without invoking any network probes. */
public class BoundedPingProcessTest {
    public static class Child {
        public static void main(String[] args) throws Exception {
            if (args[0].equals("flood")) {
                for (int i = 0; i < 65536; i++) System.out.print('x');
            } else Thread.sleep(10000);
        }
    }
    private List<String> command(String mode) throws Exception {
        String executable = new File(new File(System.getProperty("java.home"), "bin"),
                System.getProperty("os.name").startsWith("Windows") ? "java.exe" : "java").getPath();
        String location = new File(Child.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getPath();
        return Arrays.asList(executable, "-cp", location, Child.class.getName(), mode);
    }
    @Test public void outputDrainIsBoundedAndDoesNotDeadlock() throws Exception {
        CoreProbes.Execution result = new BoundedPingProcess().run(command("flood"), 5000, new ProbeSession(6000, null));
        assertFalse(result.timedOut);
        assertEquals(0, result.exit);
        assertEquals(32768, result.raw.length());
    }
    @Test public void timeoutDestroysSlowChildPromptly() throws Exception {
        long started = System.nanoTime();
        CoreProbes.Execution result = new BoundedPingProcess().run(command("sleep"), 150, new ProbeSession(6000, null));
        assertTrue(result.timedOut);
        assertTrue((System.nanoTime() - started) / 1000000 < 2500);
    }
    @Test public void cancellationClosesChildAndWorkerExits() throws Exception {
        ProbeSession session = new ProbeSession(6000, null);
        AtomicReference<Throwable> outcome = new AtomicReference<>();
        List<String> args = command("sleep");
        Thread worker = new Thread(() -> {
            try { new BoundedPingProcess().run(args, 5000, session); }
            catch (Throwable error) { outcome.set(error); }
        });
        worker.start();
        Thread.sleep(150);
        session.cancel();
        worker.interrupt();
        worker.join(2500);
        assertFalse(worker.isAlive());
        assertTrue(outcome.get() instanceof InterruptedException);
    }
}
