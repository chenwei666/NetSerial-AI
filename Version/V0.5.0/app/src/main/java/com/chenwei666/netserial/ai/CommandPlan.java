package com.chenwei666.netserial.ai;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class CommandPlan {
    private final List<EvaluatedCommandStep> steps;

    public CommandPlan(List<EvaluatedCommandStep> steps) {
        this.steps = Collections.unmodifiableList(new ArrayList<>(steps));
    }

    public List<EvaluatedCommandStep> getSteps() {
        return steps;
    }
}
