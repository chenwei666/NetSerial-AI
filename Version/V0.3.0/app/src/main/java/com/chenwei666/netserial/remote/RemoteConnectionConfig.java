package com.chenwei666.netserial.remote;

import java.nio.charset.Charset;
import java.util.Objects;

public final class RemoteConnectionConfig {
    private final RemoteProtocol protocol;
    private final String host;
    private final int port;
    private final String username;
    private final int timeoutMillis;
    private final Charset charset;

    public RemoteConnectionConfig(RemoteProtocol protocol, String host, int port, String username,
                                  int timeoutMillis, String charsetName) {
        this.protocol = Objects.requireNonNull(protocol, "protocol");
        this.host = validateHost(host);
        if (port < 1 || port > 65535) throw new IllegalArgumentException("port must be between 1 and 65535");
        this.port = port;
        this.username = username == null ? "" : username.trim();
        if (this.username.length() > 128 || containsControl(this.username)) {
            throw new IllegalArgumentException("username is invalid");
        }
        if (protocol == RemoteProtocol.SSH && this.username.isEmpty()) {
            throw new IllegalArgumentException("username is required for SSH");
        }
        if (timeoutMillis < 2_000 || timeoutMillis > 60_000) {
            throw new IllegalArgumentException("timeout must be between 2 and 60 seconds");
        }
        this.timeoutMillis = timeoutMillis;
        this.charset = parseCharset(charsetName);
    }

    public RemoteProtocol getProtocol() { return protocol; }
    public String getHost() { return host; }
    public int getPort() { return port; }
    public String getUsername() { return username; }
    public int getTimeoutMillis() { return timeoutMillis; }
    public Charset getCharset() { return charset; }

    private static String validateHost(String value) {
        String normalized = Objects.requireNonNull(value, "host").trim();
        if (normalized.isEmpty() || normalized.length() > 253 || containsControl(normalized)
                || normalized.contains("/") || normalized.contains("://")) {
            throw new IllegalArgumentException("host must be a hostname or IP address without a URL scheme");
        }
        return normalized;
    }

    private static boolean containsControl(String value) {
        for (int i = 0; i < value.length(); i++) {
            if (Character.isISOControl(value.charAt(i)) || Character.isWhitespace(value.charAt(i))) return true;
        }
        return false;
    }

    private static Charset parseCharset(String charsetName) {
        try {
            if (charsetName == null || charsetName.length() > 32 || !Charset.isSupported(charsetName)) {
                throw new IllegalArgumentException("unsupported character encoding");
            }
            return Charset.forName(charsetName);
        } catch (RuntimeException exception) {
            throw new IllegalArgumentException("unsupported character encoding", exception);
        }
    }
}
