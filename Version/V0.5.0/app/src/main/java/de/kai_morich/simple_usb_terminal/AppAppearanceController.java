package de.kai_morich.simple_usb_terminal;

import android.app.Activity;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatDelegate;

import com.chenwei666.netserial.settings.AccentTheme;
import com.chenwei666.netserial.settings.AppSettings;
import com.chenwei666.netserial.settings.AppSettingsStore;
import com.chenwei666.netserial.settings.AppearanceMode;

public final class AppAppearanceController {
    private AppAppearanceController() {
    }

    public static void applyBeforeCreate(Activity activity) {
        AppSettings settings = new AppSettingsStore(activity).load();
        AppCompatDelegate.setDefaultNightMode(toNightMode(settings.getAppearanceMode()));
        activity.setTheme(toThemeResource(settings.getAccentTheme()));
    }

    public static void applyWindowPreferences(Activity activity) {
        boolean keepAwake = new AppSettingsStore(activity).load().isKeepScreenAwake();
        if (keepAwake) {
            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        } else {
            activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }

    public static void applyNightMode(AppearanceMode mode) {
        AppCompatDelegate.setDefaultNightMode(toNightMode(mode));
    }

    private static int toNightMode(AppearanceMode mode) {
        switch (mode) {
            case LIGHT:
                return AppCompatDelegate.MODE_NIGHT_NO;
            case DARK:
                return AppCompatDelegate.MODE_NIGHT_YES;
            case SYSTEM:
            default:
                return AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
        }
    }

    private static int toThemeResource(AccentTheme theme) {
        switch (theme) {
            case EMERALD:
                return R.style.AppTheme_Emerald;
            case VIOLET:
                return R.style.AppTheme_Violet;
            case SUNSET:
                return R.style.AppTheme_Sunset;
            case OCEAN:
            default:
                return R.style.AppTheme_Ocean;
        }
    }
}
