package com.chenwei666.netserial.ai;

import java.util.Objects;

public final class AiProviderPreset {
    private final String providerId;
    private final String endpoint;
    private final String model;
    private final boolean openAiCompatible;

    public AiProviderPreset(
            String providerId,
            String endpoint,
            String model,
            boolean openAiCompatible
    ) {
        this.providerId = Objects.requireNonNull(providerId, "providerId");
        this.endpoint = Objects.requireNonNull(endpoint, "endpoint");
        this.model = Objects.requireNonNull(model, "model");
        this.openAiCompatible = openAiCompatible;
    }

    public String getProviderId() {
        return providerId;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public String getModel() {
        return model;
    }

    public boolean isOpenAiCompatible() {
        return openAiCompatible;
    }
}
