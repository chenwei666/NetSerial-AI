package com.chenwei666.netserial.ai;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import com.chenwei666.netserial.safety.RiskLevel;

import org.junit.Test;

import java.nio.charset.StandardCharsets;

public class OpenAiCompatibleJsonCodecTest {

    @Test
    public void acceptsJsonInsideMarkdownFence() {
        OpenAiCompatibleJsonCodec codec = new OpenAiCompatibleJsonCodec();
        String response = "{\"choices\":[{\"message\":{\"content\":"
                + "\"```json\\n{\\\"steps\\\":[{\\\"command\\\":\\\"display interface brief\\\","
                + "\\\"risk\\\":\\\"R1_READ_ONLY\\\"}]}\\n```\"}}]}";

        AiDraftPlan plan = codec.decodeResponse(response.getBytes(StandardCharsets.UTF_8));

        assertEquals("display interface brief", plan.getSteps().get(0).getCommand());
        assertEquals(RiskLevel.R1_READ_ONLY, plan.getSteps().get(0).getProposedRisk());
    }

    @Test
    public void rejectsCommandContainingHiddenNewline() {
        OpenAiCompatibleJsonCodec codec = new OpenAiCompatibleJsonCodec();
        String response = "{\"choices\":[{\"message\":{\"content\":\""
                + "{\\\"steps\\\":[{\\\"command\\\":\\\"display version\\nreboot\\\","
                + "\\\"risk\\\":\\\"R1_READ_ONLY\\\"}]}\"}}]}";

        try {
            codec.decodeResponse(response.getBytes(StandardCharsets.UTF_8));
            fail("Expected AiProviderException");
        } catch (AiProviderException expected) {
            assertEquals(AiProviderError.INVALID_RESPONSE, expected.getError());
        }
    }
}
