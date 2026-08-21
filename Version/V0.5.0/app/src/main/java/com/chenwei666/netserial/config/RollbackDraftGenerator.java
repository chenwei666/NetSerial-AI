package com.chenwei666.netserial.config;

import com.chenwei666.netserial.device.Vendor;

import java.util.Locale;

public final class RollbackDraftGenerator {
    public String generate(ConfigDiff diff, Vendor vendor) {
        String negate = usesCiscoNegation(vendor) ? "no " : "undo ";
        StringBuilder draft = new StringBuilder();
        for (String added : diff.getAddedLines()) {
            String line = added.trim();
            if (isCommand(line)) draft.append(negate).append(stripExistingNegation(line)).append('\n');
        }
        for (String removed : diff.getRemovedLines()) {
            String line = removed.trim();
            if (isCommand(line)) draft.append(line).append('\n');
        }
        return draft.toString();
    }

    private static boolean usesCiscoNegation(Vendor vendor) {
        return vendor == Vendor.CISCO_IOS || vendor == Vendor.RUIJIE_RGOS || vendor == Vendor.GENERIC;
    }

    private static boolean isCommand(String line) {
        return !line.isEmpty() && !line.startsWith("#") && !line.startsWith("!")
                && !line.startsWith("[") && !line.endsWith(">");
    }

    private static String stripExistingNegation(String value) {
        String lower = value.toLowerCase(Locale.ROOT);
        if (lower.startsWith("undo ")) return value.substring(5);
        if (lower.startsWith("no ")) return value.substring(3);
        return value;
    }
}
