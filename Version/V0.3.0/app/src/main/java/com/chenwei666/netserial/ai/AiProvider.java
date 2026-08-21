package com.chenwei666.netserial.ai;

@FunctionalInterface
public interface AiProvider {
    AiDraftPlan propose(AiRequest request) throws Exception;
}
