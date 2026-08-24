package com.chenwei666.netserial.network;

import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public final class MacOuiLookup {
    private static final Map<String, String> VENDORS;
    static {
        Map<String, String> values = new HashMap<>();
        values.put("00:00:0C", "Cisco");
        values.put("00:0F:E2", "H3C");
        values.put("00:18:82", "Huawei");
        values.put("00:1A:A9", "Ruijie");
        VENDORS = Collections.unmodifiableMap(values);
    }

    public MacAddressInfo lookup(String value) {
        if (value == null) throw new IllegalArgumentException("MAC address is required");
        String compact = value.trim().replace("-", "").replace(":", "").replace(".", "")
                .toUpperCase(Locale.ROOT);
        if (!compact.matches("[0-9A-F]{12}")) throw new IllegalArgumentException("invalid MAC address");
        StringBuilder normalized = new StringBuilder();
        for (int index = 0; index < compact.length(); index += 2) {
            if (normalized.length() > 0) normalized.append(':');
            normalized.append(compact, index, index + 2);
        }
        String oui = normalized.substring(0, 8);
        int first = Integer.parseInt(compact.substring(0, 2), 16);
        return new MacAddressInfo(normalized.toString(), oui,
                VENDORS.containsKey(oui) ? VENDORS.get(oui) : "Unknown in bundled catalog",
                (first & 0x02) != 0, (first & 0x01) != 0);
    }
}
