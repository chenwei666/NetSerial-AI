package com.chenwei666.netserial.network;

public final class TcpPortProbeResult {
    private final int port;
    private final boolean open;
    private final long latencyMillis;

    public TcpPortProbeResult(int port, boolean open, long latencyMillis) {
        this.port = port;
        this.open = open;
        this.latencyMillis = latencyMillis;
    }

    public int getPort() { return port; }
    public boolean isOpen() { return open; }
    public long getLatencyMillis() { return latencyMillis; }
}
