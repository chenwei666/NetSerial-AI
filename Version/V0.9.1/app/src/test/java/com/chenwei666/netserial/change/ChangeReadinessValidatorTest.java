package com.chenwei666.netserial.change;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.ArrayList;

public class ChangeReadinessValidatorTest {
    @Test public void requiresCompletePlanBeforeStart() {
        ChangeTask task = task("", "commands", "", "rollback");
        ChangeReadinessAssessment result = new ChangeReadinessValidator().assessForStart(task);
        assertFalse(result.isReady());
        assertTrue(result.getMissingRequirements().contains("precheck plan"));
        assertTrue(result.getMissingRequirements().contains("verification plan"));
    }

    @Test public void requiresExecutionAndVerificationEvidenceBeforeCompletion() {
        ChangeTask active = task("precheck", "commands", "verify", "rollback").start(1_000);
        ChangeReadinessAssessment before = new ChangeReadinessValidator().assessForCompletion(active);
        assertFalse(before.isReady());
        active = active.append(new ChangeEvent(1_100, ChangeEventType.COMMAND_SENT, "switch", "show version"));
        active = active.append(new ChangeEvent(1_200, ChangeEventType.OUTPUT_CAPTURED, "switch", "captured"));
        assertTrue(new ChangeReadinessValidator().assessForCompletion(active).isReady());
    }

    private static ChangeTask task(String precheck, String commands, String verify, String rollback) {
        return new ChangeTask("id", "CHG-1", "site", "switch", "operator", "goal",
                precheck, commands, verify, rollback, 1_000, 100_000,
                ChangeTaskStatus.DRAFT, new ArrayList<>());
    }
}
