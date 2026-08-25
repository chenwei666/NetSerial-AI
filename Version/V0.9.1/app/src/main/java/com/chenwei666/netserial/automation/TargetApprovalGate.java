package com.chenwei666.netserial.automation;

import java.util.List;

import com.chenwei666.netserial.safety.RiskLevel;

/**
 * Fail-closed authorization seam. Production implementations must apply target, change-window,
 * operator, and R0-R4 policy to every stage before returning true.
 */
public interface TargetApprovalGate {
    boolean isApproved(String changeId, String target, BatchStage stage, List<String> commands,
                       RiskLevel effectiveRisk);
}
