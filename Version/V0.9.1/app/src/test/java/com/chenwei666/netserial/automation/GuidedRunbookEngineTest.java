package com.chenwei666.netserial.automation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.chenwei666.netserial.device.Vendor;

import org.junit.Test;

import java.util.regex.Pattern;

public class GuidedRunbookEngineTest {
    @Test public void addsBoundedExpectContractToEveryStep() {
        PlaybookPlan source = new SafePlaybookEngine().plan(Vendor.H3C_COMWARE,
                PlaybookType.PRE_CHANGE_HEALTH, "");
        GuidedRunbookPlan plan = new GuidedRunbookEngine().prepare(source);
        assertEquals(source.getCommands().size(), plan.getSteps().size());
        for (GuidedRunbookStep step : plan.getSteps()) {
            Pattern.compile(step.getExpectedPromptPattern());
            assertEquals(20, step.getTimeoutSeconds());
            assertEquals(1, step.getMaxRetries());
            assertEquals(RunbookFailureAction.STOP, step.getFailureAction());
        }
        assertTrue(plan.getStopConditions().size() >= 3);
    }
}
