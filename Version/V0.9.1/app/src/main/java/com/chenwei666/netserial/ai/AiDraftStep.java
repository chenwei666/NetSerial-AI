package com.chenwei666.netserial.ai;

import com.chenwei666.netserial.safety.RiskLevel;

import java.util.Objects;

public final class AiDraftStep {
    private final String command;
    private final RiskLevel proposedRisk;
    private final PlanPhase phase;

    public AiDraftStep(String command, RiskLevel proposedRisk) {
        this(command, proposedRisk, proposedRisk.ordinal() <= RiskLevel.R1_READ_ONLY.ordinal()
                ? PlanPhase.PRECHECK : PlanPhase.CHANGE);
    }

    public AiDraftStep(String command, RiskLevel proposedRisk, PlanPhase phase) {
        this.command = Objects.requireNonNull(command, "command").trim();
        this.proposedRisk = Objects.requireNonNull(proposedRisk, "proposedRisk");
        this.phase = Objects.requireNonNull(phase, "phase");
        if (this.command.isEmpty()) {
            throw new IllegalArgumentException("command must not be empty");
        }
    }

    public String getCommand() {
        return command;
    }

    public RiskLevel getProposedRisk() {
        return proposedRisk;
    }

    public PlanPhase getPhase() { return phase; }
}
