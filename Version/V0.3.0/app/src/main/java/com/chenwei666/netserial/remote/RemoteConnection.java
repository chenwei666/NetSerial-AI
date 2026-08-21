package com.chenwei666.netserial.remote;

public interface RemoteConnection {
    void connect(char[] password);
    void send(byte[] data);
    void disconnect();
    RemoteConnectionState getState();
}
