package com.chenwei666.netserial.update;

import android.content.Context;
import android.content.SharedPreferences;

public final class UpdateCheckPreferences {
    private static final String NAME = "update_check_v1";
    private final SharedPreferences preferences;

    public UpdateCheckPreferences(Context context) {
        preferences = context.getApplicationContext().getSharedPreferences(NAME, Context.MODE_PRIVATE);
    }

    public boolean isAutomaticEnabled() { return preferences.getBoolean("automatic", true); }
    public void setAutomaticEnabled(boolean enabled) {
        if (!preferences.edit().putBoolean("automatic", enabled).commit()) {
            throw new IllegalStateException("Unable to save update preference");
        }
    }
    public long getLastCheckedAt() { return preferences.getLong("last_checked_at", 0L); }
    public void markChecked(long now) { preferences.edit().putLong("last_checked_at", now).apply(); }
    public boolean isDue(long now) {
        return isAutomaticEnabled() && now - getLastCheckedAt() >= 24L * 60L * 60L * 1000L;
    }
}
