package com.chenwei666.netserial.ai;

import com.chenwei666.netserial.safety.CommandEvaluationRequest;
import com.chenwei666.netserial.safety.ExecutionGuard;
import com.chenwei666.netserial.safety.GuardDecision;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class SafeAiCopilot implements AiCopilot {
    private final AiProvider provider;
    private final ExecutionGuard executionGuard;

    public SafeAiCopilot(AiProvider provider, ExecutionGuard executionGuard) {
        this.provider = Objects.requireNonNull(provider, "provider");
        this.executionGuard = Objects.requireNonNull(executionGuard, "executionGuard");
    }

    @Override
    public CommandPlan propose(AiRequest request) throws Exception {
        AiDraftPlan draft = Objects.requireNonNull(provider.propose(request), "AI draft plan");
        List<EvaluatedCommandStep> evaluated = new ArrayList<>();
        for (AiDraftStep step : draft.getSteps()) {
            GuardDecision decision = executionGuard.evaluate(new CommandEvaluationRequest(
                    request.getVendor(),
                    request.getCliMode(),
                    step.getCommand(),
                    step.getProposedRisk()
            ));
            evaluated.add(new EvaluatedCommandStep(
                    step.getCommand(),
                    decision.getEffectiveRisk(),
                    decision.isAutomaticExecutionAllowed()
            ));
        }
        return new CommandPlan(evaluated);
    }
}
