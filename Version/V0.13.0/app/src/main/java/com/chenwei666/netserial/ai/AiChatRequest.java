package com.chenwei666.netserial.ai;

import com.chenwei666.netserial.device.CliMode;
import com.chenwei666.netserial.device.Vendor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class AiChatRequest {
    public static final int MAX_CONTEXT_MESSAGES = 24;

    private final List<AiChatMessage> messages;
    private final Vendor vendor;
    private final CliMode cliMode;
    private final String deviceName;
    private final String terminalContext;
    private final String memoryContext;
    private final String responseLanguage;
    private final boolean autonomousTask;

    public AiChatRequest(List<AiChatMessage> messages, Vendor vendor, CliMode cliMode,
                         String deviceName, String terminalContext, String memoryContext,
                         String responseLanguage) {
        this(messages, vendor, cliMode, deviceName, terminalContext, memoryContext, responseLanguage, false);
    }

    public AiChatRequest(List<AiChatMessage> messages, Vendor vendor, CliMode cliMode,
                         String deviceName, String terminalContext, String memoryContext,
                         String responseLanguage, boolean autonomousTask) {
        Objects.requireNonNull(messages, "messages");
        if (messages.isEmpty()) throw new IllegalArgumentException("messages required");
        AiChatMessage latest = messages.get(messages.size() - 1);
        if (latest.getRole() != AiChatRole.USER) {
            throw new IllegalArgumentException("latest message must be a user message");
        }
        // Keep the original user-authorized goal for the entire bounded autonomous task.
        int window = autonomousTask && messages.size() > MAX_CONTEXT_MESSAGES ? MAX_CONTEXT_MESSAGES - 1 : MAX_CONTEXT_MESSAGES;
        int start = Math.max(0, messages.size() - window);
        while (start < messages.size() - 1 && messages.get(start).getRole() == AiChatRole.ASSISTANT) {
            start++;
        }
        List<AiChatMessage> copy = new ArrayList<>();
        if (autonomousTask && start > 0 && messages.get(0).getRole() == AiChatRole.USER) copy.add(messages.get(0));
        copy.addAll(messages.subList(start, messages.size()));
        this.messages = Collections.unmodifiableList(copy);
        this.vendor = Objects.requireNonNull(vendor, "vendor");
        this.cliMode = Objects.requireNonNull(cliMode, "cliMode");
        this.deviceName = bounded(deviceName, 128);
        this.terminalContext = bounded(terminalContext, 12_000);
        this.memoryContext = bounded(memoryContext, 8_000);
        this.responseLanguage = bounded(responseLanguage, 32);
        this.autonomousTask = autonomousTask;
    }

    public List<AiChatMessage> getMessages() { return messages; }
    public Vendor getVendor() { return vendor; }
    public CliMode getCliMode() { return cliMode; }
    public String getDeviceName() { return deviceName; }
    public String getTerminalContext() { return terminalContext; }
    public String getMemoryContext() { return memoryContext; }
    public String getResponseLanguage() { return responseLanguage; }
    public boolean isAutonomousTask() { return autonomousTask; }

    private static String bounded(String value, int limit) {
        String normalized = value == null ? "" : value.trim();
        if (normalized.length() <= limit) return normalized;
        return normalized.substring(normalized.length() - limit);
    }
}
