package com.chenwei666.netserial.config;

import com.chenwei666.netserial.device.Vendor;
import com.chenwei666.netserial.terminal.AnsiTextSanitizer;
import com.chenwei666.netserial.terminal.SensitiveTextRedactor;

import java.util.Arrays;

/** Plans read-only captures and creates normalized, redacted, content-addressed snapshots. */
public final class ConfigurationBackupEngine {
    public ConfigBackupPlan plan(Vendor vendor) {
        if (vendor == null) vendor = Vendor.GENERIC;
        switch (vendor) {
            case H3C_COMWARE:
                return new ConfigBackupPlan(vendor, Arrays.asList("screen-length disable", "display current-configuration"));
            case HUAWEI_VRP:
                return new ConfigBackupPlan(vendor, Arrays.asList("screen-length 0 temporary", "display current-configuration"));
            case CISCO_IOS:
                return new ConfigBackupPlan(vendor, Arrays.asList("terminal length 0", "show running-config"));
            case RUIJIE_RGOS:
                return new ConfigBackupPlan(vendor, Arrays.asList("terminal length 0", "show running-config"));
            case GENERIC:
            default:
                return new ConfigBackupPlan(Vendor.GENERIC, Arrays.asList("show running-config"));
        }
    }

    public ConfigSnapshot capture(String label, String rawCapture, long capturedAtMillis) {
        String safe = new SensitiveTextRedactor().redact(
                new AnsiTextSanitizer().sanitize(rawCapture == null ? "" : rawCapture));
        if (safe.trim().isEmpty()) throw new IllegalArgumentException("Configuration capture required");
        String normalized = new ConfigNormalizer().normalize(safe);
        return new ConfigSnapshot(label, capturedAtMillis, normalized);
    }
}
