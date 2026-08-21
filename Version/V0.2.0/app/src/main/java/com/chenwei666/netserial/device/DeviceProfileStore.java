package com.chenwei666.netserial.device;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Objects;

/** Stores only non-sensitive device context. Credentials are intentionally unsupported. */
public final class DeviceProfileStore {
    private static final String NAME = "device_profile_v1";
    private final SharedPreferences preferences;

    public DeviceProfileStore(Context context) {
        preferences = Objects.requireNonNull(context, "context").getApplicationContext()
                .getSharedPreferences(NAME, Context.MODE_PRIVATE);
    }

    public DeviceProfile load() {
        try {
            return new DeviceProfile(
                    preferences.getString("name", "Default switch"),
                    Vendor.valueOf(preferences.getString("vendor", Vendor.H3C_COMWARE.name())),
                    CliMode.valueOf(preferences.getString("mode", CliMode.USER_VIEW.name())),
                    preferences.getInt("baud", 9600)
            );
        } catch (RuntimeException exception) {
            return DeviceProfile.defaults();
        }
    }

    public void save(DeviceProfile profile) {
        Objects.requireNonNull(profile, "profile");
        boolean saved = preferences.edit()
                .putString("name", profile.getName())
                .putString("vendor", profile.getVendor().name())
                .putString("mode", profile.getCliMode().name())
                .putInt("baud", profile.getBaudRate())
                .commit();
        if (!saved) {
            throw new IllegalStateException("Unable to persist device profile");
        }
    }
}
