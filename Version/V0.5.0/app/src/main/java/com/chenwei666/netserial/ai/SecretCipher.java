package com.chenwei666.netserial.ai;

public interface SecretCipher {
    EncryptedCredential encrypt(String alias, byte[] plaintext);

    byte[] decrypt(String alias, EncryptedCredential credential);
}
