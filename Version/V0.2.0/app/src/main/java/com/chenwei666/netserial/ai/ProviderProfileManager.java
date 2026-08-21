package com.chenwei666.netserial.ai;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class ProviderProfileManager {
    private final ProviderProfilePersistence persistence;
    private final ProviderProfilesJsonCodec codec;
    private final AiProviderCatalog providerCatalog;

    public ProviderProfileManager(ProviderProfilePersistence persistence) {
        this(
                persistence,
                new ProviderProfilesJsonCodec(),
                AiProviderCatalog.createDefault()
        );
    }

    ProviderProfileManager(
            ProviderProfilePersistence persistence,
            ProviderProfilesJsonCodec codec,
            AiProviderCatalog providerCatalog
    ) {
        this.persistence = Objects.requireNonNull(persistence, "persistence");
        this.codec = Objects.requireNonNull(codec, "codec");
        this.providerCatalog = Objects.requireNonNull(providerCatalog, "providerCatalog");
    }

    public synchronized ProviderProfilesState load() {
        ProviderProfilesState state = codec.decode(persistence.read());
        try {
            for (ProviderProfile profile : state.getProfiles()) {
                providerCatalog.require(profile.getProviderId());
            }
            return state;
        } catch (IllegalArgumentException exception) {
            throw new ProviderProfileStoreException(
                    "Stored AI provider profile references an unknown provider",
                    exception
            );
        }
    }

    public synchronized ProviderProfilesState upsert(ProviderProfile profile) {
        Objects.requireNonNull(profile, "profile");
        providerCatalog.require(profile.getProviderId());
        ProviderProfilesState current = load();
        List<ProviderProfile> updated = new ArrayList<>(current.getProfiles());
        int existingIndex = indexOfAlias(updated, profile.getCredentialAlias());
        if (existingIndex >= 0) {
            updated.set(existingIndex, profile);
        } else {
            if (updated.size() >= ProviderProfilesState.MAX_PROFILES) {
                throw new ProviderProfileStoreException("AI provider profile limit reached");
            }
            updated.add(profile);
        }
        String activeAlias = current.getActiveCredentialAlias();
        if (activeAlias == null) {
            activeAlias = profile.getCredentialAlias();
        }
        return persist(new ProviderProfilesState(updated, activeAlias));
    }

    public synchronized ProviderProfilesState setActive(String credentialAlias) {
        String normalizedAlias = CredentialAliases.normalize(credentialAlias);
        ProviderProfilesState current = load();
        if (indexOfAlias(current.getProfiles(), normalizedAlias) < 0) {
            throw new ProviderProfileStoreException("AI provider profile not found");
        }
        return persist(new ProviderProfilesState(current.getProfiles(), normalizedAlias));
    }

    public synchronized ProviderProfilesState replace(
            String previousCredentialAlias,
            ProviderProfile replacement
    ) {
        String normalizedPreviousAlias = CredentialAliases.normalize(previousCredentialAlias);
        Objects.requireNonNull(replacement, "replacement");
        providerCatalog.require(replacement.getProviderId());
        ProviderProfilesState current = load();
        List<ProviderProfile> updated = new ArrayList<>(current.getProfiles());
        int previousIndex = indexOfAlias(updated, normalizedPreviousAlias);
        if (previousIndex < 0) {
            throw new ProviderProfileStoreException("AI provider profile not found");
        }
        int replacementIndex = indexOfAlias(updated, replacement.getCredentialAlias());
        if (replacementIndex >= 0 && replacementIndex != previousIndex) {
            throw new ProviderProfileStoreException("AI provider credential alias already exists");
        }
        updated.set(previousIndex, replacement);
        String activeAlias = normalizedPreviousAlias.equals(current.getActiveCredentialAlias())
                ? replacement.getCredentialAlias()
                : current.getActiveCredentialAlias();
        return persist(new ProviderProfilesState(updated, activeAlias));
    }

    public synchronized ProviderProfilesState delete(String credentialAlias) {
        String normalizedAlias = CredentialAliases.normalize(credentialAlias);
        ProviderProfilesState current = load();
        List<ProviderProfile> updated = new ArrayList<>(current.getProfiles());
        int index = indexOfAlias(updated, normalizedAlias);
        if (index < 0) {
            return current;
        }
        updated.remove(index);
        String activeAlias = current.getActiveCredentialAlias();
        if (normalizedAlias.equals(activeAlias)) {
            activeAlias = updated.isEmpty() ? null : updated.get(0).getCredentialAlias();
        }
        return persist(new ProviderProfilesState(updated, activeAlias));
    }

    private ProviderProfilesState persist(ProviderProfilesState state) {
        persistence.write(codec.encode(state));
        return state;
    }

    private static int indexOfAlias(List<ProviderProfile> profiles, String alias) {
        for (int index = 0; index < profiles.size(); index++) {
            if (profiles.get(index).getCredentialAlias().equals(alias)) {
                return index;
            }
        }
        return -1;
    }
}
