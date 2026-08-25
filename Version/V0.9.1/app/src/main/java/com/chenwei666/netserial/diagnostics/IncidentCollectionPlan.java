package com.chenwei666.netserial.diagnostics;

import com.chenwei666.netserial.device.Vendor;
import com.chenwei666.netserial.safety.RiskLevel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class IncidentCollectionPlan {
    private final Vendor vendor;
    private final List<IncidentCollectionStep> steps;
    private final List<String> stopConditions;

    IncidentCollectionPlan(Vendor vendor, List<IncidentCollectionStep> steps,
                           List<String> stopConditions) {
        this.vendor = Objects.requireNonNull(vendor, "vendor");
        this.steps = Collections.unmodifiableList(new ArrayList<>(steps));
        this.stopConditions = Collections.unmodifiableList(new ArrayList<>(stopConditions));
        if (steps.isEmpty()) throw new IllegalArgumentException("incident steps required");
    }

    public Vendor getVendor() { return vendor; }
    public List<IncidentCollectionStep> getSteps() { return steps; }
    public List<String> getStopConditions() { return stopConditions; }
    public RiskLevel getRisk() { return RiskLevel.R1_READ_ONLY; }

    public String commandBatch() {
        StringBuilder result = new StringBuilder();
        for (IncidentCollectionStep step : steps) {
            if (result.length() > 0) result.append('\n');
            result.append(step.getCommand());
        }
        return result.toString();
    }
}
