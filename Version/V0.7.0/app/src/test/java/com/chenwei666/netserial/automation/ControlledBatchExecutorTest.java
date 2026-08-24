package com.chenwei666.netserial.automation;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class ControlledBatchExecutorTest {
    @Test public void executesCanaryFirstAndStopsOnFailureWithRollback() {
        ControlledBatchRequest request = request();
        List<String> calls = new ArrayList<>();
        TargetCommandAdapter adapter = (target, stage, commands) -> {
            calls.add(target + ":" + stage);
            if (target.equals("sw2") && stage == BatchStage.VERIFICATION) return new TargetCommandResult(false, "failed");
            return new TargetCommandResult(true, "ok");
        };
        ControlledBatchResult result = new ControlledBatchExecutor().execute(request, adapter,
                (change, target, stage, commands, risk) -> true);
        assertFalse(result.isSuccessful());
        assertEquals(2, result.getRecords().size());
        assertTrue(result.getRecords().get(1).isRollbackAttempted());
        assertFalse(calls.toString().contains("sw3"));
    }

    @Test public void blocksMissingApprovalAndInlineCredentials() {
        ControlledBatchResult result = new ControlledBatchExecutor().execute(request(),
                (target, stage, commands) -> new TargetCommandResult(true, "ok"),
                (change, target, stage, commands, risk) -> false);
        assertEquals(1, result.getRecords().size());
        assertFalse(result.getRecords().get(0).isSuccessful());
        try {
            new ControlledBatchRequest("CHG-1", Arrays.asList("sw1"), Arrays.asList("show clock"),
                    Arrays.asList("username x password plain"), Arrays.asList("show vlan"), Arrays.asList("undo vlan 10"));
            fail("inline secrets must be rejected");
        } catch (IllegalArgumentException expected) { }
    }

    @Test public void rollsBackWhenVerificationAdapterThrows() {
        List<BatchStage> calls = new ArrayList<>();
        TargetCommandAdapter adapter = (target, stage, commands) -> {
            calls.add(stage);
            if (stage == BatchStage.VERIFICATION) throw new IllegalStateException("sensitive detail");
            return new TargetCommandResult(true, "ok");
        };
        ControlledBatchResult result = new ControlledBatchExecutor().execute(request(), adapter,
                (change, target, stage, commands, risk) -> true);
        assertFalse(result.isSuccessful());
        assertTrue(result.getRecords().get(0).isRollbackAttempted());
        assertEquals(BatchStage.VERIFICATION, result.getRecords().get(0).getLastStage());
        assertTrue(calls.contains(BatchStage.ROLLBACK));
        assertFalse(result.getRecords().get(0).getSafeSummary().contains("sensitive detail"));
    }

    @Test public void authorizesEveryStageBeforeCallingAdapter() {
        List<BatchStage> calls = new ArrayList<>();
        ControlledBatchResult result = new ControlledBatchExecutor().execute(request(),
                (target, stage, commands) -> {
                    calls.add(stage);
                    return new TargetCommandResult(true, "ok");
                }, (change, target, stage, commands, risk) -> stage != BatchStage.CHANGE);
        assertFalse(result.isSuccessful());
        assertEquals(Arrays.asList(BatchStage.PRECHECK), calls);
        assertEquals(BatchStage.CHANGE, result.getRecords().get(0).getLastStage());
    }

    @Test public void requiresRollbackAuthorizationBeforeChange() {
        List<BatchStage> calls = new ArrayList<>();
        ControlledBatchResult result = new ControlledBatchExecutor().execute(request(),
                (target, stage, commands) -> {
                    calls.add(stage);
                    return new TargetCommandResult(true, "ok");
                }, (change, target, stage, commands, risk) -> stage != BatchStage.ROLLBACK);
        assertFalse(result.isSuccessful());
        assertEquals(Arrays.asList(BatchStage.PRECHECK), calls);
        assertEquals(BatchStage.ROLLBACK, result.getRecords().get(0).getLastStage());
    }

    private static ControlledBatchRequest request() {
        return new ControlledBatchRequest("CHG-1", Arrays.asList("sw1", "sw2", "sw3"),
                Arrays.asList("show clock"), Arrays.asList("vlan 10"),
                Arrays.asList("show vlan 10"), Arrays.asList("no vlan 10"));
    }
}
