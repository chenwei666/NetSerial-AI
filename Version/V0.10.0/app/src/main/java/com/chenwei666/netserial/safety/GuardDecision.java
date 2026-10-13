package com.chenwei666.netserial.safety;

public final class GuardDecision {
    private final RiskLevel effectiveRisk;
    private final boolean automaticExecutionAllowed;
    private final boolean typedConfirmationRequired;

    public GuardDecision(RiskLevel effectiveRisk, boolean automaticExecutionAllowed,
                         boolean typedConfirmationRequired) {
        this.effectiveRisk = effectiveRisk;
        this.automaticExecutionAllowed = automaticExecutionAllowed;
        this.typedConfirmationRequired = typedConfirmationRequired;
    }

    public RiskLevel getEffectiveRisk() {
        return effectiveRisk;
    }

    public boolean isAutomaticExecutionAllowed() {
        return automaticExecutionAllowed;
    }

    public boolean isTypedConfirmationRequired() {
        return typedConfirmationRequired;
    }
}
