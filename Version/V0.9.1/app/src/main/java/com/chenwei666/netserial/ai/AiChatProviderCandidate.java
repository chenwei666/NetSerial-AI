package com.chenwei666.netserial.ai;

import java.util.Objects;

public final class AiChatProviderCandidate {
    private final String alias;
    private final AiChatProvider provider;

    public AiChatProviderCandidate(String alias, AiChatProvider provider) {
        String normalized = Objects.requireNonNull(alias, "alias").trim();
        if (normalized.isEmpty()) throw new IllegalArgumentException("alias required");
        this.alias = normalized;
        this.provider = Objects.requireNonNull(provider, "provider");
    }

    public String getAlias() { return alias; }
    public AiChatProvider getProvider() { return provider; }
}
