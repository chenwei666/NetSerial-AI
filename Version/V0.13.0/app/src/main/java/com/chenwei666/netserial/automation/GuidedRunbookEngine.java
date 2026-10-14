package com.chenwei666.netserial.automation;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Converts an approved read-only playbook into a bounded Expect-style execution contract. */
public final class GuidedRunbookEngine {
    private static final String PROMPT_PATTERN = "(?m)[>\\]#]\\s*$";

    public GuidedRunbookPlan prepare(PlaybookPlan source) {
        Objects.requireNonNull(source, "source");
        List<GuidedRunbookStep> steps = new ArrayList<>();
        int sequence = 1;
        for (String command : source.getCommands()) {
            steps.add(new GuidedRunbookStep(sequence++, command, PROMPT_PATTERN,
                    20, 1, RunbookFailureAction.STOP));
        }
        return new GuidedRunbookPlan(source.getType(), steps, source.getStopConditions());
    }
}
