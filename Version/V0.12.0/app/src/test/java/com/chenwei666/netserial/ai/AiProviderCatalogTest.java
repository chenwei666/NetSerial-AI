package com.chenwei666.netserial.ai;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public class AiProviderCatalogTest {

    @Test
    public void defaultCatalogCoversMajorAndCompatibleProviders() {
        AiProviderCatalog catalog = AiProviderCatalog.createDefault();
        List<String> expectedIds = Arrays.asList(
                "openai",
                "anthropic",
                "gemini",
                "deepseek",
                "zhipu",
                "qwen",
                "doubao",
                "hunyuan",
                "baidu",
                "kimi",
                "minimax",
                "siliconflow",
                "groq",
                "mistral",
                "xai",
                "openrouter",
                "openai-compatible",
                "ollama"
        );

        assertEquals(expectedIds, catalog.getProviderIds());
        assertTrue(catalog.require("openai-compatible").isCustomEndpointAllowed());
        assertTrue(catalog.require("ollama").isLocalProvider());
        assertEquals("智谱 GLM / Zhipu", catalog.require("zhipu").getDisplayName());
    }
}
