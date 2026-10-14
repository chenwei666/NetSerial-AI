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
                "deepseek-chat",
                catalog.require("deepseek").getModel()
        );
        assertEquals("https://open.bigmodel.cn/api/paas/v4",
                catalog.require("zhipu").getEndpoint());
        assertTrue(catalog.require("qwen").isOpenAiCompatible());
        assertTrue(catalog.require("kimi").isOpenAiCompatible());
        assertTrue(catalog.require("doubao").isOpenAiCompatible());
        assertTrue(catalog.require("hunyuan").isOpenAiCompatible());
        assertEquals("https://tokenhub.tencentmaas.com/v1",
                catalog.require("hunyuan").getEndpoint());
        assertTrue(catalog.require("baidu").isOpenAiCompatible());
        assertTrue(catalog.require("minimax").isOpenAiCompatible());
        assertTrue(catalog.require("siliconflow").isOpenAiCompatible());
        assertTrue(catalog.require("groq").isOpenAiCompatible());
        assertTrue(catalog.require("mistral").isOpenAiCompatible());
        assertTrue(catalog.require("xai").isOpenAiCompatible());
        assertTrue(catalog.require("openrouter").isOpenAiCompatible());
    }

    @Test
    public void nativeProvidersAreNotSentThroughCompatibleAdapter() {
        AiProviderPresetCatalog catalog = AiProviderPresetCatalog.createDefault();

        assertFalse(catalog.require("anthropic").isOpenAiCompatible());
        assertFalse(catalog.require("ollama").isOpenAiCompatible());
    }
}
