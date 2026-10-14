package com.chenwei666.netserial.automation;

import com.chenwei666.netserial.safety.CommandEvaluationRequest;
import com.chenwei666.netserial.safety.RiskLevel;
import com.chenwei666.netserial.safety.RuleBasedExecutionGuard;

import java.util.ArrayList;
import java.util.List;

/** Executes a canary first, requires per-target approval, and stops at the first failure. */
public final class ControlledBatchExecutor {
    public ControlledBatchResult execute(ControlledBatchRequest request, TargetCommandAdapter adapter,
                                         TargetApprovalGate approvalGate) {
        if (request == null || adapter == null || approvalGate == null) throw new IllegalArgumentException("Execution dependencies required");
        List<TargetExecutionRecord> records = new ArrayList<>();
        for (String target : request.getTargets()) {
            TargetExecutionRecord record = executeTarget(request, target, adapter, approvalGate);
            records.add(record);
            if (!record.isSuccessful()) break;
        }
        return new ControlledBatchResult(records);
    }

    private static TargetExecutionRecord executeTarget(ControlledBatchRequest request, String target,
                                                        TargetCommandAdapter adapter,
                                                        TargetApprovalGate approvalGate) {
        if (!approved(request, target, BatchStage.PRECHECK, request.getPrecheck(), approvalGate)) {
            return authorizationFailure(target, BatchStage.PRECHECK);
        }
        TargetCommandResult precheck;
        try {
            precheck = adapter.execute(target, BatchStage.PRECHECK, request.getPrecheck());
        } catch (Exception exception) {
            return adapterFailure(target, BatchStage.PRECHECK, false, exception);
        }
        if (!precheck.isSuccessful()) return failed(target, BatchStage.PRECHECK, false, precheck);

        boolean rollbackApproved = approved(request, target, BatchStage.ROLLBACK,
                request.getRollback(), approvalGate);
        if (!rollbackApproved) return authorizationFailure(target, BatchStage.ROLLBACK);
        if (!approved(request, target, BatchStage.CHANGE, request.getChange(), approvalGate)) {
            return authorizationFailure(target, BatchStage.CHANGE);
        }
        TargetCommandResult change;
        try {
            change = adapter.execute(target, BatchStage.CHANGE, request.getChange());
        } catch (Exception exception) {
            return rollback(request, target, adapter, rollbackApproved, BatchStage.CHANGE, failureResult(exception));
        }
        if (!change.isSuccessful()) return rollback(request, target, adapter, rollbackApproved, BatchStage.CHANGE, change);

        if (!approved(request, target, BatchStage.VERIFICATION, request.getVerification(), approvalGate)) {
            return rollback(request, target, adapter, rollbackApproved, BatchStage.VERIFICATION,
                    new TargetCommandResult(false, "Verification authorization missing"));
        }
        TargetCommandResult verification;
        try {
            verification = adapter.execute(target, BatchStage.VERIFICATION, request.getVerification());
        } catch (Exception exception) {
            return rollback(request, target, adapter, rollbackApproved, BatchStage.VERIFICATION, failureResult(exception));
        }
        if (!verification.isSuccessful()) return rollback(request, target, adapter, rollbackApproved, BatchStage.VERIFICATION, verification);
        return new TargetExecutionRecord(target, BatchStage.VERIFICATION, true, false,
                verification.getSafeSummary());
    }

    private static TargetExecutionRecord rollback(ControlledBatchRequest request, String target,
                                                   TargetCommandAdapter adapter, boolean rollbackApproved,
                                                   BatchStage failedStage,
                                                   TargetCommandResult failed) {
        if (!rollbackApproved) {
            return new TargetExecutionRecord(target, failedStage, false, false,
                    failed.getSafeSummary() + "; rollback authorization missing");
        }
        try {
            TargetCommandResult rollback = adapter.execute(target, BatchStage.ROLLBACK, request.getRollback());
            return new TargetExecutionRecord(target, failedStage, false, true,
                    failed.getSafeSummary() + "; rollback=" + rollback.isSuccessful());
        } catch (Exception exception) {
            return new TargetExecutionRecord(target, failedStage, false, true,
                    failed.getSafeSummary() + "; rollback adapter failure");
        }
    }

    private static TargetExecutionRecord failed(String target, BatchStage stage, boolean rollback,
                                                 TargetCommandResult result) {
        return new TargetExecutionRecord(target, stage, false, rollback, result.getSafeSummary());
    }

    private static TargetExecutionRecord adapterFailure(String target, BatchStage stage,
                                                         boolean rollback, Exception exception) {
        return new TargetExecutionRecord(target, stage, false, rollback,
                failureResult(exception).getSafeSummary());
    }

    private static TargetCommandResult failureResult(Exception exception) {
        return new TargetCommandResult(false,
                "Adapter failure: " + exception.getClass().getSimpleName());
    }

    private static boolean approved(ControlledBatchRequest request, String target, BatchStage stage,
                                    List<String> commands, TargetApprovalGate approvalGate) {
        try {
            return approvalGate.isApproved(request.getChangeId(), target, stage, commands,
                    effectiveRisk(request, stage, commands));
        } catch (RuntimeException exception) {
            return false;
        }
    }

    private static RiskLevel effectiveRisk(ControlledBatchRequest request, BatchStage stage,
                                           List<String> commands) {
        RiskLevel proposed = stage == BatchStage.CHANGE || stage == BatchStage.ROLLBACK
                ? request.getProposedChangeRisk() : RiskLevel.R1_READ_ONLY;
        StringBuilder batch = new StringBuilder();
        for (String command : commands) {
            if (batch.length() > 0) batch.append('\n');
            batch.append(command);
        }
        return RuleBasedExecutionGuard.createDefault().evaluate(new CommandEvaluationRequest(
                request.getVendor(), request.getCliMode(), batch.toString(), proposed)).getEffectiveRisk();
    }

    private static TargetExecutionRecord authorizationFailure(String target, BatchStage stage) {
        return new TargetExecutionRecord(target, stage, false, false,
                stage + " authorization missing");
    }
}
