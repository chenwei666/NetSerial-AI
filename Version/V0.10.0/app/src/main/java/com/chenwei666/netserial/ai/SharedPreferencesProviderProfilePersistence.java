package com.chenwei666.netserial.ai;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Objects;

public final class SharedPreferencesProviderProfilePersistence
        implements ProviderProfilePersistence {
    private static final String PREFERENCES_NAME = "ai_provider_profiles_v1";
    private static final String DOCUMENT_KEY = "profiles.document";

    private final SharedPreferences preferences;

    public SharedPreferencesProviderProfilePersistence(Context context) {
        Context appContext = Objects.requireNonNull(context, "context").getApplicationContext();
        preferences = appContext.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
    }

    @Override
    public String read() {
        return preferences.getString(DOCUMENT_KEY, null);
    }

    @Override
    public void write(String document) {
        boolean saved = preferences.edit().putString(DOCUMENT_KEY, document).commit();
        if (!saved) {
            throw new ProviderProfileStoreException("Unable to persist AI provider profiles");
        }
    }
}
