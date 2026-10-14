package com.chenwei666.netserial.ai;

import java.net.URI;
import java.util.Arrays;
import java.util.Objects;

public final class OpenAiCompatibleProvider implements AiProvider {
    private final ProviderProfile profile;
    private final ProviderCredentialService credentialService;
    private final ChatHttpTransport transport;
    private final HttpExecutionPolicy policy;
    private final OpenAiCompatibleJsonCodec codec;

    public OpenAiCompatibleProvider(
            ProviderProfile profile,
            ProviderCredentialService credentialService,
            ChatHttpTransport transport,
            HttpExecutionPolicy policy
    ) {
        this.profile = Objects.requireNonNull(profile, "profile");
        this.credentialService = Objects.requireNonNull(
                credentialService,
                "credentialService"
        );
        this.transport = Objects.requireNonNull(transport, "transport");
        this.policy = Objects.requireNonNull(policy, "policy");
        this.codec = new OpenAiCompatibleJsonCodec();
    }

    public static OpenAiCompatibleProvider create(
            ProviderProfile profile,
            ProviderCredentialService credentialService
    ) {
        return new OpenAiCompatibleProvider(
                profile,
                credentialService,
                new UrlConnectionChatHttpTransport(),
                HttpExecutionPolicy.defaults()
        );
    }

    @Override
    public AiDraftPlan propose(AiRequest request) {
        return propose(request, new RequestCancellation());
    }

    public AiDraftPlan propose(AiRequest request, RequestCancellation cancellation) {
        Objects.requireNonNull(request, "request");
        Objects.requireNonNull(cancellation, "cancellation");
        if (cancellation.isCancelled()) {
            throw new AiProviderException(
                    AiProviderError.CANCELLED,
                    "AI provider request was cancelled",
                    0,
                    false
            );
        }

        URI endpoint = ChatEndpointResolver.resolve(profile);
        byte[] requestBody = codec.encodeRequest(profile, request);
        try {
            return credentialService.withCredential(profile, credential -> {
                ChatHttpResponse response = transport.post(
                        endpoint,
                        requestBody,
                        credential,
                        policy,
                        cancellation
                );
                if (response.getStatus() < 200 || response.getStatus() >= 300) {
                    throw AiProviderException.fromHttpStatus(response.getStatus());
                }
                return codec.decodeResponse(response.getBody());
            });
        } catch (CredentialVaultException exception) {
            if (exception.getCause() instanceof AiProviderException) {
                throw (AiProviderException) exception.getCause();
            }
            throw exception;
        } finally {
            Arrays.fill(requestBody, (byte) 0);
        }
    }
}
