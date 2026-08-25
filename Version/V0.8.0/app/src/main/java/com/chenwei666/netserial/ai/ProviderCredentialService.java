package com.chenwei666.netserial.ai;

import java.util.Objects;

/**
 * Binds provider profiles to encrypted credentials without adding plaintext to profiles.
 */
public final class ProviderCredentialService {
    private final CredentialVault credentialVault;

    public ProviderCredentialService(CredentialVault credentialVault) {
        this.credentialVault = Objects.requireNonNull(credentialVault, "credentialVault");
    }

    public void save(ProviderProfile profile, char[] credential) {
        credentialVault.store(requireProfile(profile).getCredentialAlias(), credential);
    }

    public boolean hasCredential(ProviderProfile profile) {
        return credentialVault.contains(requireProfile(profile).getCredentialAlias());
    }

    public <T> T withCredential(
            ProviderProfile profile,
            CredentialOperation<T> operation
    ) {
        return credentialVault.withCredential(
                requireProfile(profile).getCredentialAlias(),
                operation
        );
    }

    public void delete(ProviderProfile profile) {
        credentialVault.delete(requireProfile(profile).getCredentialAlias());
    }

    private static ProviderProfile requireProfile(ProviderProfile profile) {
        return Objects.requireNonNull(profile, "profile");
    }
}
