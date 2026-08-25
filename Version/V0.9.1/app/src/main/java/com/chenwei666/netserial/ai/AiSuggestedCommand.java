package com.chenwei666.netserial.ai;

import com.chenwei666.netserial.safety.RiskLevel;

import java.util.Objects;

public final class AiSuggestedCommand {
    private final String command;
    private final RiskLevel risk;

    public AiSuggestedCommand(String command, RiskLevel risk) {
        this.command = Objects.requireNonNull(command, "command");
        this.risk = Objects.requireNonNull(risk, "risk");
    }

    public String getCommand() { return command; }
    public RiskLevel getRisk() { return risk; }
}
