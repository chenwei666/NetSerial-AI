package com.chenwei666.netserial.ai;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

final class TestCredentialComponents {
    private TestCredentialComponents() {
    }

    static final class XorSecretCipher implements SecretCipher {
        private static final byte MASK = 0x5A;

        @Override
        public EncryptedCredential encrypt(String alias, byte[] plaintext) {
            return new EncryptedCredential(
                    1,
                    alias.getBytes(StandardCharsets.UTF_8),
                    transform(plaintext)
            );
        }

        @Override
        public byte[] decrypt(String alias, EncryptedCredential credential) {
            if (!Arrays.equals(
                    alias.getBytes(StandardCharsets.UTF_8),
                    credential.getInitializationVector()
            )) {
                throw new CredentialVaultException("Credential alias mismatch");
            }
            return transform(credential.getCiphertext());
        }

        private byte[] transform(byte[] source) {
            byte[] result = Arrays.copyOf(source, source.length);
            for (int index = 0; index < result.length; index++) {
                result[index] ^= MASK;
            }
            return result;
        }
    }

    static final class InMemoryRecordStore implements CredentialRecordStore {
        private final Map<String, EncryptedCredential> records = new HashMap<>();

        @Override
        public void save(String alias, EncryptedCredential credential) {
            records.put(alias, credential);
        }

        @Override
        public EncryptedCredential load(String alias) {
            EncryptedCredential credential = records.get(alias);
            if (credential == null) {
                throw new CredentialVaultException("Credential not found");
            }
            return credential;
        }

        @Override
        public boolean contains(String alias) {
            return records.containsKey(alias);
        }

        @Override
        public void delete(String alias) {
            records.remove(alias);
        }
    }
}
