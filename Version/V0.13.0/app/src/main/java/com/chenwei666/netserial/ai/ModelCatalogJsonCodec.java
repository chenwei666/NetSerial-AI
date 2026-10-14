package com.chenwei666.netserial.ai;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public final class ModelCatalogJsonCodec {
    private static final int MAX_MODELS = 1_000;
    private static final int MAX_MODEL_ID_LENGTH = 256;

    public List<String> decode(byte[] body, ModelCatalogFormat format) {
        try {
            JsonObject root = JsonParser.parseString(
                    new String(body, StandardCharsets.UTF_8)).getAsJsonObject();
            JsonArray entries = format == ModelCatalogFormat.QWEN
                    ? requiredArray(requiredObject(root, "output"), "models")
                    : requiredArray(root,
                    format == ModelCatalogFormat.OLLAMA ? "models" : "data");
            Set<String> models = new LinkedHashSet<>();
            for (JsonElement entry : entries) {
                if (!entry.isJsonObject()) continue;
                JsonObject object = entry.getAsJsonObject();
                String field = format == ModelCatalogFormat.OLLAMA ? "name"
                        : format == ModelCatalogFormat.QWEN ? "model" : "id";
                JsonElement value = object.get(field);
                if (value == null || !value.isJsonPrimitive()) continue;
                String model = value.getAsString().trim();
                if (!model.isEmpty() && model.length() <= MAX_MODEL_ID_LENGTH) models.add(model);
                if (models.size() >= MAX_MODELS) break;
            }
            if (models.isEmpty()) throw invalidResponse();
            List<String> result = new ArrayList<>(models);
            Collections.sort(result, String.CASE_INSENSITIVE_ORDER);
            return Collections.unmodifiableList(result);
        } catch (AiProviderException exception) {
            throw exception;
        } catch (RuntimeException exception) {
            throw new AiProviderException(AiProviderError.INVALID_RESPONSE,
                    "Model catalog response is invalid", false, exception);
        }
    }

    private static JsonArray requiredArray(JsonObject root, String name) {
        JsonElement value = root.get(name);
        if (value == null || !value.isJsonArray()) throw invalidResponse();
        return value.getAsJsonArray();
    }

    private static JsonObject requiredObject(JsonObject root, String name) {
        JsonElement value = root.get(name);
        if (value == null || !value.isJsonObject()) throw invalidResponse();
        return value.getAsJsonObject();
    }

    private static AiProviderException invalidResponse() {
        return new AiProviderException(AiProviderError.INVALID_RESPONSE,
                "Model catalog response contains no models", 0, false);
    }
}
