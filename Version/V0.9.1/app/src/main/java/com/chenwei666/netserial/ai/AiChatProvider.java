package com.chenwei666.netserial.ai;

@FunctionalInterface
public interface AiChatProvider {
    AiChatResponse chat(AiChatRequest request, RequestCancellation cancellation) throws Exception;
}
