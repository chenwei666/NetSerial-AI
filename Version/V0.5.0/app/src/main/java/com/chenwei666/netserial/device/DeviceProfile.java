package com.chenwei666.netserial.device;

import java.util.Objects;

public final class DeviceProfile {
    private final String name;
    private final Vendor vendor;
    private final CliMode cliMode;
    private final int baudRate;
    private final DeviceEnvironment environment;
    private final boolean protectedDevice;
    private final String managementAddress;

    public DeviceProfile(String name, Vendor vendor, CliMode cliMode, int baudRate) {
        this(name, vendor, cliMode, baudRate, DeviceEnvironment.LAB, false);
    }

    public DeviceProfile(String name, Vendor vendor, CliMode cliMode, int baudRate,
                         DeviceEnvironment environment, boolean protectedDevice) {
        this(name, vendor, cliMode, baudRate, environment, protectedDevice, "");
    }

    public DeviceProfile(String name, Vendor vendor, CliMode cliMode, int baudRate,
                         DeviceEnvironment environment, boolean protectedDevice,
                         String managementAddress) {
        this.name = requireText(name, "name");
        this.vendor = Objects.requireNonNull(vendor, "vendor");
        this.cliMode = Objects.requireNonNull(cliMode, "cliMode");
        if (baudRate < 300 || baudRate > 4_000_000) {
            throw new IllegalArgumentException("baudRate is outside the supported range");
        }
        this.baudRate = baudRate;
        this.environment = Objects.requireNonNull(environment, "environment");
        this.protectedDevice = protectedDevice;
        this.managementAddress = optionalAddress(managementAddress);
    }

    public static DeviceProfile defaults() {
        return new DeviceProfile("Default switch", Vendor.H3C_COMWARE, CliMode.USER_VIEW, 9600);
    }

    public String getName() { return name; }
    public Vendor getVendor() { return vendor; }
    public CliMode getCliMode() { return cliMode; }
    public int getBaudRate() { return baudRate; }
    public DeviceEnvironment getEnvironment() { return environment; }
    public boolean isProtectedDevice() { return protectedDevice; }
    public String getManagementAddress() { return managementAddress; }

    private static String requireText(String value, String field) {
        String normalized = Objects.requireNonNull(value, field).trim();
        if (normalized.isEmpty() || normalized.length() > 128) {
            throw new IllegalArgumentException(field + " must contain 1 to 128 characters");
        }
        return normalized;
    }

    private static String optionalAddress(String value) {
        String normalized = value == null ? "" : value.trim();
        if (normalized.length() > 253 || normalized.contains("/") || normalized.contains("://")) {
            throw new IllegalArgumentException("invalid management address");
        }
        for (int index = 0; index < normalized.length(); index++) {
            if (Character.isWhitespace(normalized.charAt(index)) || Character.isISOControl(normalized.charAt(index))) {
                throw new IllegalArgumentException("invalid management address");
            }
        }
        return normalized;
    }
}
