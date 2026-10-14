package com.chenwei666.netserial.compliance;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class ComplianceReport {
    private final List<ComplianceFinding> findings;

    ComplianceReport(List<ComplianceFinding> findings) {
        this.findings = Collections.unmodifiableList(new ArrayList<>(findings));
    }

    public List<ComplianceFinding> getFindings() { return findings; }
    public boolean hasHighRisk() {
        for (ComplianceFinding finding : findings) {
            if (finding.getSeverity() == ComplianceSeverity.HIGH) return true;
        }
        return false;
    }
}
