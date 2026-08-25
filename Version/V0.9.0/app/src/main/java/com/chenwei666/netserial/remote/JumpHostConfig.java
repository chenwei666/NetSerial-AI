package com.chenwei666.netserial.remote;

import java.util.Objects;

public final class JumpHostConfig {
    private final String host;
    private final int port;
    private final String username;

    public JumpHostConfig(String host, int port, String username) {
        this.host = text(host, 253, "host");
        if (port < 1 || port > 65535) throw new IllegalArgumentException("invalid jump port");
        this.port = port;
        this.username = text(username, 128, "username");
    }

    public String getHost() { return host; }
    public int getPort() { return port; }
    public String getUsername() { return username; }

    private static String text(String value, int maximum, String field) {
        String normalized = Objects.requireNonNull(value, field).trim();
        if (normalized.isEmpty() || normalized.length() > maximum || normalized.contains("/")
                || normalized.contains("://")) throw new IllegalArgumentException("invalid " + field);
        for (int i = 0; i < normalized.length(); i++) {
            if (Character.isISOControl(normalized.charAt(i)) || Character.isWhitespace(normalized.charAt(i))) {
                throw new IllegalArgumentException("invalid " + field);
            }
        }
        return normalized;
    }
}
