package com.chenwei666.netserial.automation;

import java.util.Objects;

public final class GuidedRunbookStep {
    private final int sequence;
    private final String command;
    private final String expectedPromptPattern;
    private final int timeoutSeconds;
    private final int maxRetries;
    private final RunbookFailureAction failureAction;

    GuidedRunbookStep(int sequence, String command, String expectedPromptPattern,
                      int timeoutSeconds, int maxRetries, RunbookFailureAction failureAction) {
        if (sequence < 1) throw new IllegalArgumentException("sequence must be positive");
        this.sequence = sequence;
        this.command = Objects.requireNonNull(command, "command");
        this.expectedPromptPattern = Objects.requireNonNull(expectedPromptPattern, "expectedPromptPattern");
        if (timeoutSeconds < 1 || timeoutSeconds > 120) throw new IllegalArgumentException("invalid timeout");
        if (maxRetries < 0 || maxRetries > 2) throw new IllegalArgumentException("invalid retries");
        this.timeoutSeconds = timeoutSeconds;
        this.maxRetries = maxRetries;
        this.failureAction = Objects.requireNonNull(failureAction, "failureAction");
    }

    public int getSequence() { return sequence; }
    public String getCommand() { return command; }
    public String getExpectedPromptPattern() { return expectedPromptPattern; }
    public int getTimeoutSeconds() { return timeoutSeconds; }
    public int getMaxRetries() { return maxRetries; }
    public RunbookFailureAction getFailureAction() { return failureAction; }
}
