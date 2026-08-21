package com.chenwei666.netserial.ai;

import java.util.LinkedHashMap;
import java.util.Map;

public final class AiProviderPresetCatalog {
    private final Map<String, AiProviderPreset> presets;

    private AiProviderPresetCatalog(Map<String, AiProviderPreset> presets) {
        this.presets = presets;
    }

    public static AiProviderPresetCatalog createDefault() {
        Map<String, AiProviderPreset> presets = new LinkedHashMap<>();
        add(presets, "openai", "https://api.openai.com/v1", "gpt-5-mini", true);
        add(presets, "anthropic", "https://api.anthropic.com/v1", "claude-sonnet-4-5", false);
        add(
                presets,
                "gemini",
                "https://generativelanguage.googleapis.com/v1beta/openai",
                "gemini-3.7-flash",
                true
        );
        add(presets, "deepseek", "https://api.deepseek.com", "deepseek-v4-flash", true);
        add(
                presets,
                "qwen",
                "https://dashscope.aliyuncs.com/compatible-mode/v1",
                "qwen-plus",
                true
        );
        add(presets, "kimi", "https://api.moonshot.cn/v1", "kimi-k2.6", true);
        add(presets, "openai-compatible", "", "", true);
        add(presets, "ollama", "https://localhost/v1", "llama3.2", false);
        return new AiProviderPresetCatalog(presets);
    }

    public AiProviderPreset require(String providerId) {
        AiProviderPreset preset = presets.get(providerId);
        if (preset == null) {
            throw new IllegalArgumentException("unknown AI provider preset: " + providerId);
        }
        return preset;
    }

    private static void add(
            Map<String, AiProviderPreset> presets,
            String providerId,
            String endpoint,
            String model,
            boolean openAiCompatible
    ) {
        presets.put(
                providerId,
                new AiProviderPreset(providerId, endpoint, model, openAiCompatible)
        );
    }
}
