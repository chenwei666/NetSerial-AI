package com.chenwei666.netserial.ai;

public interface AiChatHistoryRepository {
    AiChatHistoryState load();
    void save(AiChatHistoryState state);
    void clear();
}
