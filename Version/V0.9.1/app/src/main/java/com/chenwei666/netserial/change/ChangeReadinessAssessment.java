package com.chenwei666.netserial.change;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class ChangeReadinessAssessment {
    private final List<String> missingRequirements;

    ChangeReadinessAssessment(List<String> missingRequirements) {
        this.missingRequirements = Collections.unmodifiableList(new ArrayList<>(missingRequirements));
    }

    public boolean isReady() { return missingRequirements.isEmpty(); }
    public List<String> getMissingRequirements() { return missingRequirements; }
}
