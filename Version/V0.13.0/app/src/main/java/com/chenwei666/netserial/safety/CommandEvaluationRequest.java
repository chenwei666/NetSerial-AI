package com.chenwei666.netserial.safety;

import com.chenwei666.netserial.device.CliMode;
import com.chenwei666.netserial.device.Vendor;

import java.util.Objects;

public final class CommandEvaluationRequest {
    private final Vendor vendor;
    private final CliMode cliMode;
    private final String command;
    private final RiskLevel proposedRisk;

    public CommandEvaluationRequest(Vendor vendor, CliMode cliMode, String command,
                                    RiskLevel proposedRisk) {
        this.vendor = Objects.requireNonNull(vendor, "vendor");
        this.cliMode = Objects.requireNonNull(cliMode, "cliMode");
        this.command = Objects.requireNonNull(command, "command").trim();
        this.proposedRisk = Objects.requireNonNull(proposedRisk, "proposedRisk");
        if (this.command.isEmpty()) {
            throw new IllegalArgumentException("command must not be empty");
        }
    }

    public Vendor getVendor() {
        return vendor;
    }

    public CliMode getCliMode() {
        return cliMode;
    }

    public String getCommand() {
        return command;
    }

    public RiskLevel getProposedRisk() {
        return proposedRisk;
    }
}
