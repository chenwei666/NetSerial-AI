package com.chenwei666.netserial.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class ConfigDiff {
    private final List<String> addedLines;
    private final List<String> removedLines;

    ConfigDiff(List<String> addedLines, List<String> removedLines) {
        this.addedLines = Collections.unmodifiableList(new ArrayList<>(addedLines));
        this.removedLines = Collections.unmodifiableList(new ArrayList<>(removedLines));
    }

    public List<String> getAddedLines() { return addedLines; }
    public List<String> getRemovedLines() { return removedLines; }
    public boolean isEmpty() { return addedLines.isEmpty() && removedLines.isEmpty(); }

    public String toUnifiedText() {
        StringBuilder result = new StringBuilder();
        for (String line : removedLines) result.append("- ").append(line).append('\n');
        for (String line : addedLines) result.append("+ ").append(line).append('\n');
        return result.toString();
    }
}
