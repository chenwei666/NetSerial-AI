package com.chenwei666.netserial.ai;

import java.util.Objects;

public final class AiChatProviderFactory {
    private AiChatProviderFactory() { }

    public static AiChatProvider create(ProviderProfile profile,
                                        ProviderCredentialService credentials) {
        Objects.requireNonNull(profile, "profile");
        if ("anthropic".equals(profile.getProviderId())) {
            return new AnthropicChatProvider(profile, Objects.requireNonNull(credentials, "credentials"));
        }
        if ("ollama".equals(profile.getProviderId())) return new OllamaChatProvider(profile);
        return new OpenAiCompatibleChatProvider(profile,
                Objects.requireNonNull(credentials, "credentials"));
    }
}
