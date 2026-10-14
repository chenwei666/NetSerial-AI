package com.chenwei666.netserial.ai;

import android.content.Context;
import android.os.Build;

import java.util.Objects;

public final class CredentialVaultFactory {
    private CredentialVaultFactory() {
    }

    public static CredentialVault create(Context context) {
        Objects.requireNonNull(context, "context");
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            throw new CredentialVaultException(
                    "AI credential storage requires Android 6.0 or newer"
            );
        }
        return new SecureCredentialVault(
                new AndroidKeystoreSecretCipher(),
                new SharedPreferencesCredentialRecordStore(context)
        );
    }
}
