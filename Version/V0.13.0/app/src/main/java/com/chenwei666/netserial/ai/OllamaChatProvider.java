package com.chenwei666.netserial.ai;

import java.util.Arrays;
import java.util.Objects;

/** HTTPS reverse-proxy Ollama chat adapter; no credential header is sent. */
public final class OllamaChatProvider implements AiChatProvider {
    private final ProviderProfile profile;
    private final UrlConnectionChatHttpTransport transport;
    private final AiChatJsonCodec codec = new AiChatJsonCodec();

    public OllamaChatProvider(ProviderProfile profile) {
        this(profile, new UrlConnectionChatHttpTransport());
    }

    OllamaChatProvider(ProviderProfile profile, UrlConnectionChatHttpTransport transport) {
        this.profile = Objects.requireNonNull(profile, "profile");
        this.transport = Objects.requireNonNull(transport, "transport");
    }

    @Override public AiChatResponse chat(AiChatRequest request, RequestCancellation cancellation) {
        byte[] body = codec.encodeOpenAi(profile, request);
        try {
            ChatHttpResponse response = transport.post(ChatEndpointResolver.resolve(profile), body,
                    new char[0], CredentialHeaderMode.NONE, HttpExecutionPolicy.defaults(), cancellation);
            if (response.getStatus() < 200 || response.getStatus() >= 300) {
                throw AiProviderException.fromHttpStatus(response.getStatus());
            }
            return codec.decodeOpenAi(response.getBody());
        } finally {
            Arrays.fill(body, (byte) 0);
        }
    }
}
