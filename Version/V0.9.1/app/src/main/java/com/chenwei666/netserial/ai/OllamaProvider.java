package com.chenwei666.netserial.ai;

import java.net.URI;
import java.util.Arrays;
import java.util.Objects;

/** Ollama adapter for an HTTPS reverse-proxy endpoint; no API key is transmitted. */
public final class OllamaProvider implements AiProvider {
    private final ProviderProfile profile;
    private final UrlConnectionChatHttpTransport transport;
    private final OpenAiCompatibleJsonCodec codec = new OpenAiCompatibleJsonCodec();

    public OllamaProvider(ProviderProfile profile) {
        this(profile, new UrlConnectionChatHttpTransport());
    }

    OllamaProvider(ProviderProfile profile, UrlConnectionChatHttpTransport transport) {
        this.profile = Objects.requireNonNull(profile, "profile");
        this.transport = Objects.requireNonNull(transport, "transport");
    }

    @Override public AiDraftPlan propose(AiRequest request) {
        return propose(request, new RequestCancellation());
    }

    @Override public AiDraftPlan propose(AiRequest request, RequestCancellation cancellation) {
        byte[] body = codec.encodeRequest(profile, request);
        try {
            ChatHttpResponse response = transport.post(ChatEndpointResolver.resolve(profile), body,
                    new char[0], CredentialHeaderMode.NONE, HttpExecutionPolicy.defaults(),
                    cancellation);
            if (response.getStatus() < 200 || response.getStatus() >= 300) {
                throw AiProviderException.fromHttpStatus(response.getStatus());
            }
            return codec.decodeResponse(response.getBody());
        } finally {
            Arrays.fill(body, (byte) 0);
        }
    }
}
