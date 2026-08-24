package com.chenwei666.netserial.automation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class BatchExecutionPlan {
    private final String canaryTarget;
    private final List<String> remainingTargets;
    private final PlaybookPlan playbook;

    BatchExecutionPlan(String canaryTarget, List<String> remainingTargets, PlaybookPlan playbook) {
        this.canaryTarget = canaryTarget;
        this.remainingTargets = Collections.unmodifiableList(new ArrayList<>(remainingTargets));
        this.playbook = playbook;
    }

    public String getCanaryTarget() { return canaryTarget; }
    public List<String> getRemainingTargets() { return remainingTargets; }
    public PlaybookPlan getPlaybook() { return playbook; }
    public boolean requiresPerTargetApproval() { return true; }
    public boolean stopsOnFirstFailure() { return true; }
}
