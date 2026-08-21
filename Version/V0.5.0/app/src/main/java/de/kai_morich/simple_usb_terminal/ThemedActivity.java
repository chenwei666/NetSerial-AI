package de.kai_morich.simple_usb_terminal;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.chenwei666.netserial.settings.AppLocaleController;

public abstract class ThemedActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        AppAppearanceController.applyBeforeCreate(this);
        AppLocaleController.applyStoredLanguage(this);
        super.onCreate(savedInstanceState);
        AppAppearanceController.applyWindowPreferences(this);
    }
}
