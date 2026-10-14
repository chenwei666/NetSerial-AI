package com.chenwei666.netserial.ai;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;

public final class SharedPreferencesCredentialRecordStore implements CredentialRecordStore {
    private static final String PREFERENCES_NAME = "ai_credential_ciphertext_v1";
    private static final String VERSION_SUFFIX = ".version";
    private static final String IV_SUFFIX = ".iv";
    private static final String DATA_SUFFIX = ".data";

    private final SharedPreferences preferences;

    public SharedPreferencesCredentialRecordStore(Context context) {
        Context appContext = Objects.requireNonNull(context, "context").getApplicationContext();
        this.preferences = appContext.getSharedPreferences(
                PREFERENCES_NAME,
                Context.MODE_PRIVATE
        );
    }

    @Override
    public void save(String alias, EncryptedCredential credential) {
        Objects.requireNonNull(credential, "credential");
        String prefix = keyPrefix(alias);
        boolean saved = preferences.edit()
                .putInt(prefix + VERSION_SUFFIX, credential.getVersion())
                .putString(
                        prefix + IV_SUFFIX,
                        Base64.encodeToString(
                                credential.getInitializationVector(),
                                Base64.NO_WRAP
                        )
                )
                .putString(
                        prefix + DATA_SUFFIX,
                        Base64.encodeToString(credential.getCiphertext(), Base64.NO_WRAP)
                )
                .commit();
        if (!saved) {
            throw new CredentialVaultException("Unable to persist encrypted credential");
        }
    }

    @Override
    public EncryptedCredential load(String alias) {
        String prefix = keyPrefix(alias);
        int version = preferences.getInt(prefix + VERSION_SUFFIX, 0);
        String encodedIv = preferences.getString(prefix + IV_SUFFIX, null);
        String encodedData = preferences.getString(prefix + DATA_SUFFIX, null);
        if (version == 0 || encodedIv == null || encodedData == null) {
            throw new CredentialVaultException("Credential not found");
        }
        try {
            return new EncryptedCredential(
                    version,
                    Base64.decode(encodedIv, Base64.NO_WRAP),
                    Base64.decode(encodedData, Base64.NO_WRAP)
            );
        } catch (IllegalArgumentException exception) {
            throw new CredentialVaultException("Stored credential is corrupted", exception);
        }
    }

    @Override
    public boolean contains(String alias) {
        String prefix = keyPrefix(alias);
        return preferences.contains(prefix + VERSION_SUFFIX)
                && preferences.contains(prefix + IV_SUFFIX)
                && preferences.contains(prefix + DATA_SUFFIX);
    }

    @Override
    public void delete(String alias) {
        String prefix = keyPrefix(alias);
        boolean deleted = preferences.edit()
                .remove(prefix + VERSION_SUFFIX)
                .remove(prefix + IV_SUFFIX)
                .remove(prefix + DATA_SUFFIX)
                .commit();
        if (!deleted) {
            throw new CredentialVaultException("Unable to delete encrypted credential");
        }
    }

    private static String keyPrefix(String alias) {
        String normalizedAlias = CredentialAliases.normalize(alias);
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(
                    normalizedAlias.getBytes(StandardCharsets.UTF_8)
            );
            StringBuilder key = new StringBuilder("credential.");
            for (byte value : digest) {
                key.append(Character.forDigit((value >>> 4) & 0x0F, 16));
                key.append(Character.forDigit(value & 0x0F, 16));
            }
            return key.toString();
        } catch (NoSuchAlgorithmException exception) {
            throw new CredentialVaultException("SHA-256 is unavailable", exception);
        }
    }
}
