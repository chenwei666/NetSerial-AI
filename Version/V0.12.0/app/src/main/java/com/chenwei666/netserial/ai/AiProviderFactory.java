package com.chenwei666.netserial.ai;

import java.util.Objects;

public final class AiProviderFactory {
    private AiProviderFactory() { }

    public static AiProvider create(ProviderProfile profile, ProviderCredentialService credentials) {
        Objects.requireNonNull(profile, "profile");
        if ("anthropic".equals(profile.getProviderId())) {
            return new AnthropicProvider(profile, Objects.requireNonNull(credentials, "credentials"));
        }
        if ("ollama".equals(profile.getProviderId())) {
            return new OllamaProvider(profile);
        }
        return OpenAiCompatibleProvider.create(profile,
                Objects.requireNonNull(credentials, "credentials"));
    }
}
