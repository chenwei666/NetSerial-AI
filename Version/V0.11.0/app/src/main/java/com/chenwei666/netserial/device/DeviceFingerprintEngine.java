package com.chenwei666.netserial.device;

import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;

/** Identifies supported switch families only from explicit vendor/version evidence. */
public final class DeviceFingerprintEngine {
    public DeviceFingerprint identify(String terminalText) {
        String value = normalize(terminalText);
        if (value.isEmpty()) return DeviceFingerprint.unknown();

        Map<Vendor, Integer> scores = new EnumMap<>(Vendor.class);
        for (Vendor vendor : Vendor.values()) scores.put(vendor, 0);
        score(scores, Vendor.H3C_COMWARE, value, "h3c", 90);
        score(scores, Vendor.H3C_COMWARE, value, "comware", 80);
        score(scores, Vendor.HUAWEI_VRP, value, "huawei", 90);
        score(scores, Vendor.HUAWEI_VRP, value, "quidway", 90);
        score(scores, Vendor.HUAWEI_VRP, value, "versatile routing platform", 80);
        score(scores, Vendor.HUAWEI_VRP, value, " vrp ", 70);
        score(scores, Vendor.CISCO_IOS, value, "cisco ios", 90);
        score(scores, Vendor.CISCO_IOS, value, "ios xe", 90);
        score(scores, Vendor.CISCO_IOS, value, "catalyst", 80);
        score(scores, Vendor.RUIJIE_RGOS, value, "ruijie", 90);
        score(scores, Vendor.RUIJIE_RGOS, value, "rgos", 85);
        score(scores, Vendor.RUIJIE_RGOS, value, "reyee", 80);

        Vendor winner = Vendor.GENERIC;
        int best = 0;
        int second = 0;
        for (Map.Entry<Vendor, Integer> entry : scores.entrySet()) {
            if (entry.getKey() == Vendor.GENERIC) continue;
            if (entry.getValue() > best) {
                second = best;
                best = entry.getValue();
                winner = entry.getKey();
            } else if (entry.getValue() > second) {
                second = entry.getValue();
            }
        }
        if (best < 70 || best - second < 20) return DeviceFingerprint.unknown();
        int confidence = Math.min(100, best);
        return new DeviceFingerprint(winner, confidence, platform(winner, value), evidence(winner));
    }

    private static void score(Map<Vendor, Integer> scores, Vendor vendor, String value,
                              String marker, int points) {
        if (value.contains(marker)) scores.put(vendor, scores.get(vendor) + points);
    }

    private static String platform(Vendor vendor, String value) {
        if (vendor == Vendor.H3C_COMWARE) {
            if (value.contains("comware software, version 7") || value.contains("comware 7")) return "Comware 7";
            if (value.contains("comware software, version 5") || value.contains("comware 5")) return "Comware 5";
            return "Comware";
        }
        if (vendor == Vendor.HUAWEI_VRP) return "VRP";
        if (vendor == Vendor.CISCO_IOS) return value.contains("ios xe") ? "IOS XE" : "IOS";
        if (vendor == Vendor.RUIJIE_RGOS) return "RGOS";
        return "";
    }

    private static String evidence(Vendor vendor) {
        switch (vendor) {
            case H3C_COMWARE: return "H3C/Comware banner";
            case HUAWEI_VRP: return "Huawei/VRP banner";
            case CISCO_IOS: return "Cisco IOS/IOS XE banner";
            case RUIJIE_RGOS: return "Ruijie/RGOS banner";
            default: return "";
        }
    }

    private static String normalize(String value) {
        if (value == null) return "";
        StringBuilder safe = new StringBuilder(Math.min(value.length(), 32_000));
        int start = Math.max(0, value.length() - 32_000);
        for (int i = start; i < value.length(); i++) {
            char c = value.charAt(i);
            safe.append(Character.isISOControl(c) && c != '\n' && c != '\r' && c != '\t' ? ' ' : c);
        }
        return safe.toString().toLowerCase(Locale.ROOT).replaceAll("\\s+", " ");
    }
}
