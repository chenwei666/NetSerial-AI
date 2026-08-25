package com.chenwei666.netserial.ai;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class AiDraftPlan {
    private final List<AiDraftStep> steps;

    public AiDraftPlan(List<AiDraftStep> steps) {
        Objects.requireNonNull(steps, "steps");
        if (steps.isEmpty()) {
            throw new IllegalArgumentException("AI draft plan must contain at least one step");
        }
        this.steps = Collections.unmodifiableList(new ArrayList<>(steps));
    }

    public List<AiDraftStep> getSteps() {
        return steps;
    }
}
