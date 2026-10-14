package com.chenwei666.netserial.ai;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class AiChatResult {
    private final AiChatResponse response;
    private final String providerAlias;
    private final List<AiProviderAttempt> attempts;

    public AiChatResult(AiChatResponse response, String providerAlias, List<AiProviderAttempt> attempts) {
        this.response = Objects.requireNonNull(response, "response");
        this.providerAlias = Objects.requireNonNull(providerAlias, "providerAlias");
        this.attempts = Collections.unmodifiableList(new ArrayList<>(attempts));
    }

    public AiChatResponse getResponse() { return response; }
    public String getProviderAlias() { return providerAlias; }
    public List<AiProviderAttempt> getAttempts() { return attempts; }
}
