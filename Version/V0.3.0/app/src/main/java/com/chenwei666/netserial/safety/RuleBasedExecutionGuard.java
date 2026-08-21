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
        if (matchesAny(normalized, "reboot", "reload", "reset saved-configuration",
                "erase startup-config", "format", "upgrade", "delete /unreserved")) {
            return RiskLevel.R4_CRITICAL;
        }
        if (matchesAny(normalized, "shutdown", "undo interface", "no interface",
                "aaa", "local-user", "radius-server", "tacacs", "ip route",
                "ip route-static", "stp root", "undo stp", "no spanning-tree")) {
            return RiskLevel.R3_HIGH;
        }
        if (matchesAny(normalized, "interface", "vlan", "description", "port ",
                "switchport", "undo shutdown", "no shutdown", "speed", "duplex")) {
            return RiskLevel.R2_CONFIGURATION;
        }
        if (matchesAny(normalized, "display", "show", "ping", "tracert", "traceroute")) {
            return RiskLevel.R1_READ_ONLY;
        }
        return normalized.isEmpty() ? RiskLevel.R0_INFORMATIONAL : RiskLevel.R2_CONFIGURATION;
    }

    private static boolean matchesAny(String command, String... prefixes) {
        for (String prefix : prefixes) {
            if (command.equals(prefix) || command.startsWith(prefix + " ")
                    || (prefix.endsWith(" ") && command.startsWith(prefix))) {
                return true;
            }
        }
        return false;
    }
}
