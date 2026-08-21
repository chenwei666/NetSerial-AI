package com.chenwei666.netserial.ai;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class ProviderProfilesState {
    public static final int MAX_PROFILES = 32;

    private final List<ProviderProfile> profiles;
    private final String activeCredentialAlias;

    public ProviderProfilesState(
            List<ProviderProfile> profiles,
            String activeCredentialAlias
    ) {
        if (profiles == null) {
            throw new NullPointerException("profiles");
        }
        if (profiles.size() > MAX_PROFILES) {
            throw new IllegalArgumentException("too many AI provider profiles");
        }
        List<ProviderProfile> copy = new ArrayList<>(profiles);
        Set<String> aliases = new HashSet<>();
        for (ProviderProfile profile : copy) {
            if (profile == null) {
                throw new NullPointerException("profile");
            }
            if (!aliases.add(profile.getCredentialAlias())) {
                throw new IllegalArgumentException("duplicate AI provider credential alias");
            }
        }
        String normalizedActive = activeCredentialAlias == null
                ? null
                : CredentialAliases.normalize(activeCredentialAlias);
        if (normalizedActive != null && !aliases.contains(normalizedActive)) {
            throw new IllegalArgumentException("active AI provider profile does not exist");
        }
        this.profiles = Collections.unmodifiableList(copy);
        this.activeCredentialAlias = normalizedActive;
    }

    public static ProviderProfilesState empty() {
        return new ProviderProfilesState(Collections.emptyList(), null);
    }

    public List<ProviderProfile> getProfiles() {
        return profiles;
    }

    public String getActiveCredentialAlias() {
        return activeCredentialAlias;
    }

    public boolean isActive(ProviderProfile profile) {
        return profile != null
                && profile.getCredentialAlias().equals(activeCredentialAlias);
    }
}
