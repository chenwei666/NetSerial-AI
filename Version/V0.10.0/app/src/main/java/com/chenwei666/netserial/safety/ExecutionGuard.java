package com.chenwei666.netserial.safety;

public interface ExecutionGuard {
    GuardDecision evaluate(CommandEvaluationRequest request);
}
