package de.kai_morich.simple_usb_terminal;

import android.app.AlertDialog;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.widget.SwitchCompat;

import com.chenwei666.netserial.settings.AppLanguage;
import com.chenwei666.netserial.settings.AppSettings;
import com.chenwei666.netserial.settings.AppSettingsStore;
import com.chenwei666.netserial.settings.AccentTheme;
import com.chenwei666.netserial.settings.AppearanceMode;
import com.chenwei666.netserial.update.UpdateCheckPreferences;

import java.io.File;

public class AppSettingsActivity extends ThemedActivity {
    private Spinner languageSpinner;
    private Spinner appearanceSpinner;
    private Spinner accentThemeSpinner;
    private SwitchCompat keepAwakeSwitch;
    private SwitchCompat telnetSwitch;
    private SwitchCompat autoUpdateSwitch;
    private EditText timeoutSeconds;
    private Spinner textSizeSpinner;
    private Spinner charsetSpinner;
    private EditText keepAliveSeconds;
    private EditText probeTimeoutSeconds;
    private AppSettingsStore store;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_settings);
        setTitle(R.string.app_settings_title);
        store = new AppSettingsStore(this);
        languageSpinner = findViewById(R.id.settings_language);
        appearanceSpinner = findViewById(R.id.settings_appearance);
        accentThemeSpinner = findViewById(R.id.settings_accent_theme);
        keepAwakeSwitch = findViewById(R.id.settings_keep_awake);
        telnetSwitch = findViewById(R.id.settings_telnet_enabled);
        autoUpdateSwitch = findViewById(R.id.settings_auto_update);
        timeoutSeconds = findViewById(R.id.settings_timeout);
        textSizeSpinner = findViewById(R.id.settings_text_size);
        charsetSpinner = findViewById(R.id.settings_charset);
        keepAliveSeconds = findViewById(R.id.settings_keepalive);
        probeTimeoutSeconds = findViewById(R.id.settings_probe_timeout);
        bindSpinner(languageSpinner, R.array.app_languages);
        bindSpinner(appearanceSpinner, R.array.appearance_modes);
        bindSpinner(accentThemeSpinner, R.array.accent_themes);
        bindSpinner(textSizeSpinner, R.array.terminal_text_sizes);
        bindSpinner(charsetSpinner, R.array.remote_charsets);
        render(store.load());
        autoUpdateSwitch.setChecked(new UpdateCheckPreferences(this).isAutomaticEnabled());
        findViewById(R.id.settings_save).setOnClickListener(v -> save());
        findViewById(R.id.settings_forget_hosts).setOnClickListener(v -> confirmForgetHosts());
        findViewById(R.id.settings_check_update).setOnClickListener(v ->
                UpdateCheckCoordinator.checkManually(this));
    }

    private void bindSpinner(Spinner spinner, int arrayId) {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, arrayId, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    private void render(AppSettings settings) {
        languageSpinner.setSelection(settings.getLanguage().ordinal());
        appearanceSpinner.setSelection(settings.getAppearanceMode().ordinal());
        accentThemeSpinner.setSelection(settings.getAccentTheme().ordinal());
        keepAwakeSwitch.setChecked(settings.isKeepScreenAwake());
        telnetSwitch.setChecked(settings.isTelnetEnabled());
        timeoutSeconds.setText(String.valueOf(settings.getRemoteTimeoutMillis() / 1000));
        String[] sizes = getResources().getStringArray(R.array.terminal_text_size_values);
        textSizeSpinner.setSelection(indexOf(sizes, String.valueOf(settings.getTerminalTextSizeSp())));
        String[] charsets = getResources().getStringArray(R.array.remote_charsets);
        charsetSpinner.setSelection(indexOf(charsets, settings.getRemoteCharset()));
        keepAliveSeconds.setText(String.valueOf(settings.getSshKeepAliveMillis() / 1000));
        probeTimeoutSeconds.setText(String.valueOf(settings.getNetworkProbeTimeoutMillis() / 1000));
    }

    private void save() {
        AppSettings previous = store.load();
        AppSettings settings;
        try {
            int seconds = Integer.parseInt(timeoutSeconds.getText().toString().trim());
            String[] sizes = getResources().getStringArray(R.array.terminal_text_size_values);
            settings = new AppSettings(
                    AppLanguage.values()[languageSpinner.getSelectedItemPosition()],
                    telnetSwitch.isChecked(),
                    seconds * 1000,
                    Integer.parseInt(sizes[textSizeSpinner.getSelectedItemPosition()]),
                    charsetSpinner.getSelectedItem().toString(),
                    Integer.parseInt(keepAliveSeconds.getText().toString().trim()) * 1000,
                    Integer.parseInt(probeTimeoutSeconds.getText().toString().trim()) * 1000,
                    AppearanceMode.values()[appearanceSpinner.getSelectedItemPosition()],
                    AccentTheme.values()[accentThemeSpinner.getSelectedItemPosition()],
                    keepAwakeSwitch.isChecked()
            );
        } catch (IllegalArgumentException | IndexOutOfBoundsException exception) {
            Toast.makeText(this, R.string.settings_invalid, Toast.LENGTH_LONG).show();
            return;
        }
        try {
            store.save(settings);
            new UpdateCheckPreferences(this).setAutomaticEnabled(autoUpdateSwitch.isChecked());
        } catch (RuntimeException exception) {
            Toast.makeText(this, R.string.settings_save_failed, Toast.LENGTH_LONG).show();
            return;
        }
        Toast.makeText(this, R.string.settings_saved, Toast.LENGTH_SHORT).show();
        try {
            AppAppearanceController.applySavedSettings(this, previous, settings);
        } catch (RuntimeException exception) {
            Toast.makeText(this, R.string.settings_apply_restart, Toast.LENGTH_LONG).show();
        }
    }

    private void confirmForgetHosts() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.settings_forget_hosts)
                .setMessage(R.string.settings_forget_hosts_warning)
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(R.string.forget, (dialog, which) -> forgetHosts())
                .show();
    }

    private void forgetHosts() {
        File file = new File(getFilesDir(), "ssh_known_hosts");
        if (!file.exists() || file.delete()) {
            Toast.makeText(this, R.string.settings_hosts_forgotten, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, R.string.settings_hosts_forget_failed, Toast.LENGTH_LONG).show();
        }
    }

    private static int indexOf(String[] values, String target) {
        for (int i = 0; i < values.length; i++) if (values[i].equalsIgnoreCase(target)) return i;
        return 0;
    }
}
