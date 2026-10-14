package com.chenwei666.netserial.terminal;

import java.util.Objects;

/** Immutable, redacted, in-memory view of one connected terminal. */
public final class ActiveTerminalSnapshot {
    private final String sessionId;
    private final TerminalSessionTransport transport;
    private final String targetLabel;
    private final String text;
    private final long updatedAtMillis;

    ActiveTerminalSnapshot(String sessionId, TerminalSessionTransport transport,
                           String targetLabel, String text, long updatedAtMillis) {
        this.sessionId = Objects.requireNonNull(sessionId, "sessionId");
        this.transport = Objects.requireNonNull(transport, "transport");
        this.targetLabel = Objects.requireNonNull(targetLabel, "targetLabel");
        this.text = Objects.requireNonNull(text, "text");
        this.updatedAtMillis = updatedAtMillis;
    }

    public String getSessionId() { return sessionId; }
    public TerminalSessionTransport getTransport() { return transport; }
    public String getTargetLabel() { return targetLabel; }
    public String getText() { return text; }
    public long getUpdatedAtMillis() { return updatedAtMillis; }
}
