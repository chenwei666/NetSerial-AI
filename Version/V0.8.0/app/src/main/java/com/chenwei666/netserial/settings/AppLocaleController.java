package com.chenwei666.netserial.settings;

import android.content.Context;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;

public final class AppLocaleController {
    private AppLocaleController() { }

    public static void applyStoredLanguage(Context context) {
        apply(new AppSettingsStore(context).load().getLanguage());
    }

    public static void apply(AppLanguage language) {
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(language.getLanguageTag()));
    }
}
