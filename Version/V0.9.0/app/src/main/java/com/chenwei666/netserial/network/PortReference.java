package com.chenwei666.netserial.network;

public final class PortReference {
    private final int port;
    private final String protocol;
    private final String purpose;

    PortReference(int port, String protocol, String purpose) {
        this.port = port;
        this.protocol = protocol;
        this.purpose = purpose;
    }

    public int getPort() { return port; }
    public String getProtocol() { return protocol; }
    public String getPurpose() { return purpose; }
}
