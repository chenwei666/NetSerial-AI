package com.chenwei666.netserial.device;

import java.util.Objects;

public final class DeviceProfile {
    private final String name;
    private final Vendor vendor;
    private final CliMode cliMode;
    private final int baudRate;

    public DeviceProfile(String name, Vendor vendor, CliMode cliMode, int baudRate) {
        this.name = requireText(name, "name");
        this.vendor = Objects.requireNonNull(vendor, "vendor");
        this.cliMode = Objects.requireNonNull(cliMode, "cliMode");
        if (baudRate < 300 || baudRate > 4_000_000) {
            throw new IllegalArgumentException("baudRate is outside the supported range");
        }
        this.baudRate = baudRate;
    }

    public static DeviceProfile defaults() {
        return new DeviceProfile("Default switch", Vendor.H3C_COMWARE, CliMode.USER_VIEW, 9600);
    }

    public String getName() { return name; }
    public Vendor getVendor() { return vendor; }
    public CliMode getCliMode() { return cliMode; }
    public int getBaudRate() { return baudRate; }

    private static String requireText(String value, String field) {
        String normalized = Objects.requireNonNull(value, field).trim();
        if (normalized.isEmpty() || normalized.length() > 128) {
            throw new IllegalArgumentException(field + " must contain 1 to 128 characters");
        }
        return normalized;
    }
}
