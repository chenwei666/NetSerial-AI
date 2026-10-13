package com.chenwei666.netserial.change;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Enforces the plan-before-change and evidence-before-close lifecycle. */
public final class ChangeReadinessValidator {
    public ChangeReadinessAssessment assessForStart(ChangeTask task) {
        Objects.requireNonNull(task, "task");
        List<String> missing = new ArrayList<>();
        requireText(missing, task.getPrecheckPlan(), "precheck plan");
        requireText(missing, task.getCommandPlan(), "command plan");
        requireText(missing, task.getVerificationPlan(), "verification plan");
        requireText(missing, task.getRollbackPlan(), "rollback plan");
        return new ChangeReadinessAssessment(missing);
    }

    public ChangeReadinessAssessment assessForCompletion(ChangeTask task) {
        Objects.requireNonNull(task, "task");
        List<String> missing = new ArrayList<>(assessForStart(task).getMissingRequirements());
        if (!hasEvent(task, ChangeEventType.COMMAND_SENT)) missing.add("command execution evidence");
        if (!hasEvent(task, ChangeEventType.OUTPUT_CAPTURED)
                && !hasEvent(task, ChangeEventType.VERIFICATION)) {
            missing.add("post-change verification evidence");
        }
        return new ChangeReadinessAssessment(missing);
    }

    private static boolean hasEvent(ChangeTask task, ChangeEventType type) {
        for (ChangeEvent event : task.getEvents()) if (event.getType() == type) return true;
        return false;
    }

    private static void requireText(List<String> missing, String value, String label) {
        if (value == null || value.trim().isEmpty()) missing.add(label);
    }
}
