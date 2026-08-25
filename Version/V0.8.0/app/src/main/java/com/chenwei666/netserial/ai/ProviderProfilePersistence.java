package com.chenwei666.netserial.ai;

public interface ProviderProfilePersistence {
    String read();

    void write(String document);
}
