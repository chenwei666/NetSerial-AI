package com.chenwei666.netserial.diagnostics;

import java.util.Objects;

public final class TroubleshootingStep {
    private final String phase;
    private final String command;
    private final String purpose;

    public TroubleshootingStep(String phase, String command, String purpose) {
        this.phase = require(phase, 32);
        this.command = require(command, 256);
        this.purpose = require(purpose, 256);
    }

    public String getPhase() { return phase; }
    public String getCommand() { return command; }
    public String getPurpose() { return purpose; }

    private static String require(String value, int max) {
        String result = Objects.requireNonNull(value, "value").trim();
        if (result.isEmpty() || result.length() > max) throw new IllegalArgumentException("Invalid step");
        return result;
    }
}
