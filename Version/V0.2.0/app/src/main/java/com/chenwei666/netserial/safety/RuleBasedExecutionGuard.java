package com.chenwei666.netserial.safety;

import java.util.Locale;

public final class RuleBasedExecutionGuard implements ExecutionGuard {

    private RuleBasedExecutionGuard() {
    }

    public static RuleBasedExecutionGuard createDefault() {
        return new RuleBasedExecutionGuard();
    }

    @Override
    public GuardDecision evaluate(CommandEvaluationRequest request) {
        RiskLevel deterministicRisk = classify(request.getCommand());
        RiskLevel effectiveRisk = RiskLevel.max(deterministicRisk, request.getProposedRisk());
        boolean critical = effectiveRisk == RiskLevel.R4_CRITICAL;
        return new GuardDecision(effectiveRisk, false, critical);
    }

    private RiskLevel classify(String command) {
        String normalized = command.trim().toLowerCase(Locale.ROOT);
        if (normalized.equals("reboot") || normalized.startsWith("reboot ")) {
            return RiskLevel.R4_CRITICAL;
        }
        if ((normalized.equals("display") || normalized.startsWith("display "))) {
            return RiskLevel.R1_READ_ONLY;
        }
        return RiskLevel.R0_INFORMATIONAL;
    }
}
