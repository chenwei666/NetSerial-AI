package com.chenwei666.netserial.automation;

import com.chenwei666.netserial.safety.RiskLevel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class PlaybookPlan {
    private final PlaybookType type;
    private final List<String> commands;
    private final RiskLevel risk;
    private final List<String> stopConditions;

    PlaybookPlan(PlaybookType type, List<String> commands, RiskLevel risk,
                 List<String> stopConditions) {
        this.type = type;
        this.commands = Collections.unmodifiableList(new ArrayList<>(commands));
        this.risk = risk;
        this.stopConditions = Collections.unmodifiableList(new ArrayList<>(stopConditions));
    }

    public PlaybookType getType() { return type; }
    public List<String> getCommands() { return commands; }
    public RiskLevel getRisk() { return risk; }
    public List<String> getStopConditions() { return stopConditions; }
    public String commandBatch() {
        StringBuilder result = new StringBuilder();
        for (String command : commands) {
            if (result.length() > 0) result.append('\n');
            result.append(command);
        }
        return result.toString();
    }
}
