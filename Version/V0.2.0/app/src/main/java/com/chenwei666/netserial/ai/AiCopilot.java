package com.chenwei666.netserial.ai;

public interface AiCopilot {
    CommandPlan propose(AiRequest request) throws Exception;
}
