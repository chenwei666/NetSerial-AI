package com.chenwei666.netserial.ai;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;

import org.junit.Test;

public class UnavailableCredentialVaultTest {
    private final CredentialVault vault = new UnavailableCredentialVault();

    @Test
    public void reportsThatCredentialsAreUnavailable() {
        assertFalse(vault.contains("provider"));
    }

    @Test
    public void rejectsEveryCredentialOperation() {
        assertThrows(CredentialVaultException.class,
                () -> vault.store("provider", "secret".toCharArray()));
        assertThrows(CredentialVaultException.class,
                () -> vault.withCredential("provider", credential -> null));
        assertThrows(CredentialVaultException.class,
                () -> vault.delete("provider"));
    }
}
