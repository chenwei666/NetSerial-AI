package com.chenwei666.netserial.web;

import com.chenwei666.netserial.device.Vendor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class WebAccessPlan implements AutoCloseable {
    private final Vendor vendor;
    private final List<String> commands;
    private final List<String> redactedCommands;
    private final List<String> verificationCommands;
    private final List<String> rollbackCommands;
    private final boolean containsPlainHttp;

    WebAccessPlan(Vendor vendor, List<String> commands, List<String> redactedCommands,
                  List<String> verificationCommands, List<String> rollbackCommands,
                  boolean containsPlainHttp) {
        this.vendor = vendor;
        this.commands = new ArrayList<>(commands);
        this.redactedCommands = immutable(redactedCommands);
        this.verificationCommands = immutable(verificationCommands);
        this.rollbackCommands = immutable(rollbackCommands);
        this.containsPlainHttp = containsPlainHttp;
    }

    public Vendor getVendor() { return vendor; }
    public List<String> getCommands() { return Collections.unmodifiableList(new ArrayList<>(commands)); }
    public List<String> getRedactedCommands() { return redactedCommands; }
    public List<String> getVerificationCommands() { return verificationCommands; }
    public List<String> getRollbackCommands() { return rollbackCommands; }
    public boolean containsPlainHttp() { return containsPlainHttp; }
    public String commandBatch() { return join(commands); }
    public String redactedBatch() { return join(redactedCommands); }

    public synchronized void destroy() { commands.clear(); }

    @Override public void close() { destroy(); }

    private static List<String> immutable(List<String> values) {
        return Collections.unmodifiableList(new ArrayList<>(values));
    }

    private static String join(List<String> values) {
        StringBuilder output = new StringBuilder();
        for (String value : values) {
            if (output.length() > 0) output.append('\n');
            output.append(value);
        }
        return output.toString();
    }
}
