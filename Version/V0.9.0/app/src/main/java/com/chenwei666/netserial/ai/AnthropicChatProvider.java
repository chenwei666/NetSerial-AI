package com.chenwei666.netserial.ai;

import java.net.URI;
import java.util.Arrays;
import java.util.Objects;

public final class AnthropicChatProvider implements AiChatProvider {
    private final ProviderProfile profile;
    private final ProviderCredentialService credentials;
    private final UrlConnectionChatHttpTransport transport;
    private final HttpExecutionPolicy policy;
    private final AiChatJsonCodec codec = new AiChatJsonCodec();

    public AnthropicChatProvider(ProviderProfile profile, ProviderCredentialService credentials) {
        this(profile, credentials, new UrlConnectionChatHttpTransport(), HttpExecutionPolicy.defaults());
    }

    AnthropicChatProvider(ProviderProfile profile, ProviderCredentialService credentials,
                          UrlConnectionChatHttpTransport transport, HttpExecutionPolicy policy) {
        this.profile = Objects.requireNonNull(profile, "profile");
        this.credentials = Objects.requireNonNull(credentials, "credentials");
        this.transport = Objects.requireNonNull(transport, "transport");
        this.policy = Objects.requireNonNull(policy, "policy");
    }

    @Override public AiChatResponse chat(AiChatRequest request, RequestCancellation cancellation) {
        byte[] body = codec.encodeAnthropic(profile, request);
        try {
            return credentials.withCredential(profile, credential -> {
                ChatHttpResponse response = transport.post(resolveEndpoint(), body, credential,
                        CredentialHeaderMode.ANTHROPIC_X_API_KEY, policy, cancellation);
                if (response.getStatus() < 200 || response.getStatus() >= 300) {
                    throw AiProviderException.fromHttpStatus(response.getStatus());
                }
                return codec.decodeAnthropic(response.getBody());
            });
        } catch (CredentialVaultException exception) {
            if (exception.getCause() instanceof AiProviderException) {
                throw (AiProviderException) exception.getCause();
            }
            throw exception;
        } finally {
            Arrays.fill(body, (byte) 0);
        }
    }

    private URI resolveEndpoint() {
        String base = profile.getEndpoint().toString().replaceAll("/+$", "");
        return URI.create(base.endsWith("/messages") ? base : base + "/messages");
    }
}
