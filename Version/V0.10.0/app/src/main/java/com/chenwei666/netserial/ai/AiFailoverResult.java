package com.chenwei666.netserial.ai;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class AiFailoverResult {
    private final CommandPlan plan;
    private final String providerAlias;
    private final List<AiProviderAttempt> attempts;

    AiFailoverResult(CommandPlan plan, String providerAlias, List<AiProviderAttempt> attempts) {
        this.plan = plan;
        this.providerAlias = providerAlias;
        this.attempts = Collections.unmodifiableList(new ArrayList<>(attempts));
    }
    public CommandPlan getPlan() { return plan; }
    public String getProviderAlias() { return providerAlias; }
    public List<AiProviderAttempt> getAttempts() { return attempts; }
}
