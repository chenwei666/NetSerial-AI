package com.chenwei666.netserial.memory;

import android.content.Context;
import android.content.SharedPreferences;
import com.chenwei666.netserial.ai.CredentialVault;
import com.chenwei666.netserial.ai.CredentialVaultFactory;
import java.util.Arrays;

/** Only ciphertext enters preferences; no plaintext or API <23 fallback. */
public final class EncryptedConfirmedMemoryPersistence implements MemoryVault.Persistence {
    private static final String ALIAS = "confirmed-chat-memory-v1";
    private final CredentialVault vault;
    private final SharedPreferences metadata;

    public EncryptedConfirmedMemoryPersistence(Context context) {
        vault = CredentialVaultFactory.create(context.getApplicationContext());
        metadata = context.getApplicationContext().getSharedPreferences("confirmed_memory_metadata_v1", Context.MODE_PRIVATE);
    }

    @Override public String read() {
        if (!vault.contains(ALIAS)) {
            if (metadata.getBoolean("initialized", false)) throw new IllegalStateException("Encrypted memory unavailable");
            return null;
        }
        return vault.withCredential(ALIAS, String::new);
    }

    @Override public void write(String document) {
        // Set the marker first: an interrupted or corrupt write can never look like a new empty vault.
        if (!metadata.getBoolean("initialized", false)
                && !metadata.edit().putBoolean("initialized", true).commit())
            throw new IllegalStateException("Unable to initialize memory");
        char[] chars = document.toCharArray();
        try { vault.store(ALIAS, chars); }
        finally { Arrays.fill(chars, '\0'); }
    }
}
