package com.chenwei666.netserial.settings;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Objects;

public final class AppSettingsStore {
    private static final String NAME = "app_settings_v1";
    private final SharedPreferences preferences;

    public AppSettingsStore(Context context) {
        preferences = Objects.requireNonNull(context, "context").getApplicationContext()
                .getSharedPreferences(NAME, Context.MODE_PRIVATE);
    }

    public AppSettings load() {
        AppSettings defaults = AppSettings.defaults();
        try {
            return new AppSettings(
                    AppLanguage.fromTag(preferences.getString("language", defaults.getLanguage().getLanguageTag())),
                    preferences.getBoolean("telnet_enabled", defaults.isTelnetEnabled()),
                    preferences.getInt("remote_timeout_ms", defaults.getRemoteTimeoutMillis()),
                    preferences.getInt("terminal_text_size_sp", defaults.getTerminalTextSizeSp()),
                    preferences.getString("remote_charset", defaults.getRemoteCharset()),
                    preferences.getInt("ssh_keepalive_ms", defaults.getSshKeepAliveMillis()),
                    preferences.getInt("network_probe_timeout_ms", defaults.getNetworkProbeTimeoutMillis()),
                    AppearanceMode.fromStoredValue(preferences.getString("appearance_mode", null)),
                    AccentTheme.fromStoredValue(preferences.getString("accent_theme", null)),
                    preferences.getBoolean("keep_screen_awake", defaults.isKeepScreenAwake())
            );
        } catch (RuntimeException exception) {
            return defaults;
        }
    }

    public void save(AppSettings settings) {
        Objects.requireNonNull(settings, "settings");
        boolean saved = preferences.edit()
                .putString("language", settings.getLanguage().getLanguageTag())
                .putBoolean("telnet_enabled", settings.isTelnetEnabled())
                .putInt("remote_timeout_ms", settings.getRemoteTimeoutMillis())
                .putInt("terminal_text_size_sp", settings.getTerminalTextSizeSp())
                .putString("remote_charset", settings.getRemoteCharset())
                .putInt("ssh_keepalive_ms", settings.getSshKeepAliveMillis())
                .putInt("network_probe_timeout_ms", settings.getNetworkProbeTimeoutMillis())
                .putString("appearance_mode", settings.getAppearanceMode().name())
                .putString("accent_theme", settings.getAccentTheme().name())
                .putBoolean("keep_screen_awake", settings.isKeepScreenAwake())
                .commit();
        if (!saved) throw new IllegalStateException("Unable to persist app settings");
    }
}
