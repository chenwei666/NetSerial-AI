package com.chenwei666.netserial.config;

import java.util.Objects;

public final class ConfigDriftAssessment {
    private final ConfigDiff diff;
    private final ConfigDriftSeverity severity;
    private final int sensitiveChanges;
    private final int totalChanges;

    ConfigDriftAssessment(ConfigDiff diff, ConfigDriftSeverity severity,
                          int sensitiveChanges, int totalChanges) {
        this.diff = Objects.requireNonNull(diff, "diff");
        this.severity = Objects.requireNonNull(severity, "severity");
        this.sensitiveChanges = sensitiveChanges;
        this.totalChanges = totalChanges;
    }

    public ConfigDiff getDiff() { return diff; }
    public ConfigDriftSeverity getSeverity() { return severity; }
    public int getSensitiveChanges() { return sensitiveChanges; }
    public int getTotalChanges() { return totalChanges; }
}
