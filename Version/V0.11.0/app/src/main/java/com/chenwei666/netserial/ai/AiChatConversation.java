package com.chenwei666.netserial.ai;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class AiChatConversation {
    public static final int MAX_MESSAGES = 50;
    public static final int MAX_TOTAL_CONTENT_CHARACTERS = 120_000;

    private final String id;
    private final String title;
    private final String deviceName;
    private final long createdAtMillis;
    private final long updatedAtMillis;
    private final List<AiChatMessage> messages;

    public AiChatConversation(String id, String title, String deviceName, long createdAtMillis,
                              long updatedAtMillis, List<AiChatMessage> messages) {
        this.id = identifier(id);
        this.title = text(title, "title", 80);
        this.deviceName = text(deviceName, "deviceName", 128);
        if (createdAtMillis <= 0 || updatedAtMillis < createdAtMillis) {
            throw new IllegalArgumentException("invalid conversation timestamps");
        }
        Objects.requireNonNull(messages, "messages");
        if (messages.size() > MAX_MESSAGES) throw new IllegalArgumentException("too many messages");
        int total = 0;
        List<AiChatMessage> copy = new ArrayList<>();
        for (AiChatMessage message : messages) {
            AiChatMessage safe = Objects.requireNonNull(message, "message");
            total += safe.getContent().length();
            if (total > MAX_TOTAL_CONTENT_CHARACTERS) {
                throw new IllegalArgumentException("conversation content is too large");
            }
            copy.add(safe);
        }
        this.createdAtMillis = createdAtMillis;
        this.updatedAtMillis = updatedAtMillis;
        this.messages = Collections.unmodifiableList(copy);
    }

    public static AiChatConversation create(String id, String deviceName, long now) {
        return new AiChatConversation(id, "New conversation", deviceName, now, now,
                Collections.emptyList());
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getDeviceName() { return deviceName; }
    public long getCreatedAtMillis() { return createdAtMillis; }
    public long getUpdatedAtMillis() { return updatedAtMillis; }
    public List<AiChatMessage> getMessages() { return messages; }

    public AiChatConversation append(AiChatMessage message) {
        Objects.requireNonNull(message, "message");
        List<AiChatMessage> updated = new ArrayList<>(messages);
        updated.add(message);
        while (updated.size() > MAX_MESSAGES) updated.remove(0);
        while (totalCharacters(updated) > MAX_TOTAL_CONTENT_CHARACTERS && updated.size() > 1) {
            updated.remove(0);
        }
        String nextTitle = title;
        if (messages.isEmpty() && message.getRole() == AiChatRole.USER) {
            nextTitle = compactTitle(message.getContent());
        }
        return new AiChatConversation(id, nextTitle, deviceName, createdAtMillis,
                Math.max(updatedAtMillis, message.getCreatedAtMillis()), updated);
    }

    public AiChatConversation rename(String newTitle, long now) {
        return new AiChatConversation(id, newTitle, deviceName, createdAtMillis,
                Math.max(updatedAtMillis, now), messages);
    }

    private static int totalCharacters(List<AiChatMessage> values) {
        int result = 0;
        for (AiChatMessage value : values) result += value.getContent().length();
        return result;
    }

    private static String compactTitle(String content) {
        String value = content.replace('\n', ' ').replace('\r', ' ').trim();
        return value.length() <= 32 ? value : value.substring(0, 32) + "…";
    }

    private static String identifier(String value) {
        String normalized = Objects.requireNonNull(value, "id").trim();
        if (!normalized.matches("[A-Za-z0-9_-]{1,64}")) throw new IllegalArgumentException("invalid id");
        return normalized;
    }

    private static String text(String value, String name, int maximum) {
        String normalized = Objects.requireNonNull(value, name).trim();
        if (normalized.isEmpty() || normalized.length() > maximum || normalized.indexOf('\0') >= 0) {
            throw new IllegalArgumentException("invalid " + name);
        }
        return normalized;
    }
}
