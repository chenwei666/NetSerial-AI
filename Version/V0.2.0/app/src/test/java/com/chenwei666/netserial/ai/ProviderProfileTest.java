package com.chenwei666.netserial.ai;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class ProviderProfileTest {

    @Test
    public void remoteProfileStoresCredentialReferenceInsteadOfSecret() {
        ProviderProfile profile = ProviderProfile.remote(
                "deepseek",
                "https://api.deepseek.com/v1",
                "deepseek-chat",
                "credential-deepseek-main"
        );

        assertEquals("deepseek", profile.getProviderId());
        assertEquals("https://api.deepseek.com/v1", profile.getEndpoint().toString());
        assertEquals("deepseek-chat", profile.getModel());
        assertEquals("credential-deepseek-main", profile.getCredentialAlias());
    }

    @Test(expected = IllegalArgumentException.class)
    public void remoteProfileRejectsCleartextEndpoint() {
        ProviderProfile.remote(
                "openai-compatible",
                "http://ai.example.com/v1",
                "network-ops-model",
                "credential-compatible-main"
        );
    }
}
