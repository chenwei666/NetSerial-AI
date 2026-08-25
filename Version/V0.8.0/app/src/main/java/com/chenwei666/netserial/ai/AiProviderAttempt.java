package com.chenwei666.netserial.ai;

public final class AiProviderAttempt {
    private final String alias;
    private final boolean successful;
    private final boolean retryable;
    private final long durationMillis;
    private final AiProviderError error;

    AiProviderAttempt(String alias, boolean successful, boolean retryable,
                      long durationMillis, AiProviderError error) {
        this.alias = alias;
        this.successful = successful;
        this.retryable = retryable;
        this.durationMillis = Math.max(0, durationMillis);
        this.error = error;
    }
    public String getAlias() { return alias; }
    public boolean isSuccessful() { return successful; }
    public boolean isRetryable() { return retryable; }
    public long getDurationMillis() { return durationMillis; }
    public AiProviderError getError() { return error; }
}
