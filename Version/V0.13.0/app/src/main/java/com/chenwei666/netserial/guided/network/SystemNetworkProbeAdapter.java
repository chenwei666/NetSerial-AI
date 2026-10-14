package com.chenwei666.netserial.guided.network;

import java.io.Closeable;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/** Platform probes use a previously resolved address and own every cancellable resource. */
final class SystemNetworkProbeAdapter implements NetworkDiagnosisRunner.ProbeAdapter {
    private final BoundedDnsResolver resolver = new BoundedDnsResolver();
    private final AtomicReference<Socket> activeSocket = new AtomicReference<>();
    private final AtomicReference<Process> activeProcess = new AtomicReference<>();

    @Override public InetAddress[] resolve(String host, int timeoutMillis)
            throws IOException, InterruptedException {
        return resolver.resolve(host, timeoutMillis);
    }

    @Override public boolean reachable(InetAddress address, int timeoutMillis)
            throws IOException, InterruptedException {
        checkInterrupted();
        String executable = address instanceof Inet4Address ? "/system/bin/ping" : "/system/bin/ping6";
        Process process = new ProcessBuilder(executable, "-n", "-c", "1", "-W",
                String.valueOf(Math.max(1, (timeoutMillis + 999) / 1000)), address.getHostAddress())
                .redirectErrorStream(true).start();
        if (!activeProcess.compareAndSet(null, process)) {
            dispose(process);
            throw new IOException("another reachability probe is running");
        }
        try {
            long deadline = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(timeoutMillis);
            while (true) {
                checkInterrupted();
                try {
                    int exitCode = process.exitValue();
                    if (exitCode > 1) throw new IOException("Ping is unavailable or not permitted");
                    return exitCode == 0;
                } catch (IllegalThreadStateException running) {
                    if (System.nanoTime() >= deadline) return false;
                    Thread.sleep(25);
                }
            }
        } finally {
            activeProcess.compareAndSet(process, null);
            dispose(process);
        }
    }

    @Override public boolean connect(InetAddress address, int port, int timeoutMillis)
            throws IOException, InterruptedException {
        checkInterrupted();
        Socket socket = new Socket();
        if (!activeSocket.compareAndSet(null, socket)) {
            close(socket);
            throw new IOException("another TCP probe is running");
        }
        try {
            checkInterrupted();
            socket.connect(new InetSocketAddress(address, port), timeoutMillis);
            checkInterrupted();
            return true;
        } finally {
            activeSocket.compareAndSet(socket, null);
            close(socket);
        }
    }

    @Override public void cancel() {
        close(activeSocket.getAndSet(null));
        dispose(activeProcess.getAndSet(null));
    }

    private static void checkInterrupted() throws InterruptedException {
        if (Thread.currentThread().isInterrupted()) throw new InterruptedException("cancelled");
    }

    private static void close(Closeable closeable) {
        if (closeable == null) return;
        try { closeable.close(); } catch (IOException ignored) { }
    }

    private static void dispose(Process process) {
        if (process == null) return;
        process.destroy();
        close(process.getInputStream());
        close(process.getErrorStream());
        close(process.getOutputStream());
    }
}
