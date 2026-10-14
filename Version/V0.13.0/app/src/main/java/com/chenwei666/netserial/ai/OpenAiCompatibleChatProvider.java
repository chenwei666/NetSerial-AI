package com.chenwei666.netserial.ai;

import java.net.URI;
import java.util.Arrays;
import java.util.Objects;

public final class OpenAiCompatibleChatProvider implements AiChatProvider {
    private final ProviderProfile profile;
    private final ProviderCredentialService credentials;
    private final ChatHttpTransport transport;
    private final HttpExecutionPolicy policy;
    private final AiChatJsonCodec codec = new AiChatJsonCodec();

    public OpenAiCompatibleChatProvider(ProviderProfile profile,
                                        ProviderCredentialService credentials) {
        this(profile, credentials, new UrlConnectionChatHttpTransport(), HttpExecutionPolicy.defaults());
    }

    OpenAiCompatibleChatProvider(ProviderProfile profile, ProviderCredentialService credentials,
                                 ChatHttpTransport transport, HttpExecutionPolicy policy) {
        this.profile = Objects.requireNonNull(profile, "profile");
        this.credentials = Objects.requireNonNull(credentials, "credentials");
        this.transport = Objects.requireNonNull(transport, "transport");
        this.policy = Objects.requireNonNull(policy, "policy");
    }

    @Override public AiChatResponse chat(AiChatRequest request, RequestCancellation cancellation) {
        byte[] body = codec.encodeOpenAi(profile, request);
        URI endpoint = ChatEndpointResolver.resolve(profile);
        try {
            return credentials.withCredential(profile, credential -> {
                ChatHttpResponse response = transport.post(endpoint, body, credential, policy, cancellation);
                if (response.getStatus() < 200 || response.getStatus() >= 300) {
                    throw AiProviderException.fromHttpStatus(response.getStatus());
                }
                return codec.decodeOpenAi(response.getBody());
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
}
