package com.chenwei666.netserial.ai;

import java.util.Objects;

/** Prevents a stored credential from being sent to a different provider or HTTPS endpoint. */
public final class CredentialDestinationPolicy {
    private CredentialDestinationPolicy() { }

    public static boolean hasChanged(ProviderProfile existing, ProviderProfile replacement) {
        ProviderProfile before = Objects.requireNonNull(existing, "existing");
        ProviderProfile after = Objects.requireNonNull(replacement, "replacement");
        return !before.getProviderId().equals(after.getProviderId())
                || !before.getEndpoint().equals(after.getEndpoint());
    }
}
