package com.chenwei666.netserial.settings;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Test;

public class AppSettingsTest {
    @Test
    public void secureDefaultsDisableTelnet() {
        AppSettings settings = AppSettings.defaults();
        assertFalse(settings.isTelnetEnabled());
        assertEquals(AppLanguage.SYSTEM, settings.getLanguage());
        assertEquals("UTF-8", settings.getRemoteCharset());
        assertEquals(AppearanceMode.SYSTEM, settings.getAppearanceMode());
        assertEquals(AccentTheme.OCEAN, settings.getAccentTheme());
    }

    @Test
    public void languageTagsAreNormalized() {
        assertEquals(AppLanguage.SIMPLIFIED_CHINESE, AppLanguage.fromTag("zh-Hans"));
        assertEquals(AppLanguage.ENGLISH, AppLanguage.fromTag("en-US"));
        assertEquals(AppLanguage.SYSTEM, AppLanguage.fromTag("fr"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsUnsafeTimeoutRange() {
        new AppSettings(AppLanguage.SYSTEM, false, 1_000, 14, "UTF-8");
    }
}
