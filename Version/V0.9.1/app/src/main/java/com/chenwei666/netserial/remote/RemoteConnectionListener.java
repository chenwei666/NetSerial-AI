package com.chenwei666.netserial.remote;

public interface RemoteConnectionListener {
    void onStateChanged(RemoteConnectionState state, String detail);
    void onTextReceived(String text);
    void onError(String safeMessage);

    /** Called on a worker thread. Implementations may block while showing a confirmation UI. */
    boolean confirmUnknownSshHost(String verificationMessage);
}
