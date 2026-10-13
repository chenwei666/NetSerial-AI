package com.chenwei666.netserial.commands;

import com.chenwei666.netserial.device.CliMode;
import com.chenwei666.netserial.device.Vendor;
import com.chenwei666.netserial.safety.RiskLevel;

import java.util.Objects;

public final class CommonCommand {
    private final Vendor vendor;
    private final CliMode mode;
    private final CommandCategory category;
    private final String command;
    private final String description;
    private final RiskLevel riskLevel;

    public CommonCommand(Vendor vendor, CliMode mode, CommandCategory category,
                         String command, String description, RiskLevel riskLevel) {
        this.vendor = Objects.requireNonNull(vendor, "vendor");
        this.mode = Objects.requireNonNull(mode, "mode");
        this.category = Objects.requireNonNull(category, "category");
        this.command = requireText(command, "command");
        this.description = requireText(description, "description");
        this.riskLevel = Objects.requireNonNull(riskLevel, "riskLevel");
    }

    public Vendor getVendor() { return vendor; }
    public CliMode getMode() { return mode; }
    public CommandCategory getCategory() { return category; }
    public String getCommand() { return command; }
    public String getDescription() { return description; }
    public RiskLevel getRiskLevel() { return riskLevel; }

    private static String requireText(String value, String field) {
        String normalized = Objects.requireNonNull(value, field).trim();
        if (normalized.isEmpty() || normalized.length() > 256) {
            throw new IllegalArgumentException(field + " must contain 1 to 256 characters");
        }
        return normalized;
    }
}
