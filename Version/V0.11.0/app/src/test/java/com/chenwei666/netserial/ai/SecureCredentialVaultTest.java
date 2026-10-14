package com.chenwei666.netserial.ai;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

import java.nio.charset.StandardCharsets;

public class SecureCredentialVaultTest {

    @Test
    public void storeThenUseProvidesCredentialOnlyDuringCallbackAndWipesBuffer() {
        TestCredentialComponents.InMemoryRecordStore recordStore =
                new TestCredentialComponents.InMemoryRecordStore();
        SecureCredentialVault vault = new SecureCredentialVault(
                new TestCredentialComponents.XorSecretCipher(),
                recordStore
        );
        char[] supplied = "example-credential".toCharArray();
        char[][] observedBuffer = new char[1][];

        vault.store("provider-main", supplied);
        String result = vault.withCredential("provider-main", credential -> {
            observedBuffer[0] = credential;
            assertArrayEquals(supplied, credential);
            return "used";
        });

        assertEquals("used", result);
        assertTrue(vault.contains("provider-main"));
        assertArrayEquals(new char[observedBuffer[0].length], observedBuffer[0]);
        assertNotEquals(
                new String(supplied),
                new String(recordStore.load("provider-main").getCiphertext(), StandardCharsets.UTF_8)
        );
    }

    @Test
    public void callbackFailureStillWipesCredentialBuffer() {
        SecureCredentialVault vault = new SecureCredentialVault(
                new TestCredentialComponents.XorSecretCipher(),
                new TestCredentialComponents.InMemoryRecordStore()
        );
        char[][] observedBuffer = new char[1][];
        vault.store("provider-main", "temporary-value".toCharArray());

        try {
            vault.withCredential("provider-main", credential -> {
                observedBuffer[0] = credential;
                throw new IllegalStateException("request failed");
            });
            fail("Expected CredentialVaultException");
        } catch (CredentialVaultException expected) {
            assertTrue(expected.getCause() instanceof IllegalStateException);
        }

        assertArrayEquals(new char[observedBuffer[0].length], observedBuffer[0]);
    }

    @Test
    public void deleteRemovesCredential() {
        SecureCredentialVault vault = new SecureCredentialVault(
                new TestCredentialComponents.XorSecretCipher(),
                new TestCredentialComponents.InMemoryRecordStore()
        );
        vault.store("provider-main", "temporary-value".toCharArray());

        vault.delete("provider-main");

        assertFalse(vault.contains("provider-main"));
    }

    @Test(expected = CredentialVaultException.class)
    public void encryptedRecordCannotBeMovedToAnotherAlias() {
        TestCredentialComponents.InMemoryRecordStore recordStore =
                new TestCredentialComponents.InMemoryRecordStore();
        SecureCredentialVault vault = new SecureCredentialVault(
                new TestCredentialComponents.XorSecretCipher(),
                recordStore
        );
        vault.store("provider-first", "temporary-value".toCharArray());
        recordStore.save("provider-second", recordStore.load("provider-first"));

        vault.withCredential("provider-second", credential -> null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void blankAliasIsRejected() {
        SecureCredentialVault vault = new SecureCredentialVault(
                new TestCredentialComponents.XorSecretCipher(),
                new TestCredentialComponents.InMemoryRecordStore()
        );
        vault.store("  ", "temporary-value".toCharArray());
    }

    @Test(expected = IllegalArgumentException.class)
    public void emptyCredentialIsRejected() {
        SecureCredentialVault vault = new SecureCredentialVault(
                new TestCredentialComponents.XorSecretCipher(),
                new TestCredentialComponents.InMemoryRecordStore()
        );
        vault.store("provider-main", new char[0]);
    }

}
