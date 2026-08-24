package com.chenwei666.netserial.ai;

/**
 * Stores provider credentials and exposes plaintext only inside a short-lived callback.
 */
public interface CredentialVault {
    void store(String alias, char[] credential);

    boolean contains(String alias);

    <T> T withCredential(String alias, CredentialOperation<T> operation);

    void delete(String alias);
}
