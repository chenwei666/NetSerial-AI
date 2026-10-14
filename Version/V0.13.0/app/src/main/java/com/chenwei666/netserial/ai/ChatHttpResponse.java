package com.chenwei666.netserial.ai;

import java.util.Arrays;

public final class ChatHttpResponse {
    private final int status;
    private final byte[] body;

    public ChatHttpResponse(int status, byte[] body) {
        if (status < 100 || status > 599) {
            throw new IllegalArgumentException("status must be a valid HTTP status");
        }
        if (body == null) {
            throw new NullPointerException("body");
        }
        this.status = status;
        this.body = Arrays.copyOf(body, body.length);
    }

    public int getStatus() {
        return status;
    }

    public byte[] getBody() {
        return Arrays.copyOf(body, body.length);
    }
}
