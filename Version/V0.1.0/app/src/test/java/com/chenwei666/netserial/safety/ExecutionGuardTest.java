package com.chenwei666.netserial.safety;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.chenwei666.netserial.device.CliMode;
import com.chenwei666.netserial.device.Vendor;

import org.junit.Test;

public class ExecutionGuardTest {

    @Test
    public void rebootIsCriticalAndAiCannotLowerItsRisk() {
        ExecutionGuard guard = RuleBasedExecutionGuard.createDefault();
        CommandEvaluationRequest request = new CommandEvaluationRequest(
                Vendor.H3C_COMWARE,
                CliMode.USER_VIEW,
                "reboot",
                RiskLevel.R0_INFORMATIONAL
        );

        GuardDecision decision = guard.evaluate(request);

        assertEquals(RiskLevel.R4_CRITICAL, decision.getEffectiveRisk());
        assertFalse(decision.isAutomaticExecutionAllowed());
        assertTrue(decision.isTypedConfirmationRequired());
    }

    @Test
    public void h3cDisplayCommandIsReadOnlyWithoutTypedConfirmation() {
        ExecutionGuard guard = RuleBasedExecutionGuard.createDefault();

        GuardDecision decision = guard.evaluate(new CommandEvaluationRequest(
                Vendor.H3C_COMWARE,
                CliMode.USER_VIEW,
                "display current-configuration",
                RiskLevel.R0_INFORMATIONAL
        ));

        assertEquals(RiskLevel.R1_READ_ONLY, decision.getEffectiveRisk());
        assertFalse(decision.isAutomaticExecutionAllowed());
        assertFalse(decision.isTypedConfirmationRequired());
    }
}
