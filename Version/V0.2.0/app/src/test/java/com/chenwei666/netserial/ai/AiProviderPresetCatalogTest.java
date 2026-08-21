package com.chenwei666.netserial.ai;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class AiProviderPresetCatalogTest {

    @Test
    public void compatibleProvidersHaveSafeOfficialDefaults() {
        AiProviderPresetCatalog catalog = AiProviderPresetCatalog.createDefault();

        assertEquals(
                "https://api.openai.com/v1",
                catalog.require("openai").getEndpoint()
        );
        assertEquals(
                "gemini-3.7-flash",
                catalog.require("gemini").getModel()
        );
        assertEquals(
                "deepseek-v4-flash",
                catalog.require("deepseek").getModel()
        );
        assertTrue(catalog.require("qwen").isOpenAiCompatible());
        assertTrue(catalog.require("kimi").isOpenAiCompatible());
    }

    @Test
    public void nativeProvidersAreNotSentThroughCompatibleAdapter() {
        AiProviderPresetCatalog catalog = AiProviderPresetCatalog.createDefault();

        assertFalse(catalog.require("anthropic").isOpenAiCompatible());
        assertFalse(catalog.require("ollama").isOpenAiCompatible());
    }
}
