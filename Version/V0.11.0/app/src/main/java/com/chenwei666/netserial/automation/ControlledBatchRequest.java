package com.chenwei666.netserial.automation;

import com.chenwei666.netserial.device.CliMode;
import com.chenwei666.netserial.device.Vendor;
import com.chenwei666.netserial.safety.RiskLevel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;

public final class ControlledBatchRequest {
    private static final int MAX_TARGETS = 25;
    private final String changeId;
    private final List<String> targets;
    private final List<String> precheck;
    private final List<String> change;
    private final List<String> verification;
    private final List<String> rollback;
    private final Vendor vendor;
    private final CliMode cliMode;
    private final RiskLevel proposedChangeRisk;

    public ControlledBatchRequest(String changeId, List<String> targets, List<String> precheck,
                                  List<String> change, List<String> verification, List<String> rollback) {
        this(changeId, targets, precheck, change, verification, rollback,
                Vendor.GENERIC, CliMode.UNKNOWN, RiskLevel.R2_CONFIGURATION);
    }

    public ControlledBatchRequest(String changeId, List<String> targets, List<String> precheck,
                                  List<String> change, List<String> verification, List<String> rollback,
                                  Vendor vendor, CliMode cliMode, RiskLevel proposedChangeRisk) {
        this.changeId = token(changeId, 64, "change id");
        this.targets = targets(targets);
        this.precheck = commands(precheck, "precheck");
        this.change = commands(change, "change");
        this.verification = commands(verification, "verification");
        this.rollback = commands(rollback, "rollback");
        this.vendor = java.util.Objects.requireNonNull(vendor, "vendor");
        this.cliMode = java.util.Objects.requireNonNull(cliMode, "cliMode");
        this.proposedChangeRisk = java.util.Objects.requireNonNull(proposedChangeRisk, "proposedChangeRisk");
    }

    public String getChangeId() { return changeId; }
    public List<String> getTargets() { return targets; }
    public List<String> getPrecheck() { return precheck; }
    public List<String> getChange() { return change; }
    public List<String> getVerification() { return verification; }
    public List<String> getRollback() { return rollback; }
    public Vendor getVendor() { return vendor; }
    public CliMode getCliMode() { return cliMode; }
    public RiskLevel getProposedChangeRisk() { return proposedChangeRisk; }

    private static List<String> targets(List<String> input) {
        if (input == null) throw new IllegalArgumentException("Targets required");
        LinkedHashSet<String> result = new LinkedHashSet<>();
        for (String value : input) result.add(token(value, 253, "target"));
        if (result.isEmpty() || result.size() > MAX_TARGETS) throw new IllegalArgumentException("Invalid target count");
        return Collections.unmodifiableList(new ArrayList<>(result));
    }

    private static List<String> commands(List<String> input, String stage) {
        if (input == null || input.isEmpty() || input.size() > 100) throw new IllegalArgumentException(stage + " commands required");
        List<String> result = new ArrayList<>();
        for (String value : input) {
            String command = value == null ? "" : value.trim();
            if (command.isEmpty() || command.length() > 512 || command.contains("\n") || command.contains("\r")) {
                throw new IllegalArgumentException("Invalid " + stage + " command");
            }
            String lower = command.toLowerCase(java.util.Locale.ROOT);
            if (lower.matches(".*\\b(password|secret|community)\\s+\\S+.*")) {
                throw new IllegalArgumentException("Inline credentials are forbidden");
            }
            result.add(command);
        }
        return Collections.unmodifiableList(result);
    }

    private static String token(String value, int max, String field) {
        String result = value == null ? "" : value.trim();
        if (result.isEmpty() || result.length() > max || !result.matches("[A-Za-z0-9._:/-]+")) {
            throw new IllegalArgumentException("Invalid " + field);
        }
        return result;
    }
}
