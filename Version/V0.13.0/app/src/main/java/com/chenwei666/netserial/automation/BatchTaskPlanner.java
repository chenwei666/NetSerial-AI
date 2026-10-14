package com.chenwei666.netserial.automation;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/** Plans a canary-first batch. It intentionally has no execution capability. */
public final class BatchTaskPlanner {
    public BatchExecutionPlan plan(List<String> targets, PlaybookPlan playbook) {
        Objects.requireNonNull(targets, "targets");
        Objects.requireNonNull(playbook, "playbook");
        Set<String> unique = new LinkedHashSet<>();
        for (String raw : targets) {
            String target = raw == null ? "" : raw.trim();
            if (!target.matches("[A-Za-z0-9][A-Za-z0-9._:-]{0,252}")) {
                throw new IllegalArgumentException("invalid target");
            }
            unique.add(target);
        }
        if (unique.isEmpty() || unique.size() > 50) throw new IllegalArgumentException("target count");
        List<String> ordered = new ArrayList<>(unique);
        return new BatchExecutionPlan(ordered.get(0), ordered.subList(1, ordered.size()), playbook);
    }
}
