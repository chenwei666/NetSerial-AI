package com.chenwei666.netserial.safety;

public final class CommandBatchInspector {
    public int count(String value) {
        if (value == null || value.trim().isEmpty()) return 0;
        int count = 0;
        for (String line : value.replace('\r', '\n').split("\\n")) {
            if (!line.trim().isEmpty()) count++;
        }
        return count;
    }
}
