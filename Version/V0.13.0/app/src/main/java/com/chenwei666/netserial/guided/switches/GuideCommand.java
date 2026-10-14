package com.chenwei666.netserial.guided.switches;

import com.chenwei666.netserial.guided.GuideText;

/** A read-only draft for manual review; it has no transport or execution capability. */
public final class GuideCommand {
    private final String command;
    private final GuideText purpose;

    GuideCommand(String command, GuideText purpose) {
        this.command = command;
        this.purpose = purpose;
    }

    public String getCommand() { return command; }
    public GuideText getPurpose() { return purpose; }
}
