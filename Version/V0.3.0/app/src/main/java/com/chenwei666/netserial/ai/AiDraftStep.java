package com.chenwei666.netserial.ai;

import com.chenwei666.netserial.safety.RiskLevel;

import java.util.Objects;

public final class AiDraftStep {
    private final String command;
    private final RiskLevel proposedRisk;

    public AiDraftStep(String command, RiskLevel proposedRisk) {
        this.command = Objects.requireNonNull(command, "command").trim();
        this.proposedRisk = Objects.requireNonNull(proposedRisk, "proposedRisk");
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
}
