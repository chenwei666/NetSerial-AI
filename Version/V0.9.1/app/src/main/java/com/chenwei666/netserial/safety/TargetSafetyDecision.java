package com.chenwei666.netserial.safety;

public final class TargetSafetyDecision {
    private final boolean allowed;
    private final TargetSafetyReason reason;

    public TargetSafetyDecision(boolean allowed, TargetSafetyReason reason) {
        this.allowed = allowed;
        this.reason = reason == null ? TargetSafetyReason.TARGET_MISMATCH : reason;
    }

    public boolean isAllowed() { return allowed; }
    public TargetSafetyReason getReason() { return reason; }
}
