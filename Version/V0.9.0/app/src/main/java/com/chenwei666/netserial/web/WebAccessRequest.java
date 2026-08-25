package com.chenwei666.netserial.web;

import com.chenwei666.netserial.device.Vendor;

import java.util.Arrays;
import java.util.Objects;

public final class WebAccessRequest implements AutoCloseable {
    private final Vendor vendor;
    private final String platform;
    private final String username;
    private final char[] password;
    private final boolean httpsEnabled;
    private final boolean httpEnabled;

    public WebAccessRequest(Vendor vendor, String platform, String username, char[] password,
                            boolean httpsEnabled, boolean httpEnabled) {
        this.vendor = Objects.requireNonNull(vendor, "vendor");
        if (vendor == Vendor.GENERIC) throw new IllegalArgumentException("supported vendor required");
        this.platform = platform == null ? "" : platform.trim();
        this.username = validateUsername(username);
        this.password = validatePassword(password);
        if (!httpsEnabled && !httpEnabled) throw new IllegalArgumentException("enable at least one protocol");
        this.httpsEnabled = httpsEnabled;
        this.httpEnabled = httpEnabled;
    }

    public Vendor getVendor() { return vendor; }
    public String getPlatform() { return platform; }
    public String getUsername() { return username; }
    public char[] copyPassword() { return Arrays.copyOf(password, password.length); }
    public boolean isHttpsEnabled() { return httpsEnabled; }
    public boolean isHttpEnabled() { return httpEnabled; }

    @Override public void close() { Arrays.fill(password, '\0'); }

    private static String validateUsername(String value) {
        String normalized = Objects.requireNonNull(value, "username").trim();
        if (!normalized.matches("[A-Za-z0-9._-]{1,32}")) {
            throw new IllegalArgumentException("username must use letters, digits, dot, underscore or hyphen");
        }
        return normalized;
    }

    private static char[] validatePassword(char[] value) {
        Objects.requireNonNull(value, "password");
        if (value.length < 8 || value.length > 64) throw new IllegalArgumentException("password length");
        char[] copy = Arrays.copyOf(value, value.length);
        boolean letter = false;
        boolean digit = false;
        for (char c : copy) {
            if (Character.isLetter(c)) letter = true;
            if (Character.isDigit(c)) digit = true;
            if (c < 33 || c > 126 || "\\\"'`;?".indexOf(c) >= 0) {
                Arrays.fill(copy, '\0');
                throw new IllegalArgumentException("password contains unsafe CLI characters");
            }
        }
        if (!letter || !digit) {
            Arrays.fill(copy, '\0');
            throw new IllegalArgumentException("password must include letters and digits");
        }
        return copy;
    }
}
