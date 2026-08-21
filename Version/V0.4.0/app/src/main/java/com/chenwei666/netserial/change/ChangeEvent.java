package com.chenwei666.netserial.change;

import java.util.Objects;

public final class ChangeEvent {
    private final long timestampMillis;
    private final ChangeEventType type;
    private final String target;
    private final String detail;

    public ChangeEvent(long timestampMillis, ChangeEventType type, String target, String detail) {
        if (timestampMillis <= 0) throw new IllegalArgumentException("timestamp must be positive");
        this.timestampMillis = timestampMillis;
        this.type = Objects.requireNonNull(type, "type");
        this.target = bounded(target, 256, "target");
        this.detail = bounded(detail, 8_000, "detail");
    }

    public long getTimestampMillis() { return timestampMillis; }
    public ChangeEventType getType() { return type; }
    public String getTarget() { return target; }
    public String getDetail() { return detail; }

    private static String bounded(String value, int maximum, String field) {
        String normalized = Objects.requireNonNull(value, field).trim();
        if (normalized.length() > maximum) throw new IllegalArgumentException(field + " is too long");
        return normalized;
    }
}
