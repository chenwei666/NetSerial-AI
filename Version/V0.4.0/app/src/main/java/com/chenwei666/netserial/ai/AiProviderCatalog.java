package com.chenwei666.netserial.ai;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class AiProviderCatalog {
    private final Map<String, AiProviderDefinition> definitions;

    private AiProviderCatalog(List<AiProviderDefinition> definitions) {
        Map<String, AiProviderDefinition> byId = new LinkedHashMap<>();
        for (AiProviderDefinition definition : definitions) {
            if (byId.put(definition.getId(), definition) != null) {
                throw new IllegalArgumentException("duplicate AI provider id: " + definition.getId());
            }
        }
        this.definitions = Collections.unmodifiableMap(byId);
    }

    public static AiProviderCatalog createDefault() {
        List<AiProviderDefinition> definitions = new ArrayList<>();
        definitions.add(new AiProviderDefinition("openai", "OpenAI", false, false));
        definitions.add(new AiProviderDefinition("anthropic", "Claude / Anthropic", false, false));
        definitions.add(new AiProviderDefinition("gemini", "Google Gemini", false, false));
        definitions.add(new AiProviderDefinition("deepseek", "DeepSeek", false, false));
        definitions.add(new AiProviderDefinition("qwen", "通义千问", false, false));
        definitions.add(new AiProviderDefinition("kimi", "Kimi", false, false));
        definitions.add(new AiProviderDefinition("openai-compatible", "OpenAI Compatible", true, false));
        definitions.add(new AiProviderDefinition("ollama", "Ollama / Local", true, true));
        return new AiProviderCatalog(definitions);
    }

    public List<String> getProviderIds() {
        return Collections.unmodifiableList(new ArrayList<>(definitions.keySet()));
    }

    public AiProviderDefinition require(String id) {
        AiProviderDefinition definition = definitions.get(id);
        if (definition == null) {
            throw new IllegalArgumentException("unknown AI provider: " + id);
        }
        return definition;
    }
}
