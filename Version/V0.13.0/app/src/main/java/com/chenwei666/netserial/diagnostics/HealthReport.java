package com.chenwei666.netserial.diagnostics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class HealthReport {
    private final List<HealthFinding> findings;

    public HealthReport(List<HealthFinding> findings) {
        this.findings = Collections.unmodifiableList(new ArrayList<>(findings));
    }

    public List<HealthFinding> getFindings() { return findings; }
    public boolean hasCriticalFinding() {
        for (HealthFinding finding : findings) {
            if (finding.getSeverity() == DiagnosticSeverity.CRITICAL) return true;
        }
        return false;
    }
}
