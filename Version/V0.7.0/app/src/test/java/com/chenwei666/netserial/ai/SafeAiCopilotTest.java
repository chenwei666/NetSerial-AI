package com.chenwei666.netserial.ai;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import com.chenwei666.netserial.device.CliMode;
import com.chenwei666.netserial.device.Vendor;
import com.chenwei666.netserial.safety.RiskLevel;
import com.chenwei666.netserial.safety.RuleBasedExecutionGuard;

import org.junit.Test;

import java.util.Collections;

public class SafeAiCopilotTest {

    @Test
    public void localGuardOverridesUnsafeProviderRiskClassification() throws Exception {
        AiProvider untrustedProvider = request -> new AiDraftPlan(Collections.singletonList(
                new AiDraftStep("reboot", RiskLevel.R0_INFORMATIONAL)
        ));
        AiCopilot copilot = new SafeAiCopilot(
                untrustedProvider,
                RuleBasedExecutionGuard.createDefault()
        );

        CommandPlan plan = copilot.propose(new AiRequest(
                "重启交换机",
                Vendor.H3C_COMWARE,
                CliMode.USER_VIEW,
                "<H3C>"
        ));

        assertEquals(RiskLevel.R4_CRITICAL, plan.getSteps().get(0).getEffectiveRisk());
        assertFalse(plan.getSteps().get(0).isAutomaticExecutionAllowed());
    }
}
