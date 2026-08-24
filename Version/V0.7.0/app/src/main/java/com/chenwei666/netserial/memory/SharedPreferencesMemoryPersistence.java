package com.chenwei666.netserial.memory;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Objects;

public final class SharedPreferencesMemoryPersistence implements MemoryVault.Persistence {
    private final SharedPreferences preferences;

    public SharedPreferencesMemoryPersistence(Context context) {
        preferences = Objects.requireNonNull(context, "context").getApplicationContext()
                .getSharedPreferences("ai_memory_v1", Context.MODE_PRIVATE);
    }

    @Override public String read() { return preferences.getString("document", null); }

    @Override public void write(String document) {
        if (!preferences.edit().putString("document", document).commit()) {
            throw new IllegalStateException("Unable to persist AI memory");
        }
    }
}
