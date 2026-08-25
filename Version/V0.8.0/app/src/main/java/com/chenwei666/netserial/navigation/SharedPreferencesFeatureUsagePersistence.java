package com.chenwei666.netserial.navigation;

import android.content.Context;
import android.content.SharedPreferences;

public final class SharedPreferencesFeatureUsagePersistence implements FeatureUsagePersistence {
    private static final String KEY_DOCUMENT = "document";
    private final SharedPreferences preferences;

    public SharedPreferencesFeatureUsagePersistence(Context context) {
        preferences = context.getApplicationContext().getSharedPreferences(
                "netserial_feature_usage", Context.MODE_PRIVATE);
    }

    @Override public String read() { return preferences.getString(KEY_DOCUMENT, ""); }

    @Override public void write(String document) {
        if (!preferences.edit().putString(KEY_DOCUMENT, document == null ? "" : document).commit()) {
            throw new IllegalStateException("Unable to save feature usage");
        }
    }
}
