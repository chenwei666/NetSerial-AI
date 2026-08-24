package com.chenwei666.netserial.ai;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class AiFailoverException extends Exception {
    private final List<AiProviderAttempt> attempts;
    AiFailoverException(List<AiProviderAttempt> attempts, Throwable cause) {
        super("All configured AI providers failed", cause);
        this.attempts = Collections.unmodifiableList(new ArrayList<>(attempts));
    }
    public List<AiProviderAttempt> getAttempts() { return attempts; }
}
