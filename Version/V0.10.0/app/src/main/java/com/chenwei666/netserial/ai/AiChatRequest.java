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

    public AiChatRequest(List<AiChatMessage> messages, Vendor vendor, CliMode cliMode,
                         String deviceName, String terminalContext, String memoryContext,
                         String responseLanguage) {
        Objects.requireNonNull(messages, "messages");
        if (messages.isEmpty()) throw new IllegalArgumentException("messages required");
        AiChatMessage latest = messages.get(messages.size() - 1);
        if (latest.getRole() != AiChatRole.USER) {
            throw new IllegalArgumentException("latest message must be a user message");
        }
        int start = Math.max(0, messages.size() - MAX_CONTEXT_MESSAGES);
        while (start < messages.size() - 1 && messages.get(start).getRole() == AiChatRole.ASSISTANT) {
            start++;
        }
        this.messages = Collections.unmodifiableList(new ArrayList<>(messages.subList(start, messages.size())));
        this.vendor = Objects.requireNonNull(vendor, "vendor");
        this.cliMode = Objects.requireNonNull(cliMode, "cliMode");
        this.deviceName = bounded(deviceName, 128);
        this.terminalContext = bounded(terminalContext, 12_000);
        this.memoryContext = bounded(memoryContext, 8_000);
        this.responseLanguage = bounded(responseLanguage, 32);
    }

    public List<AiChatMessage> getMessages() { return messages; }
    public Vendor getVendor() { return vendor; }
    public CliMode getCliMode() { return cliMode; }
    public String getDeviceName() { return deviceName; }
    public String getTerminalContext() { return terminalContext; }
    public String getMemoryContext() { return memoryContext; }
    public String getResponseLanguage() { return responseLanguage; }

    private static String bounded(String value, int limit) {
        String normalized = value == null ? "" : value.trim();
        if (normalized.length() <= limit) return normalized;
        return normalized.substring(normalized.length() - limit);
    }
}
