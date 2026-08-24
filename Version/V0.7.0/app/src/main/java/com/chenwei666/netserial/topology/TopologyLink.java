package com.chenwei666.netserial.topology;

import java.util.Objects;

public final class TopologyLink {
    private final String localNode;
    private final String localPort;
    private final String remoteNode;
    private final String remotePort;

    public TopologyLink(String localNode, String localPort, String remoteNode, String remotePort) {
        this.localNode = require(localNode);
        this.localPort = require(localPort);
        this.remoteNode = require(remoteNode);
        this.remotePort = require(remotePort);
    }
    public String getLocalNode() { return localNode; }
    public String getLocalPort() { return localPort; }
    public String getRemoteNode() { return remoteNode; }
    public String getRemotePort() { return remotePort; }

    private static String require(String value) {
        String result = Objects.requireNonNull(value, "value").trim();
        if (result.isEmpty() || result.length() > 128) throw new IllegalArgumentException("Invalid link");
        return result;
    }
}
