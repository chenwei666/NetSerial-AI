package com.chenwei666.netserial.ai;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/** Active-provider-first chat with bounded transient retries and provider failover. */
public final class FailoverAiChatService {
    private final List<AiChatProviderCandidate> candidates;
    private final int maxAttemptsPerProvider;

    public FailoverAiChatService(List<AiChatProviderCandidate> candidates, int maxAttemptsPerProvider) {
        if (candidates == null || candidates.isEmpty()) throw new IllegalArgumentException("AI candidates required");
        if (maxAttemptsPerProvider < 1 || maxAttemptsPerProvider > 3) throw new IllegalArgumentException("invalid retry count");
        this.candidates = Collections.unmodifiableList(new ArrayList<>(candidates));
        this.maxAttemptsPerProvider = maxAttemptsPerProvider;
    }

    public AiChatResult chat(AiChatRequest request, RequestCancellation cancellation) throws Exception {
        Objects.requireNonNull(request, "request");
        Objects.requireNonNull(cancellation, "cancellation");
        List<AiProviderAttempt> attempts = new ArrayList<>();
        Exception last = null;
        for (AiChatProviderCandidate candidate : candidates) {
            for (int attempt = 0; attempt < maxAttemptsPerProvider; attempt++) {
                if (cancellation.isCancelled()) throw cancelled();
                long started = System.nanoTime();
                try {
                    AiChatResponse response = candidate.getProvider().chat(request, cancellation);
                    attempts.add(new AiProviderAttempt(candidate.getAlias(), true, false,
                            elapsedMillis(started), null));
                    return new AiChatResult(response, candidate.getAlias(), attempts);
                } catch (Exception exception) {
                    last = exception;
                    AiProviderException provider = findProviderException(exception);
                    if (provider != null && provider.getError() == AiProviderError.CANCELLED) throw provider;
                    boolean retryable = provider != null && provider.isRetryable();
                    attempts.add(new AiProviderAttempt(candidate.getAlias(), false, retryable,
                            elapsedMillis(started), provider == null ? null : provider.getError()));
                    if (!retryable) break;
                }
            }
        }
        throw new AiFailoverException(attempts, last);
    }

    private static long elapsedMillis(long startedNanos) {
        return (System.nanoTime() - startedNanos) / 1_000_000L;
    }

    private static AiProviderException findProviderException(Throwable value) {
        Throwable current = value;
        for (int depth = 0; current != null && depth < 8; depth++, current = current.getCause()) {
            if (current instanceof AiProviderException) return (AiProviderException) current;
        }
        return null;
    }

    private static AiProviderException cancelled() {
        return new AiProviderException(AiProviderError.CANCELLED, "AI chat cancelled", 0, false);
    }
}
