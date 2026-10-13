package com.chenwei666.netserial.remote;

public final class TelnetFrame {
    private final byte[] payload;
    private final byte[] response;

    TelnetFrame(byte[] payload, byte[] response) {
        this.payload = payload;
        this.response = response;
    }

    public byte[] getPayload() { return payload.clone(); }
    public byte[] getResponse() { return response.clone(); }
}
