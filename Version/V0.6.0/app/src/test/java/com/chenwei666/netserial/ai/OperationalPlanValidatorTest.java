package com.chenwei666.netserial.ai;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.chenwei666.netserial.safety.RiskLevel;

import org.junit.Test;

import java.util.Arrays;

public class OperationalPlanValidatorTest {
    @Test public void completeHighRiskPlanHasNoWarnings() {
        CommandPlan plan = new CommandPlan(Arrays.asList(
                new EvaluatedCommandStep("display current", RiskLevel.R1_READ_ONLY, false, PlanPhase.PRECHECK),
                new EvaluatedCommandStep("shutdown", RiskLevel.R3_HIGH, false, PlanPhase.CHANGE),
                new EvaluatedCommandStep("display interface", RiskLevel.R1_READ_ONLY, false, PlanPhase.VERIFY),
                new EvaluatedCommandStep("undo shutdown", RiskLevel.R2_CONFIGURATION, false, PlanPhase.ROLLBACK)));
        assertTrue(new OperationalPlanValidator().assess(plan).isComplete());
    }

    @Test public void warnsWhenVerificationAndRollbackAreMissing() {
        CommandPlan plan = new CommandPlan(Arrays.asList(
                new EvaluatedCommandStep("shutdown", RiskLevel.R3_HIGH, false, PlanPhase.CHANGE)));
        assertFalse(new OperationalPlanValidator().assess(plan).isComplete());
        assertTrue(new OperationalPlanValidator().assess(plan).getWarnings().size() >= 2);
    }
}
