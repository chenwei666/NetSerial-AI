package com.chenwei666.netserial.ai;

import java.util.List;
import java.util.Objects;

public final class AiModelCatalogService {
    private final ModelCatalogHttpTransport transport;
    private final ModelCatalogJsonCodec codec;

    public AiModelCatalogService() {
        this(new UrlConnectionModelCatalogTransport(), new ModelCatalogJsonCodec());
    }

    AiModelCatalogService(ModelCatalogHttpTransport transport, ModelCatalogJsonCodec codec) {
        this.transport = Objects.requireNonNull(transport, "transport");
        this.codec = Objects.requireNonNull(codec, "codec");
    }

    public List<String> fetch(ProviderProfile profile, char[] credential,
                              RequestCancellation cancellation) {
        Objects.requireNonNull(profile, "profile");
        Objects.requireNonNull(credential, "credential");
        String providerId = profile.getProviderId();
        CredentialHeaderMode headerMode = "ollama".equals(providerId)
                ? CredentialHeaderMode.NONE
                : "anthropic".equals(providerId)
                ? CredentialHeaderMode.ANTHROPIC_X_API_KEY
                : CredentialHeaderMode.BEARER;
        ModelCatalogFormat format = "ollama".equals(providerId)
                ? ModelCatalogFormat.OLLAMA
                : "qwen".equals(providerId)
                ? ModelCatalogFormat.QWEN
                : ModelCatalogFormat.OPENAI;
        ChatHttpResponse response = transport.get(
                ModelCatalogEndpointResolver.resolve(profile), credential, headerMode,
                HttpExecutionPolicy.defaults(), cancellation);
        if (response.getStatus() < 200 || response.getStatus() >= 300) {
            throw AiProviderException.fromHttpStatus(response.getStatus());
        }
        return codec.decode(response.getBody(), format);
    }
}
