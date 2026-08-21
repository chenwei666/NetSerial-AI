package com.chenwei666.netserial.ai;

import com.chenwei666.netserial.safety.RiskLevel;

public final class EvaluatedCommandStep {
    private final String command;
    private final RiskLevel effectiveRisk;
    private final boolean automaticExecutionAllowed;
    private final PlanPhase phase;

    public EvaluatedCommandStep(String command, RiskLevel effectiveRisk,
                                boolean automaticExecutionAllowed) {
        this(command, effectiveRisk, automaticExecutionAllowed,
                effectiveRisk.ordinal() <= RiskLevel.R1_READ_ONLY.ordinal()
                        ? PlanPhase.PRECHECK : PlanPhase.CHANGE);
    }

    public EvaluatedCommandStep(String command, RiskLevel effectiveRisk,
                                boolean automaticExecutionAllowed, PlanPhase phase) {
        this.command = command;
        this.effectiveRisk = effectiveRisk;
        this.automaticExecutionAllowed = automaticExecutionAllowed;
        this.phase = java.util.Objects.requireNonNull(phase, "phase");
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

    public PlanPhase getPhase() { return phase; }
}
