package com.chenwei666.netserial.remote;

public interface RemoteConnection {
    void connect(char[] password);
    void send(byte[] data);
    /** Synchronous worker-thread write for a leased AI task. Must not enqueue stale commands. */
    default void sendNow(byte[] data) throws Exception { throw new UnsupportedOperationException("Synchronous send unavailable"); }
    void disconnect();
    RemoteConnectionState getState();
}
