package com.chenwei666.netserial.diagnostics;

import com.chenwei666.netserial.safety.RiskLevel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class PortTroubleshootingPlan {
    private final List<TroubleshootingStep> steps;
    PortTroubleshootingPlan(List<TroubleshootingStep> steps) {
        this.steps = Collections.unmodifiableList(new ArrayList<>(steps));
    }
    public List<TroubleshootingStep> getSteps() { return steps; }
    public RiskLevel getRisk() { return RiskLevel.R0_INFORMATIONAL; }
}
