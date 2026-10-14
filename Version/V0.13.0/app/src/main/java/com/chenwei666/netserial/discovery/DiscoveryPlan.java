package com.chenwei666.netserial.discovery;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class DiscoveryPlan {
    private final List<String> commands;
    DiscoveryPlan(List<String> commands) {
        this.commands = Collections.unmodifiableList(new ArrayList<>(commands));
    }
    public List<String> getCommands() { return commands; }
}
