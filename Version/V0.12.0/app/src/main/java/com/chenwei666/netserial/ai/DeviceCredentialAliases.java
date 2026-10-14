package com.chenwei666.netserial.ai;

/** Creates a separate, validated namespace for switch login and SNMP credentials. */
public final class DeviceCredentialAliases {
    private DeviceCredentialAliases() { }

    public static String vaultKey(String alias) {
        String value = alias == null ? "" : alias.trim();
        if (value.isEmpty() || value.length() > 100 || !value.matches("[A-Za-z0-9._@:/-]+")) {
            throw new IllegalArgumentException("Invalid device credential alias");
        }
        return "device:" + value;
    }
}
