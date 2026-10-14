package com.chenwei666.netserial.diagnostics;

import com.chenwei666.netserial.device.Vendor;
import com.chenwei666.netserial.safety.RiskLevel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class HealthCheckPlan {
    private final Vendor vendor;
    private final List<String> commands;

    public HealthCheckPlan(Vendor vendor, List<String> commands) {
        this.vendor = Objects.requireNonNull(vendor, "vendor");
        if (commands == null || commands.isEmpty()) throw new IllegalArgumentException("Commands required");
        this.commands = Collections.unmodifiableList(new ArrayList<>(commands));
    }

    public Vendor getVendor() { return vendor; }
    public List<String> getCommands() { return commands; }
    public RiskLevel getRisk() { return RiskLevel.R0_INFORMATIONAL; }

    public String commandBatch() {
        StringBuilder result = new StringBuilder();
        for (String command : commands) {
            if (result.length() > 0) result.append('\n');
            result.append(command);
        }
        return result.toString();
    }
}
