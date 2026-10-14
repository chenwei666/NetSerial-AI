package com.chenwei666.netserial.ai;

import java.util.Objects;

public final class AiChatMessage {
    public static final int MAX_CONTENT_CHARACTERS = 24_000;

    private final AiChatRole role;
    private final String content;
    private final long createdAtMillis;

    public AiChatMessage(AiChatRole role, String content, long createdAtMillis) {
        this.role = Objects.requireNonNull(role, "role");
        this.content = requireContent(content);
        if (createdAtMillis <= 0) throw new IllegalArgumentException("createdAtMillis must be positive");
        this.createdAtMillis = createdAtMillis;
    }

    public AiChatRole getRole() { return role; }
    public String getContent() { return content; }
    public long getCreatedAtMillis() { return createdAtMillis; }

    private static String requireContent(String value) {
        String normalized = Objects.requireNonNull(value, "content").trim();
        if (normalized.isEmpty()) throw new IllegalArgumentException("content must not be empty");
        if (normalized.length() > MAX_CONTENT_CHARACTERS) {
            throw new IllegalArgumentException("content is too long");
        }
        if (normalized.indexOf('\0') >= 0) throw new IllegalArgumentException("content contains NUL");
        return normalized;
    }
}
