package com.chenwei666.netserial.config;

import com.chenwei666.netserial.device.Vendor;
import com.chenwei666.netserial.safety.RiskLevel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class ConfigBackupPlan {
    private final Vendor vendor;
    private final List<String> commands;
    ConfigBackupPlan(Vendor vendor, List<String> commands) {
        this.vendor = Objects.requireNonNull(vendor, "vendor");
        this.commands = Collections.unmodifiableList(new ArrayList<>(commands));
    }
    public Vendor getVendor() { return vendor; }
    public List<String> getCommands() { return commands; }
    public RiskLevel getRisk() { return RiskLevel.R0_INFORMATIONAL; }
}
