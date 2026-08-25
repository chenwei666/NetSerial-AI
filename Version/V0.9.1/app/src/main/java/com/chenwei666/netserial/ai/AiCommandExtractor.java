package com.chenwei666.netserial.ai;

import com.chenwei666.netserial.device.CliMode;
import com.chenwei666.netserial.device.Vendor;
import com.chenwei666.netserial.safety.CommandEvaluationRequest;
import com.chenwei666.netserial.safety.GuardDecision;
import com.chenwei666.netserial.safety.RiskLevel;
import com.chenwei666.netserial.safety.RuleBasedExecutionGuard;
import com.chenwei666.netserial.terminal.SensitiveTextRedactor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/** Extracts only explicit fenced command drafts and reclassifies every line locally. */
public final class AiCommandExtractor {
    private static final int MAX_COMMANDS = 20;
    private static final int MAX_COMMAND_CHARACTERS = 512;

    public List<AiSuggestedCommand> extract(String response, Vendor vendor, CliMode cliMode) {
        if (response == null || response.trim().isEmpty()) return Collections.emptyList();
        Set<String> commands = fencedLines(response);
        List<AiSuggestedCommand> result = new ArrayList<>();
        RuleBasedExecutionGuard guard = RuleBasedExecutionGuard.createDefault();
        SensitiveTextRedactor redactor = new SensitiveTextRedactor();
        for (String command : commands) {
            if (result.size() >= MAX_COMMANDS) break;
            if (!isCandidate(command)) continue;
            if (!redactor.redact(command).equals(command)) continue;
            GuardDecision decision = guard.evaluate(new CommandEvaluationRequest(vendor, cliMode,
                    command, RiskLevel.R0_INFORMATIONAL));
            result.add(new AiSuggestedCommand(command, decision.getEffectiveRisk()));
        }
        return Collections.unmodifiableList(result);
    }

    private static Set<String> fencedLines(String response) {
        Set<String> result = new LinkedHashSet<>();
        boolean fenced = false;
        for (String raw : response.replace('\r', '\n').split("\n", -1)) {
            String line = raw.trim();
            if (line.startsWith("```")) {
                fenced = !fenced;
                continue;
            }
            if (fenced && !line.isEmpty()) result.add(stripPrompt(line));
        }
        return result;
    }

    private static String stripPrompt(String value) {
        if (value.startsWith("$ ")) return value.substring(2).trim();
        return value;
    }

    private static boolean isCandidate(String value) {
        if (value.isEmpty() || value.length() > MAX_COMMAND_CHARACTERS) return false;
        if (value.startsWith("#") || value.startsWith("//") || value.startsWith("--")) return false;
        if (value.contains("{") || value.contains("}") || value.contains(";") || value.indexOf('\0') >= 0) return false;
        String lower = value.toLowerCase(java.util.Locale.ROOT);
        if (lower.startsWith("json") || lower.startsWith("yaml") || lower.startsWith("text")
                || lower.startsWith("diff") || lower.contains("[redacted]")) return false;
        return startsWithCommand(lower, "display", "show", "ping", "tracert", "traceroute",
                "terminal", "screen-length", "system-view", "configure", "conf t", "interface",
                "vlan", "description", "port", "switchport", "ip", "ipv6", "undo", "no",
                "shutdown", "speed", "duplex", "stp", "spanning-tree", "lacp", "link-aggregation",
                "eth-trunk", "aaa", "local-user", "username", "ssh", "stelnet", "snmp",
                "ntp", "clock", "logging", "info-center", "save", "write", "copy", "reload",
                "reboot", "reset", "delete", "erase", "format", "upgrade", "bootrom");
    }

    private static boolean startsWithCommand(String value, String... commands) {
        for (String command : commands) {
            if (value.equals(command) || value.startsWith(command + " ")) return true;
        }
        return false;
    }
}
