package com.chenwei666.netserial.network.toolbox;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.TimeUnit;

/** Dedicated merged-output drain prevents a full stderr/stdout pipe from blocking the process. */
final class BoundedPingProcess implements CoreProbes.Runner {
    private static final int MAX_OUTPUT = 32768;

    @Override public CoreProbes.Execution run(List<String> command, int timeoutMs, ProbeSession session) throws Exception {
        session.check();
        ProcessBuilder builder = new ProcessBuilder(command).redirectErrorStream(true);
        builder.environment().put("LC_ALL", "C");
        Process process = builder.start();
        Closeable cleanup = () -> {
            process.destroy();
            close(process.getInputStream()); close(process.getErrorStream()); close(process.getOutputStream());
        };
        session.track(cleanup);
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        Thread reader = new Thread(() -> {
            byte[] block = new byte[1024];
            try {
                int read;
                while ((read = process.getInputStream().read(block)) >= 0) {
                    synchronized (bytes) {
                        int keep = Math.min(read, MAX_OUTPUT - bytes.size());
                        if (keep > 0) bytes.write(block, 0, keep);
                    }
                }
            } catch (IOException ignored) { /* Cancellation closes this stream. */ }
        }, "toolbox-ping-output");
        reader.setDaemon(true);
        reader.start();
        boolean timeout = false;
        int exit = -1;
        try {
            long deadline = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(session.remainingMs(timeoutMs));
            while (true) {
                session.check();
                try { exit = process.exitValue(); break; }
                catch (IllegalThreadStateException running) {
                    if (System.nanoTime() >= deadline) { timeout = true; break; }
                    Thread.sleep(25);
                }
            }
            if (!timeout) reader.join(200);
            synchronized (bytes) {
                return new CoreProbes.Execution(exit, new String(bytes.toByteArray(), StandardCharsets.UTF_8), timeout);
            }
        } finally {
            cleanup.close(); session.release(cleanup);
        }
    }

    private static void close(Closeable value) { try { value.close(); } catch (IOException ignored) { } }
}
