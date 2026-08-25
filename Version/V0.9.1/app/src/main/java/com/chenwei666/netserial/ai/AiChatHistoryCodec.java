package com.chenwei666.netserial.ai;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.ArrayList;
import java.util.List;

public final class AiChatHistoryCodec {
    public static final int MAX_SERIALIZED_CHARACTERS = 400_000;
    private static final int FORMAT_VERSION = 1;
    private final Gson gson = new GsonBuilder().disableHtmlEscaping().create();

    public String encode(AiChatHistoryState state) {
        JsonObject root = new JsonObject();
        root.addProperty("version", FORMAT_VERSION);
        if (state.getActiveConversationId() != null) {
            root.addProperty("active", state.getActiveConversationId());
        }
        JsonArray conversations = new JsonArray();
        for (AiChatConversation conversation : state.getConversations()) {
            JsonObject item = new JsonObject();
            item.addProperty("id", conversation.getId());
            item.addProperty("title", conversation.getTitle());
            item.addProperty("device", conversation.getDeviceName());
            item.addProperty("created", conversation.getCreatedAtMillis());
            item.addProperty("updated", conversation.getUpdatedAtMillis());
            JsonArray messages = new JsonArray();
            for (AiChatMessage message : conversation.getMessages()) {
                JsonObject value = new JsonObject();
                value.addProperty("role", message.getRole().name());
                value.addProperty("content", message.getContent());
                value.addProperty("created", message.getCreatedAtMillis());
                messages.add(value);
            }
            item.add("messages", messages);
            conversations.add(item);
        }
        root.add("conversations", conversations);
        String encoded = gson.toJson(root);
        if (encoded.length() > MAX_SERIALIZED_CHARACTERS) {
            throw new IllegalArgumentException("chat history is too large");
        }
        return encoded;
    }

    public AiChatHistoryState decode(String encoded) {
        if (encoded == null || encoded.trim().isEmpty()) return AiChatHistoryState.empty();
        if (encoded.length() > MAX_SERIALIZED_CHARACTERS) throw new IllegalArgumentException("chat history is too large");
        try {
            JsonObject root = JsonParser.parseString(encoded).getAsJsonObject();
            if (integer(root, "version") != FORMAT_VERSION) throw new IllegalArgumentException("unsupported history version");
            String active = optionalString(root, "active");
            JsonArray values = array(root, "conversations");
            List<AiChatConversation> conversations = new ArrayList<>();
            for (JsonElement value : values) {
                JsonObject item = value.getAsJsonObject();
                List<AiChatMessage> messages = new ArrayList<>();
                for (JsonElement rawMessage : array(item, "messages")) {
                    JsonObject message = rawMessage.getAsJsonObject();
                    messages.add(new AiChatMessage(
                            AiChatRole.valueOf(string(message, "role")),
                            string(message, "content"), number(message, "created")));
                }
                conversations.add(new AiChatConversation(string(item, "id"), string(item, "title"),
                        string(item, "device"), number(item, "created"), number(item, "updated"), messages));
            }
            return new AiChatHistoryState(conversations, active);
        } catch (RuntimeException exception) {
            throw new IllegalArgumentException("invalid chat history", exception);
        }
    }

    private static JsonArray array(JsonObject object, String key) {
        JsonElement value = object.get(key);
        if (value == null || !value.isJsonArray()) throw new IllegalArgumentException("missing " + key);
        return value.getAsJsonArray();
    }

    private static String string(JsonObject object, String key) {
        JsonElement value = object.get(key);
        if (value == null || !value.isJsonPrimitive() || !value.getAsJsonPrimitive().isString()) {
            throw new IllegalArgumentException("missing " + key);
        }
        return value.getAsString();
    }

    private static String optionalString(JsonObject object, String key) {
        JsonElement value = object.get(key);
        return value == null || value.isJsonNull() ? null : value.getAsString();
    }

    private static int integer(JsonObject object, String key) {
        return object.get(key).getAsInt();
    }

    private static long number(JsonObject object, String key) {
        return object.get(key).getAsLong();
    }
}
