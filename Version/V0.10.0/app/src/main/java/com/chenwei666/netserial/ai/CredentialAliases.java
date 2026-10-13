package com.chenwei666.netserial.ai;

import java.util.Objects;

final class CredentialAliases {
    private CredentialAliases() {
    }

    static String normalize(String alias) {
        String normalized = Objects.requireNonNull(alias, "alias").trim();
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException("alias must not be empty");
        }
        if (normalized.length() > 128) {
            throw new IllegalArgumentException("alias must not exceed 128 characters");
        }
        return normalized;
    }
}
