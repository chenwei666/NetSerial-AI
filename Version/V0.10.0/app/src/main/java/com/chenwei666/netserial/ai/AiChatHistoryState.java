package com.chenwei666.netserial.ai;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class AiChatHistoryState {
    public static final int MAX_CONVERSATIONS = 12;

    private final List<AiChatConversation> conversations;
    private final String activeConversationId;

    public AiChatHistoryState(List<AiChatConversation> conversations, String activeConversationId) {
        Objects.requireNonNull(conversations, "conversations");
        if (conversations.size() > MAX_CONVERSATIONS) throw new IllegalArgumentException("too many conversations");
        List<AiChatConversation> copy = new ArrayList<>();
        for (AiChatConversation conversation : conversations) {
            AiChatConversation value = Objects.requireNonNull(conversation, "conversation");
            if (find(copy, value.getId()) != null) throw new IllegalArgumentException("duplicate conversation id");
            copy.add(value);
        }
        if (activeConversationId != null && find(copy, activeConversationId) == null) {
            throw new IllegalArgumentException("active conversation not found");
        }
        this.conversations = Collections.unmodifiableList(copy);
        this.activeConversationId = activeConversationId;
    }

    public static AiChatHistoryState empty() {
        return new AiChatHistoryState(Collections.emptyList(), null);
    }

    public List<AiChatConversation> getConversations() { return conversations; }
    public String getActiveConversationId() { return activeConversationId; }

    public AiChatConversation active() {
        return activeConversationId == null ? null : find(conversations, activeConversationId);
    }

    public AiChatHistoryState upsert(AiChatConversation conversation) {
        Objects.requireNonNull(conversation, "conversation");
        List<AiChatConversation> updated = new ArrayList<>();
        updated.add(conversation);
        for (AiChatConversation item : conversations) {
            if (!item.getId().equals(conversation.getId())) updated.add(item);
        }
        while (updated.size() > MAX_CONVERSATIONS) updated.remove(updated.size() - 1);
        return new AiChatHistoryState(updated, conversation.getId());
    }

    public AiChatHistoryState select(String id) {
        if (find(conversations, id) == null) throw new IllegalArgumentException("conversation not found");
        return new AiChatHistoryState(conversations, id);
    }

    public AiChatHistoryState delete(String id) {
        List<AiChatConversation> updated = new ArrayList<>();
        for (AiChatConversation item : conversations) if (!item.getId().equals(id)) updated.add(item);
        String active = Objects.equals(id, activeConversationId)
                ? (updated.isEmpty() ? null : updated.get(0).getId()) : activeConversationId;
        return new AiChatHistoryState(updated, active);
    }

    private static AiChatConversation find(List<AiChatConversation> values, String id) {
        if (id == null) return null;
        for (AiChatConversation value : values) if (value.getId().equals(id)) return value;
        return null;
    }
}
