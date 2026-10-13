package com.chenwei666.netserial.automation;

public final class TargetCommandResult {
    private final boolean successful;
    private final String safeSummary;

    public TargetCommandResult(boolean successful, String safeSummary) {
        this.successful = successful;
        String value = safeSummary == null ? "" : safeSummary.trim();
        this.safeSummary = value.length() > 512 ? value.substring(0, 512) : value;
    }
    public boolean isSuccessful() { return successful; }
    public String getSafeSummary() { return safeSummary; }
}
