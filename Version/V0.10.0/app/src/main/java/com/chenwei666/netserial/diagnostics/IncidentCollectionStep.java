package com.chenwei666.netserial.diagnostics;

import java.util.Objects;

public final class IncidentCollectionStep {
    private final String category;
    private final String command;
    private final String purpose;

    public IncidentCollectionStep(String category, String command, String purpose) {
        this.category = text(category, "category");
        this.command = text(command, "command");
        this.purpose = text(purpose, "purpose");
    }

    public String getCategory() { return category; }
    public String getCommand() { return command; }
    public String getPurpose() { return purpose; }

    private static String text(String value, String name) {
        String normalized = Objects.requireNonNull(value, name).trim();
        if (normalized.isEmpty()) throw new IllegalArgumentException(name + " required");
        return normalized;
    }
}
