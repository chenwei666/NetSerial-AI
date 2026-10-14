package com.chenwei666.netserial.remote;

public enum RemoteProtocol {
    SSH(22),
    TELNET(23);

    private final int defaultPort;

    RemoteProtocol(int defaultPort) { this.defaultPort = defaultPort; }
    public int getDefaultPort() { return defaultPort; }
}
