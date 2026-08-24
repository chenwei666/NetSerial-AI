package com.chenwei666.netserial.automation;

public final class TargetExecutionRecord {
    private final String target;
    private final BatchStage lastStage;
    private final boolean successful;
    private final boolean rollbackAttempted;
    private final String safeSummary;

    TargetExecutionRecord(String target, BatchStage lastStage, boolean successful,
                          boolean rollbackAttempted, String safeSummary) {
        this.target = target;
        this.lastStage = lastStage;
        this.successful = successful;
        this.rollbackAttempted = rollbackAttempted;
        this.safeSummary = safeSummary == null ? "" : safeSummary;
    }
    public String getTarget() { return target; }
    public BatchStage getLastStage() { return lastStage; }
    public boolean isSuccessful() { return successful; }
    public boolean isRollbackAttempted() { return rollbackAttempted; }
    public String getSafeSummary() { return safeSummary; }
}
