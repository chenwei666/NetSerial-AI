package com.chenwei666.netserial.ai;

import android.os.Build;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;

import androidx.annotation.RequiresApi;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyStore;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

@RequiresApi(Build.VERSION_CODES.M)
public final class AndroidKeystoreSecretCipher implements SecretCipher {
    private static final int FORMAT_VERSION = 1;
    private static final int GCM_TAG_LENGTH_BITS = 128;
    private static final String KEYSTORE_PROVIDER = "AndroidKeyStore";
    private static final String KEY_ALIAS = "com.chenwei666.netserial.ai.credentials.v1";
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";

    @Override
    public EncryptedCredential encrypt(String alias, byte[] plaintext) {
        try {
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, getOrCreateKey());
            cipher.updateAAD(alias.getBytes(StandardCharsets.UTF_8));
            return new EncryptedCredential(
                    FORMAT_VERSION,
                    cipher.getIV(),
                    cipher.doFinal(plaintext)
            );
        } catch (GeneralSecurityException | IOException exception) {
            throw new CredentialVaultException("Unable to encrypt credential", exception);
        }
    }

    @Override
    public byte[] decrypt(String alias, EncryptedCredential credential) {
        if (credential.getVersion() != FORMAT_VERSION) {
            throw new CredentialVaultException("Unsupported credential format version");
        }
        try {
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(
                    Cipher.DECRYPT_MODE,
                    getOrCreateKey(),
                    new GCMParameterSpec(
                            GCM_TAG_LENGTH_BITS,
                            credential.getInitializationVector()
                    )
            );
            cipher.updateAAD(alias.getBytes(StandardCharsets.UTF_8));
            return cipher.doFinal(credential.getCiphertext());
        } catch (GeneralSecurityException | IOException exception) {
            throw new CredentialVaultException("Unable to decrypt credential", exception);
        }
    }

    private synchronized SecretKey getOrCreateKey()
            throws GeneralSecurityException, IOException {
        KeyStore keyStore = KeyStore.getInstance(KEYSTORE_PROVIDER);
        keyStore.load(null);
        if (keyStore.containsAlias(KEY_ALIAS)) {
            return (SecretKey) keyStore.getKey(KEY_ALIAS, null);
        }

        KeyGenerator keyGenerator = KeyGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_AES,
                KEYSTORE_PROVIDER
        );
        keyGenerator.init(new KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT
        )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .setRandomizedEncryptionRequired(true)
                .build());
        return keyGenerator.generateKey();
    }
}
