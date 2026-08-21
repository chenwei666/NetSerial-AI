package com.chenwei666.netserial.ai;

public interface CredentialRecordStore {
    void save(String alias, EncryptedCredential credential);

    EncryptedCredential load(String alias);

    boolean contains(String alias);

    void delete(String alias);
}
