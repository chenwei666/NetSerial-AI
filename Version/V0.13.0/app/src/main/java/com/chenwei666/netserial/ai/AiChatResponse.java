package com.chenwei666.netserial.ai;

import java.util.Objects;

public final class AiChatResponse {
    private final String content;

    public AiChatResponse(String content) {
        String normalized = Objects.requireNonNull(content, "content").trim();
        if (normalized.isEmpty() || normalized.length() > AiChatMessage.MAX_CONTENT_CHARACTERS) {
            throw new IllegalArgumentException("invalid AI chat content");
        }
        this.content = normalized;
    }

    public String getContent() { return content; }
}
