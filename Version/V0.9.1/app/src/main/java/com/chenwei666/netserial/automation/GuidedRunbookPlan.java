package com.chenwei666.netserial.automation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class GuidedRunbookPlan {
    private final PlaybookType type;
    private final List<GuidedRunbookStep> steps;
    private final List<String> stopConditions;

    GuidedRunbookPlan(PlaybookType type, List<GuidedRunbookStep> steps, List<String> stopConditions) {
        this.type = type;
        this.steps = Collections.unmodifiableList(new ArrayList<>(steps));
        this.stopConditions = Collections.unmodifiableList(new ArrayList<>(stopConditions));
    }

    public PlaybookType getType() { return type; }
    public List<GuidedRunbookStep> getSteps() { return steps; }
    public List<String> getStopConditions() { return stopConditions; }
}
