package com.chenwei666.netserial.ai;

@FunctionalInterface
public interface AiProvider {
    AiDraftPlan propose(AiRequest request) throws Exception;

    default AiDraftPlan propose(AiRequest request, RequestCancellation cancellation) throws Exception {
        if (cancellation.isCancelled()) {
            throw new AiProviderException(AiProviderError.CANCELLED,
                    "AI provider request was cancelled", 0, false);
        }
        return propose(request);
    }
}
