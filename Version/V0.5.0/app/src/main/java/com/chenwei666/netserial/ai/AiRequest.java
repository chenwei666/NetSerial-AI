package com.chenwei666.netserial.ai;

import com.chenwei666.netserial.device.CliMode;
import com.chenwei666.netserial.device.Vendor;

import java.util.Objects;

public final class AiRequest {
    private final String intent;
    private final Vendor vendor;
    private final CliMode cliMode;
    private final String recentTerminalOutput;

    public AiRequest(String intent, Vendor vendor, CliMode cliMode, String recentTerminalOutput) {
        this.intent = requireText(intent, "intent");
        this.vendor = Objects.requireNonNull(vendor, "vendor");
        this.cliMode = Objects.requireNonNull(cliMode, "cliMode");
        this.recentTerminalOutput = Objects.requireNonNull(recentTerminalOutput,
                "recentTerminalOutput");
    }

    public String getIntent() {
        return intent;
    }

    public Vendor getVendor() {
        return vendor;
    }

    public CliMode getCliMode() {
        return cliMode;
    }

    public String getRecentTerminalOutput() {
        return recentTerminalOutput;
    }

    private static String requireText(String value, String name) {
        String normalized = Objects.requireNonNull(value, name).trim();
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException(name + " must not be empty");
        }
        return normalized;
    }
}
