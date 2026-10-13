package com.chenwei666.netserial.ai;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Objects;

public final class SecureCredentialVault implements CredentialVault {
    private final SecretCipher cipher;
    private final CredentialRecordStore recordStore;

    public SecureCredentialVault(SecretCipher cipher, CredentialRecordStore recordStore) {
        this.cipher = Objects.requireNonNull(cipher, "cipher");
        this.recordStore = Objects.requireNonNull(recordStore, "recordStore");
    }

    @Override
    public void store(String alias, char[] credential) {
        String normalizedAlias = CredentialAliases.normalize(alias);
        if (credential == null || credential.length == 0) {
            throw new IllegalArgumentException("credential must not be empty");
        }

        byte[] plaintext = encode(credential);
        try {
            recordStore.save(
                    normalizedAlias,
                    cipher.encrypt(normalizedAlias, plaintext)
            );
        } catch (CredentialVaultException exception) {
            throw exception;
        } catch (RuntimeException exception) {
            throw new CredentialVaultException("Unable to store credential", exception);
        } finally {
            Arrays.fill(plaintext, (byte) 0);
        }
    }

    @Override
    public boolean contains(String alias) {
        return recordStore.contains(CredentialAliases.normalize(alias));
    }

    @Override
    public <T> T withCredential(String alias, CredentialOperation<T> operation) {
        String normalizedAlias = CredentialAliases.normalize(alias);
        Objects.requireNonNull(operation, "operation");
        byte[] plaintext = null;
        char[] credential = null;
        try {
            plaintext = cipher.decrypt(
                    normalizedAlias,
                    recordStore.load(normalizedAlias)
            );
            credential = decode(plaintext);
            return operation.execute(credential);
        } catch (CredentialVaultException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new CredentialVaultException("Credential operation failed", exception);
        } finally {
            if (credential != null) {
                Arrays.fill(credential, '\0');
            }
            if (plaintext != null) {
                Arrays.fill(plaintext, (byte) 0);
            }
        }
    }

    @Override
    public void delete(String alias) {
        recordStore.delete(CredentialAliases.normalize(alias));
    }

    private static byte[] encode(char[] value) {
        ByteBuffer encoded = null;
        try {
            encoded = StandardCharsets.UTF_8.newEncoder()
                    .onMalformedInput(CodingErrorAction.REPORT)
                    .onUnmappableCharacter(CodingErrorAction.REPORT)
                    .encode(CharBuffer.wrap(value));
            byte[] result = new byte[encoded.remaining()];
            encoded.get(result);
            return result;
        } catch (CharacterCodingException exception) {
            throw new IllegalArgumentException("credential is not valid UTF-8", exception);
        } finally {
            wipe(encoded);
        }
    }

    private static char[] decode(byte[] value) {
        CharBuffer decoded = null;
        try {
            decoded = StandardCharsets.UTF_8.newDecoder()
                    .onMalformedInput(CodingErrorAction.REPORT)
                    .onUnmappableCharacter(CodingErrorAction.REPORT)
                    .decode(ByteBuffer.wrap(value));
            char[] result = new char[decoded.remaining()];
            decoded.get(result);
            return result;
        } catch (CharacterCodingException exception) {
            throw new CredentialVaultException("Stored credential is not valid UTF-8", exception);
        } finally {
            wipe(decoded);
        }
    }

    private static void wipe(ByteBuffer buffer) {
        if (buffer != null && buffer.hasArray()) {
            Arrays.fill(buffer.array(), (byte) 0);
        }
    }

    private static void wipe(CharBuffer buffer) {
        if (buffer != null && buffer.hasArray()) {
            Arrays.fill(buffer.array(), '\0');
        }
    }
}
