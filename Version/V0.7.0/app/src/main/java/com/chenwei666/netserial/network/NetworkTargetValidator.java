package com.chenwei666.netserial.network;

import java.util.Objects;
import java.util.regex.Pattern;

public final class NetworkTargetValidator {
    private static final Pattern HOST = Pattern.compile(
            "(?i)^(?:[a-z0-9](?:[a-z0-9-]{0,61}[a-z0-9])?)(?:\\.(?:[a-z0-9](?:[a-z0-9-]{0,61}[a-z0-9])?))*\\.?$|^[0-9a-f:]+$");

    public String validate(String value) {
        String normalized = Objects.requireNonNull(value, "target").trim();
        if (normalized.isEmpty() || normalized.length() > 253 || !HOST.matcher(normalized).matches()) {
            throw new IllegalArgumentException("invalid hostname or IP address");
        }
        return normalized;
    }

    public int validatePort(String value) {
        int port;
        try { port = Integer.parseInt(Objects.requireNonNull(value, "port").trim()); }
        catch (RuntimeException exception) { throw new IllegalArgumentException("invalid port", exception); }
        if (port < 1 || port > 65535) throw new IllegalArgumentException("invalid port");
        return port;
    }
}
