package de.kai_morich.simple_usb_terminal;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.chenwei666.netserial.settings.AppLocaleController;
import com.chenwei666.netserial.settings.AppSettings;
import com.chenwei666.netserial.settings.AppSettingsStore;

public abstract class ThemedActivity extends AppCompatActivity {
    private AppSettings appliedSettings;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        AppSettings settings = AppAppearanceController.applyBeforeCreate(this);
        appliedSettings = settings;
        AppLocaleController.applyStoredLanguage(this);
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        AppSettings current = new AppSettingsStore(this).load();
        if (appliedSettings.hasDifferentAppearance(current)) {
            appliedSettings = current;
            recreate();
        }
    }
}
