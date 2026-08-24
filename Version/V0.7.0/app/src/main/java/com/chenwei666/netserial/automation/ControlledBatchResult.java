package com.chenwei666.netserial.automation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class ControlledBatchResult {
    private final List<TargetExecutionRecord> records;
    ControlledBatchResult(List<TargetExecutionRecord> records) {
        this.records = Collections.unmodifiableList(new ArrayList<>(records));
    }
    public List<TargetExecutionRecord> getRecords() { return records; }
    public boolean isSuccessful() {
        if (records.isEmpty()) return false;
        for (TargetExecutionRecord record : records) if (!record.isSuccessful()) return false;
        return true;
    }
}
