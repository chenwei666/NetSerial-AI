package com.chenwei666.netserial.network.toolbox;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Structured facts for both the screen and its export; never infer success from raw output. */
public final class ToolResult {
    public enum Status { OK, PARTIAL, FAILED, UNSUPPORTED, CANCELLED }
    public static final class Row {
        public final String label, value;
        public Row(String label, String value) { this.label = label; this.value = value; }
    }
    public final Status status;
    public final String title, summary, raw;
    public final List<Row> rows;
    /** Ping RTT values in milliseconds; NaN denotes a missing reply, never zero. */
    public final List<Double> samples;
    public ToolResult(Status status, String title, String summary, List<Row> rows, String raw, List<Double> samples) {
        this.status = status; this.title = title; this.summary = summary;
        this.rows = Collections.unmodifiableList(new ArrayList<>(rows));
        this.raw = raw == null ? "" : raw;
        this.samples = Collections.unmodifiableList(new ArrayList<>(samples));
    }
    public ToolResult(Status status, String title, String summary, List<Row> rows, String raw) {
        this(status, title, summary, rows, raw, Collections.emptyList());
    }
    public static ToolResult message(Status status, String title, String summary) {
        return new ToolResult(status, title, summary, Collections.emptyList(), "");
    }
    public String export() {
        StringBuilder out = new StringBuilder(title).append("\n").append(status).append("\n").append(summary);
        for (Row row : rows) out.append("\n").append(row.label).append(": ").append(row.value);
        if (!raw.isEmpty()) out.append("\n\n").append(raw);
        return out.toString();
    }
}
