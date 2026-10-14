package com.chenwei666.netserial.ai;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class PlanSafetyAssessment {
    private final List<PlanValidationIssue> warnings;

    PlanSafetyAssessment(List<PlanValidationIssue> warnings) {
        this.warnings = Collections.unmodifiableList(new ArrayList<>(warnings));
    }

    public List<PlanValidationIssue> getWarnings() { return warnings; }
    public boolean isComplete() { return warnings.isEmpty(); }
}
