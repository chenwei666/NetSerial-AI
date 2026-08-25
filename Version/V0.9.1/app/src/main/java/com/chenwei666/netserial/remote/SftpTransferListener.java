package com.chenwei666.netserial.remote;

public interface SftpTransferListener {
    void onComplete();
    void onError(String safeMessage);
}
