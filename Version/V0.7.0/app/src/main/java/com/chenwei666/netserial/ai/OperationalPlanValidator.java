package com.chenwei666.netserial.ai;

import java.util.ArrayList;
import java.util.List;

public final class OperationalPlanValidator {
    public PlanSafetyAssessment assess(CommandPlan plan) {
        boolean hasPrecheck = false;
        boolean hasChange = false;
        boolean hasVerification = false;
        boolean hasRollback = false;
        List<PlanValidationIssue> warnings = new ArrayList<>();
        for (EvaluatedCommandStep step : plan.getSteps()) {
            if (step.getPhase() == PlanPhase.PRECHECK) hasPrecheck = true;
            if (step.getPhase() == PlanPhase.CHANGE) hasChange = true;
            if (step.getPhase() == PlanPhase.VERIFY) hasVerification = true;
            if (step.getPhase() == PlanPhase.ROLLBACK) hasRollback = true;
        }
        if (!hasPrecheck) warnings.add(PlanValidationIssue.MISSING_PRECHECK);
        if (!hasChange) warnings.add(PlanValidationIssue.MISSING_CHANGE);
        if (!hasVerification) warnings.add(PlanValidationIssue.MISSING_VERIFICATION);
        if (!hasRollback) warnings.add(PlanValidationIssue.MISSING_ROLLBACK);
        if (plan.getSteps().size() > 15) warnings.add(PlanValidationIssue.TOO_MANY_STEPS);
        return new PlanSafetyAssessment(warnings);
    }
}
