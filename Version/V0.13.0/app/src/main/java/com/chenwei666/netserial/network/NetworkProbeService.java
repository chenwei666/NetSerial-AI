package com.chenwei666.netserial.network;

import java.net.InetAddress;
import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public final class NetworkProbeService {
    private final NetworkTargetValidator validator = new NetworkTargetValidator();
    private final AtomicReference<Socket> activeSocket = new AtomicReference<>();
    private final AtomicReference<com.chenwei666.netserial.network.toolbox.ProbeSession> activeCore = new AtomicReference<>();

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
        Socket socket = registerSocket();
        try (Socket managedSocket = socket) {
            managedSocket.connect(new InetSocketAddress(address, port), boundedTimeout(timeoutMillis));
        } finally {
            activeSocket.compareAndSet(socket, null);
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
            Socket socket = registerSocket();
            try (Socket managedSocket = socket) {
                managedSocket.connect(new InetSocketAddress(address, port), perPortTimeout);
                open = true;
            } catch (IOException ignored) {
                throwIfInterrupted();
                // A refused or timed-out connection is reported as CLOSED/FILTERED.
            } finally {
                activeSocket.compareAndSet(socket, null);
            }
            long elapsed = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - started);
            results.add(new TcpPortProbeResult(port, open, elapsed));
        }
        return new TcpBatchProbeResult(host, perPortTimeout, results);
    }

    /** Cancels a currently blocking TCP connect. The worker interruption stops any remaining batch. */
    public void cancelActiveProbe() {
        com.chenwei666.netserial.network.toolbox.ProbeSession core = activeCore.getAndSet(null);
        if (core != null) core.cancel();
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
        return coreProbe(target, timeoutMillis, 4, new com.chenwei666.netserial.network.toolbox.CoreProbes()::ping);
    }

    public String traceroute(String target, int timeoutMillis) throws Exception {
        return coreProbe(target, timeoutMillis, 20, new com.chenwei666.netserial.network.toolbox.CoreProbes()::trace);
    }

    public String pathMtu(String target, int timeoutMillis) throws Exception {
        return coreProbe(target, timeoutMillis, 4, new com.chenwei666.netserial.network.toolbox.CoreProbes()::mtu);
    }

    private interface CoreAction {
        com.chenwei666.netserial.network.toolbox.ToolResult run(
                com.chenwei666.netserial.network.toolbox.ToolRequest request,
                com.chenwei666.netserial.network.toolbox.ProbeSession session) throws Exception;
    }

    private String coreProbe(String target, int timeout, int count, CoreAction action) throws Exception {
        throwIfInterrupted();
        com.chenwei666.netserial.network.toolbox.ToolRequest request = new com.chenwei666.netserial.network.toolbox.ToolRequest(
                validator.validate(target), "", boundedTimeout(timeout), count, Locale.getDefault().getLanguage().equals("zh"));
        com.chenwei666.netserial.network.toolbox.ProbeSession session = new com.chenwei666.netserial.network.toolbox.ProbeSession(120000, null);
        if (!activeCore.compareAndSet(null, session)) throw new IllegalStateException("another ICMP probe is already active");
        try { return action.run(request, session).export(); }
        finally { activeCore.compareAndSet(session, null); session.cancel(); }
    }

    private static int boundedTimeout(int timeoutMillis) {
        if (timeoutMillis < 1_000 || timeoutMillis > 15_000) {
            throw new IllegalArgumentException("timeout must be 1 to 15 seconds");
        }
        return timeoutMillis;
    }

}
