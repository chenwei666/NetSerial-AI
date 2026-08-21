package com.chenwei666.netserial.ai;

import com.chenwei666.netserial.safety.RiskLevel;

public final class EvaluatedCommandStep {
    private final String command;
    private final RiskLevel effectiveRisk;
    private final boolean automaticExecutionAllowed;

    public EvaluatedCommandStep(String command, RiskLevel effectiveRisk,
                                boolean automaticExecutionAllowed) {
        this.command = command;
        this.effectiveRisk = effectiveRisk;
        this.automaticExecutionAllowed = automaticExecutionAllowed;
    }

    public String getCommand() {
        return command;
    }

    public RiskLevel getEffectiveRisk() {
        return effectiveRisk;
    }

    public boolean isAutomaticExecutionAllowed() {
        return automaticExecutionAllowed;
    }
}
