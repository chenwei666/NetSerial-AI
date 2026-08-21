package com.chenwei666.netserial.ai;

import com.chenwei666.netserial.safety.RiskLevel;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class StructuredPlanParserTest {
    @Test
    public void parsesNativeProviderStructuredText() {
        AiDraftPlan plan = new StructuredPlanParser().parse(
                "```json\n{\"steps\":[{\"command\":\"show version\",\"risk\":\"R1_READ_ONLY\"}]}\n```");
        assertEquals(1, plan.getSteps().size());
        assertEquals("show version", plan.getSteps().get(0).getCommand());
        assertEquals(RiskLevel.R1_READ_ONLY, plan.getSteps().get(0).getProposedRisk());
    }

    @Test(expected = AiProviderException.class)
    public void rejectsMultilineCommands() {
        new StructuredPlanParser().parse(
                "{\"steps\":[{\"command\":\"show version\\nreboot\",\"risk\":\"R0_INFORMATIONAL\"}]}");
    }
}
