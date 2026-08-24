package com.chenwei666.netserial.ai;

import java.util.Objects;

public final class AiProviderCandidate {
    private final String alias;
    private final AiCopilot copilot;

    public AiProviderCandidate(String alias, AiCopilot copilot) {
        this.alias = Objects.requireNonNull(alias, "alias").trim();
        if (this.alias.isEmpty() || this.alias.length() > 128) throw new IllegalArgumentException("Invalid alias");
        this.copilot = Objects.requireNonNull(copilot, "copilot");
    }
    public String getAlias() { return alias; }
    AiCopilot getCopilot() { return copilot; }
}
