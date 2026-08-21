package com.chenwei666.netserial.ai;

import com.chenwei666.netserial.safety.RiskLevel;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

import java.util.ArrayList;
import java.util.List;

final class StructuredPlanParser {
    private static final int MAX_STEPS = 20;
    private static final int MAX_COMMAND_CHARACTERS = 1_024;

    AiDraftPlan parse(String content) {
        try {
            JsonObject planObject = JsonParser.parseString(stripFence(content)).getAsJsonObject();
            JsonElement value = planObject.get("steps");
            if (value == null || !value.isJsonArray()) throw invalid();
            JsonArray steps = value.getAsJsonArray();
            if (steps.size() == 0 || steps.size() > MAX_STEPS) throw invalid();
            List<AiDraftStep> decoded = new ArrayList<>();
            for (JsonElement element : steps) {
                JsonObject step = element.getAsJsonObject();
                String command = string(step, "command").trim();
                if (command.isEmpty() || command.length() > MAX_COMMAND_CHARACTERS
                        || containsControlCharacter(command)) throw invalid();
                decoded.add(new AiDraftStep(command, RiskLevel.valueOf(string(step, "risk"))));
            }
            return new AiDraftPlan(decoded);
        } catch (AiProviderException exception) {
            throw exception;
        } catch (JsonParseException | IllegalStateException | IllegalArgumentException exception) {
            throw invalid();
        }
    }

    private static String string(JsonObject object, String name) {
        JsonElement value = object.get(name);
        if (value == null || !value.isJsonPrimitive() || !value.getAsJsonPrimitive().isString()) {
            throw invalid();
        }
        return value.getAsString();
    }

    private static String stripFence(String content) {
        String value = content.trim();
        if (!value.startsWith("```") || !value.endsWith("```")) return value;
        int end = value.indexOf('\n');
        if (end < 0) throw invalid();
        return value.substring(end + 1, value.length() - 3).trim();
    }

    private static boolean containsControlCharacter(String value) {
        for (int i = 0; i < value.length(); i++) {
            if (Character.isISOControl(value.charAt(i))) return true;
        }
        return false;
    }

    private static AiProviderException invalid() {
        return new AiProviderException(AiProviderError.INVALID_RESPONSE,
                "AI provider returned an invalid response", 0, false);
    }
}
