package com.chenwei666.netserial.session;

import com.chenwei666.netserial.remote.RemoteProtocol;

import java.util.Objects;

/** Non-sensitive reusable connection metadata. Passwords and private keys are never included. */
public final class RemoteSessionProfile {
    private final String id;
    private final String label;
    private final RemoteProtocol protocol;
    private final String host;
    private final int port;
    private final String username;

    public RemoteSessionProfile(String id, String label, RemoteProtocol protocol, String host,
                                int port, String username) {
        this.id = require(id, "id", 64);
        this.label = require(label, "label", 64);
        this.protocol = Objects.requireNonNull(protocol, "protocol");
        this.host = validateHost(host);
        if (port < 1 || port > 65535) throw new IllegalArgumentException("port");
        this.port = port;
        this.username = username == null ? "" : username.trim();
        if (!this.username.isEmpty() && !this.username.matches("[A-Za-z0-9._@-]{1,128}")) {
            throw new IllegalArgumentException("username");
        }
    }

    public String getId() { return id; }
    public String getLabel() { return label; }
    public RemoteProtocol getProtocol() { return protocol; }
    public String getHost() { return host; }
    public int getPort() { return port; }
    public String getUsername() { return username; }

    @Override public String toString() { return label + " · " + protocol + " · " + host + ":" + port; }

    private static String require(String value, String field, int max) {
        String normalized = Objects.requireNonNull(value, field).trim();
        if (normalized.isEmpty() || normalized.length() > max) throw new IllegalArgumentException(field);
        return normalized;
    }

    private static String validateHost(String value) {
        String host = require(value, "host", 253);
        if (!host.matches("[A-Za-z0-9][A-Za-z0-9.:-]{0,252}") || host.contains("://")) {
            throw new IllegalArgumentException("host");
        }
        return host;
    }
}
