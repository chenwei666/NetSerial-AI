package com.chenwei666.netserial.ai;

import java.util.Objects;

public final class AiProviderDefinition {
    private final String id;
    private final String displayName;
    private final boolean customEndpointAllowed;
    private final boolean localProvider;

    public AiProviderDefinition(String id, String displayName, boolean customEndpointAllowed,
                                boolean localProvider) {
        this.id = requireText(id, "id");
        this.displayName = requireText(displayName, "displayName");
        this.customEndpointAllowed = customEndpointAllowed;
        this.localProvider = localProvider;
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isCustomEndpointAllowed() {
        return customEndpointAllowed;
    }

    public boolean isLocalProvider() {
        return localProvider;
    }

    private static String requireText(String value, String name) {
        String normalized = Objects.requireNonNull(value, name).trim();
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException(name + " must not be empty");
        }
        return normalized;
    }
}
