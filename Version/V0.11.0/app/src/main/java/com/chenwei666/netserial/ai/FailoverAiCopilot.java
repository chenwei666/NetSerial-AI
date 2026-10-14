package com.chenwei666.netserial.ai;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Tries the active provider first, retries transient failures, then fails over in configured order. */
public final class FailoverAiCopilot {
    private final List<AiProviderCandidate> candidates;
    private final int maxAttemptsPerProvider;

    public FailoverAiCopilot(List<AiProviderCandidate> candidates, int maxAttemptsPerProvider) {
        if (candidates == null || candidates.isEmpty()) throw new IllegalArgumentException("AI candidates required");
        if (maxAttemptsPerProvider < 1 || maxAttemptsPerProvider > 3) throw new IllegalArgumentException("Invalid retry count");
        this.candidates = Collections.unmodifiableList(new ArrayList<>(candidates));
        this.maxAttemptsPerProvider = maxAttemptsPerProvider;
    }

    public AiFailoverResult propose(AiRequest request) throws Exception {
        List<AiProviderAttempt> attempts = new ArrayList<>();
        Exception last = null;
        for (AiProviderCandidate candidate : candidates) {
            for (int attempt = 0; attempt < maxAttemptsPerProvider; attempt++) {
                long started = System.nanoTime();
                try {
                    CommandPlan plan = candidate.getCopilot().propose(request);
                    attempts.add(new AiProviderAttempt(candidate.getAlias(), true, false,
                            elapsedMillis(started), null));
                    return new AiFailoverResult(plan, candidate.getAlias(), attempts);
                } catch (Exception exception) {
                    last = exception;
                    AiProviderException provider = findProviderException(exception);
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
        for (int i = 0; current != null && i < 8; i++, current = current.getCause()) {
            if (current instanceof AiProviderException) return (AiProviderException) current;
        }
        return null;
    }
}
