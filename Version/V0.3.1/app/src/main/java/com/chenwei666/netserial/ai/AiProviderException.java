package com.chenwei666.netserial.ai;

public final class AiProviderException extends RuntimeException {
    private final AiProviderError error;
    private final int httpStatus;
    private final boolean retryable;

    public AiProviderException(
            AiProviderError error,
            String safeMessage,
            int httpStatus,
            boolean retryable
    ) {
        super(safeMessage);
        this.error = error;
        this.httpStatus = httpStatus;
        this.retryable = retryable;
    }

    public AiProviderException(
            AiProviderError error,
            String safeMessage,
            boolean retryable,
            Throwable cause
    ) {
        super(safeMessage, cause);
        this.error = error;
        this.httpStatus = 0;
        this.retryable = retryable;
    }

    public AiProviderError getError() {
        return error;
    }

    public int getHttpStatus() {
        return httpStatus;
    }

    public boolean isRetryable() {
        return retryable;
    }

    static AiProviderException fromHttpStatus(int status) {
        if (status == 401 || status == 403) {
            return new AiProviderException(
                    AiProviderError.AUTHENTICATION,
                    "AI provider rejected the credential",
                    status,
                    false
            );
        }
        if (status == 408) {
            return new AiProviderException(
                    AiProviderError.TIMEOUT,
                    "AI provider request timed out",
                    status,
                    true
            );
        }
        if (status == 429) {
            return new AiProviderException(
                    AiProviderError.RATE_LIMIT,
                    "AI provider rate limit reached",
                    status,
                    true
            );
        }
        if (status >= 500) {
            return new AiProviderException(
                    AiProviderError.SERVER,
                    "AI provider server error",
                    status,
                    true
            );
        }
        return new AiProviderException(
                AiProviderError.HTTP,
                "AI provider returned an HTTP error",
                status,
                false
        );
    }
}
