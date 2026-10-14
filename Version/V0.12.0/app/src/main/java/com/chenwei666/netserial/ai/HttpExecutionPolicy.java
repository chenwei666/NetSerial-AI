package com.chenwei666.netserial.ai;

public final class HttpExecutionPolicy {
    private static final int MIN_TIMEOUT_MILLIS = 1_000;
    private static final int MAX_CONNECT_TIMEOUT_MILLIS = 60_000;
    private static final int MAX_READ_TIMEOUT_MILLIS = 120_000;
    private static final int MIN_RESPONSE_BYTES = 1_024;
    private static final int MAX_RESPONSE_BYTES = 1_048_576;

    private final int connectTimeoutMillis;
    private final int readTimeoutMillis;
    private final int maxResponseBytes;

    public HttpExecutionPolicy(
            int connectTimeoutMillis,
            int readTimeoutMillis,
            int maxResponseBytes
    ) {
        this.connectTimeoutMillis = requireRange(
                connectTimeoutMillis,
                MIN_TIMEOUT_MILLIS,
                MAX_CONNECT_TIMEOUT_MILLIS,
                "connectTimeoutMillis"
        );
        this.readTimeoutMillis = requireRange(
                readTimeoutMillis,
                MIN_TIMEOUT_MILLIS,
                MAX_READ_TIMEOUT_MILLIS,
                "readTimeoutMillis"
        );
        this.maxResponseBytes = requireRange(
                maxResponseBytes,
                MIN_RESPONSE_BYTES,
                MAX_RESPONSE_BYTES,
                "maxResponseBytes"
        );
    }

    public static HttpExecutionPolicy defaults() {
        return new HttpExecutionPolicy(10_000, 60_000, 524_288);
    }

    public int getConnectTimeoutMillis() {
        return connectTimeoutMillis;
    }

    public int getReadTimeoutMillis() {
        return readTimeoutMillis;
    }

    public int getMaxResponseBytes() {
        return maxResponseBytes;
    }

    private static int requireRange(int value, int min, int max, String name) {
        if (value < min || value > max) {
            throw new IllegalArgumentException(name + " is outside the allowed range");
        }
        return value;
    }
}
