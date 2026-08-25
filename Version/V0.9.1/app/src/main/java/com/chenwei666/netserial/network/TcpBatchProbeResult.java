package com.chenwei666.netserial.network;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class TcpBatchProbeResult {
    private final String host;
    private final int timeoutMillis;
    private final List<TcpPortProbeResult> ports;

    public TcpBatchProbeResult(String host, int timeoutMillis, List<TcpPortProbeResult> ports) {
        this.host = Objects.requireNonNull(host, "host");
        this.timeoutMillis = timeoutMillis;
        this.ports = Collections.unmodifiableList(new ArrayList<>(
                Objects.requireNonNull(ports, "ports")));
    }

    public String getHost() { return host; }
    public int getTimeoutMillis() { return timeoutMillis; }
    public List<TcpPortProbeResult> getPorts() { return ports; }
}
