package com.chenwei666.netserial.agent;

import com.google.gson.Strictness;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

/** Deliberately rejects Markdown, prose, duplicate keys, coercion and multi-action responses. */
public final class AgentActionCodec {
    private AgentActionCodec() { }

    public static AgentAction parse(String json) {
        if (json == null || json.trim().isEmpty() || json.length() > 8192) invalid();
        Map<String, Object> fields = new HashMap<>();
        try (JsonReader reader = new JsonReader(new StringReader(json))) {
            reader.setStrictness(Strictness.STRICT);
            if (reader.peek() != JsonToken.BEGIN_OBJECT) invalid();
            reader.beginObject();
            while (reader.hasNext()) {
                String key = reader.nextName();
                if (fields.containsKey(key)) invalid();
                if ("https".equals(key) || "http".equals(key)) {
                    if (reader.peek() != JsonToken.BOOLEAN) invalid();
                    fields.put(key, reader.nextBoolean());
                } else {
                    if (!("type".equals(key) || "command".equals(key) || "message".equals(key)
                            || "username".equals(key)) || reader.peek() != JsonToken.STRING) invalid();
                    fields.put(key, reader.nextString());
                }
            }
            reader.endObject();
            if (reader.peek() != JsonToken.END_DOCUMENT) invalid();
        } catch (IOException | IllegalStateException error) {
            throw new IllegalArgumentException("Invalid agent action JSON", error);
        }
        AgentAction.Type type;
        try { type = AgentAction.Type.valueOf((String) fields.get("type")); }
        catch (RuntimeException error) { throw new IllegalArgumentException("Unknown agent action type"); }
        String message = (String) fields.get("message");
        if (message == null || message.trim().isEmpty() || message.length() > 4096) invalid();
        String command = (String) fields.get("command");
        if (type == AgentAction.Type.WEB_ACCESS) {
            String username = (String) fields.get("username");
            if (fields.size() != 5 || fields.containsKey("command") || username == null
                    || !username.matches("[A-Za-z0-9_.-]{1,32}")
                    || !fields.containsKey("https") || !fields.containsKey("http")) invalid();
            boolean https = Boolean.TRUE.equals(fields.get("https"));
            boolean http = Boolean.TRUE.equals(fields.get("http"));
            if (!https && !http) invalid();
            return new AgentAction(type, "", message, username, https, http);
        }
        if (type == AgentAction.Type.COMMAND) {
            if (fields.size() != 3 || command == null || command.trim().isEmpty() || command.length() > 512) invalid();
        } else if (fields.size() != 2 || fields.containsKey("command")) invalid();
        return new AgentAction(type, command == null ? "" : command, message);
    }

    private static void invalid() { throw new IllegalArgumentException("Invalid agent action schema"); }
}
