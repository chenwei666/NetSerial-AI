package com.chenwei666.netserial.ai;

/**
 * Fail-closed credential vault used on Android versions without a supported
 * platform keystore. It lets credential-free providers such as Ollama remain
 * usable without ever falling back to plaintext credential storage.
 */
public final class UnavailableCredentialVault implements CredentialVault {
    private static final String MESSAGE =
            "AI credential storage requires Android 6.0 or newer";

    @Override
    public void store(String alias, char[] credential) {
        throw unavailable();
    }

    @Override
    public boolean contains(String alias) {
        return false;
    }

    @Override
    public <T> T withCredential(String alias, CredentialOperation<T> operation) {
        throw unavailable();
    }

    @Override
    public void delete(String alias) {
        throw unavailable();
    }

    private static CredentialVaultException unavailable() {
        return new CredentialVaultException(MESSAGE);
    }
}
