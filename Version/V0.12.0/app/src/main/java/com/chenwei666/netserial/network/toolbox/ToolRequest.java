package com.chenwei666.netserial.network.toolbox;

/** Immutable input captured on the UI thread before any work starts. */
public final class ToolRequest {
    public final String target, option;
    public final int timeoutMs, count;
    public final boolean chinese;
    public ToolRequest(String target, String option, int timeoutMs, int count, boolean chinese) {
        this.target = target == null ? "" : target.trim();
        this.option = option == null ? "" : option.trim();
        if (timeoutMs < 1000 || timeoutMs > 15000 || count < 1 || count > 20)
            throw new IllegalArgumentException("Invalid probe limits");
        this.timeoutMs = timeoutMs; this.count = count; this.chinese = chinese;
    }
    public String text(String zh, String en) { return chinese ? zh : en; }
}
