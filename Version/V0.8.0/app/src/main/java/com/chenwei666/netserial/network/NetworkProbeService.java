package com.chenwei666.netserial.network;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public final class NetworkProbeService {
    private static final int MAX_OUTPUT_LINES = 80;
    private final NetworkTargetValidator validator = new NetworkTargetValidator();
    private final AtomicReference<Socket> activeSocket = new AtomicReference<>();

    public String dns(String target) throws Exception {
        String host = validator.validate(target);
        StringBuilder result = new StringBuilder();
        for (InetAddress address : InetAddress.getAllByName(host)) {
            if (result.length() > 0) result.append('\n');
            result.append(address.getHostAddress());
        }
        return result.toString();
    }

    public String tcp(String target, String portText, int timeoutMillis) throws Exception {
        String host = validator.validate(target);
        int port = validator.validatePort(portText);
        long started = System.nanoTime();
        InetAddress address = InetAddress.getByName(host);
        throwIfInterrupted();
        try (Socket socket = registerSocket()) {
            socket.connect(new InetSocketAddress(address, port), boundedTimeout(timeoutMillis));
        } finally {
            activeSocket.set(null);
        }
        long elapsed = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - started);
        return String.format(Locale.ROOT, "TCP %s:%d\nstatus=OK\nlatency_ms=%d", host, port, elapsed);
    }

    public TcpBatchProbeResult tcpBatch(String target, String portText, int timeoutMillis) throws Exception {
        String host = validator.validate(target);
        List<Integer> ports = new PortBatchParser().parse(portText);
        InetAddress address = InetAddress.getByName(host);
        int perPortTimeout = Math.min(boundedTimeout(timeoutMillis), 1_500);
        List<TcpPortProbeResult> results = new ArrayList<>();
        for (int port : ports) {
            throwIfInterrupted();
            long started = System.nanoTime();
            boolean open = false;
            try (Socket socket = registerSocket()) {
                socket.connect(new InetSocketAddress(address, port), perPortTimeout);
                open = true;
            } catch (IOException ignored) {
                throwIfInterrupted();
                // A refused or timed-out connection is reported as CLOSED/FILTERED.
            } finally {
                activeSocket.set(null);
            }
            long elapsed = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - started);
            results.add(new TcpPortProbeResult(port, open, elapsed));
        }
        return new TcpBatchProbeResult(host, perPortTimeout, results);
    }

    /** Cancels a currently blocking TCP connect. The worker interruption stops any remaining batch. */
    public void cancelActiveProbe() {
        Socket socket = activeSocket.getAndSet(null);
        if (socket == null) return;
        try {
            socket.close();
        } catch (IOException ignored) {
        }
    }

    private Socket registerSocket() throws InterruptedException {
        throwIfInterrupted();
        Socket socket = new Socket();
        if (!activeSocket.compareAndSet(null, socket)) {
            try {
                socket.close();
            } catch (IOException ignored) {
            }
            throw new IllegalStateException("another TCP probe is already active");
        }
        if (Thread.currentThread().isInterrupted()) {
            cancelActiveProbe();
            throw new InterruptedException("network probe was cancelled");
        }
        return socket;
    }

    private static void throwIfInterrupted() throws InterruptedException {
        if (Thread.currentThread().isInterrupted()) {
            throw new InterruptedException("network probe was cancelled");
        }
    }

    public AddressProbeResult addressSummary(String target) throws Exception {
        String host = validator.validate(target);
        List<AddressProbeEntry> results = new ArrayList<>();
        for (InetAddress address : InetAddress.getAllByName(host)) {
            results.add(new AddressProbeEntry(
                    address.getHostAddress(), address instanceof Inet4Address ? "IPv4" : "IPv6",
                    address.isLoopbackAddress(), address.isLinkLocalAddress(),
                    address.isSiteLocalAddress(), address.isMulticastAddress(),
                    address.isAnyLocalAddress()));
        }
        return new AddressProbeResult(host, results);
    }

    public String ping(String target, int timeoutMillis) throws Exception {
        String host = validator.validate(target);
        long started = System.nanoTime();
        int timeout = boundedTimeout(timeoutMillis);
        Boolean icmpResult = systemPing(host, timeout);
        boolean reachable = icmpResult != null
                ? icmpResult
                : InetAddress.getByName(host).isReachable(timeout);
        long elapsed = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - started);
        String method = icmpResult == null ? "ANDROID_REACHABILITY" : "ICMP";
        return String.format(Locale.ROOT, "%s %s\nstatus=%s\nlatency_ms=%d\ntimeout_ms=%d",
                method, host, reachable ? "OK" : "TIMEOUT", elapsed, timeout);
    }

    public String traceroute(String target, int timeoutMillis) throws Exception {
        String host = validator.validate(target);
        int seconds = Math.max(1, boundedTimeout(timeoutMillis) / 1000);
        List<String> command = new ArrayList<>();
        command.add("/system/bin/toybox");
        command.add("traceroute");
        command.add("-m");
        command.add("20");
        command.add("-w");
        command.add(String.valueOf(seconds));
        command.add(host);
        Process process = new ProcessBuilder(command).redirectErrorStream(true).start();
        if (!waitFor(process, 45_000L)) {
            process.destroy();
            throw new IllegalStateException("traceroute timed out");
        }
        StringBuilder output = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                process.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            int count = 0;
            while ((line = reader.readLine()) != null && count++ < MAX_OUTPUT_LINES) {
                output.append(line).append('\n');
            }
        }
        if (process.exitValue() != 0 && output.length() == 0) {
            throw new IllegalStateException("traceroute is unavailable on this Android device");
        }
        return output.toString().trim();
    }

    public String pathMtu(String target, int timeoutMillis) throws Exception {
        String host = validator.validate(target);
        int low = 1_200;
        int high = 1_472;
        int best = -1;
        while (low <= high) {
            int payload = (low + high) >>> 1;
            if (pingPayload(host, payload, timeoutMillis)) {
                best = payload;
                low = payload + 1;
            } else {
                high = payload - 1;
            }
        }
        if (best < 0) throw new IllegalStateException("DF ping is unavailable or the target is unreachable");
        return "ICMP " + host + "\npayload_bytes=" + best + "\npath_mtu_bytes=" + (best + 28);
    }

    private boolean pingPayload(String host, int payload, int timeoutMillis) {
        int seconds = Math.max(1, boundedTimeout(timeoutMillis) / 1000);
        Process process = null;
        try {
            process = new ProcessBuilder("/system/bin/ping", "-c", "1", "-W", String.valueOf(seconds),
                    "-M", "do", "-s", String.valueOf(payload), host)
                    .redirectErrorStream(true).start();
            if (!waitFor(process, timeoutMillis + 2_000L)) {
                process.destroy();
                return false;
            }
            return process.exitValue() == 0;
        } catch (Exception exception) {
            if (process != null) process.destroy();
            return false;
        }
    }

    /** Returns null when the Android ping binary is unavailable. */
    private Boolean systemPing(String host, int timeoutMillis) throws InterruptedException {
        int seconds = Math.max(1, timeoutMillis / 1_000);
        Process process = null;
        try {
            process = new ProcessBuilder("/system/bin/ping", "-c", "1", "-W",
                    String.valueOf(seconds), host).redirectErrorStream(true).start();
            if (!waitFor(process, timeoutMillis + 2_000L)) {
                process.destroy();
                return false;
            }
            return process.exitValue() == 0;
        } catch (java.io.IOException unavailable) {
            if (process != null) process.destroy();
            return null;
        } finally {
            if (process != null) process.destroy();
        }
    }

    private static int boundedTimeout(int timeoutMillis) {
        if (timeoutMillis < 1_000 || timeoutMillis > 15_000) {
            throw new IllegalArgumentException("timeout must be 1 to 15 seconds");
        }
        return timeoutMillis;
    }

    private static boolean waitFor(Process process, long timeoutMillis) throws InterruptedException {
        long deadline = System.currentTimeMillis() + timeoutMillis;
        while (System.currentTimeMillis() < deadline) {
            try {
                process.exitValue();
                return true;
            } catch (IllegalThreadStateException stillRunning) {
                Thread.sleep(100);
            }
        }
        return false;
    }
}
