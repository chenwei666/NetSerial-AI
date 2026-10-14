package com.chenwei666.netserial.ai;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Test;

import java.util.Arrays;

public class ProviderProfilesJsonCodecTest {
    private final ProviderProfilesJsonCodec codec = new ProviderProfilesJsonCodec();

    @Test
    public void roundTripPersistsReferencesButNeverCredentialPlaintext() {
        ProviderProfile openAi = profile(
                "openai",
                "https://api.openai.com/v1",
                "gpt-5-mini",
                "credential-openai"
        );
        ProviderProfile deepSeek = profile(
                "deepseek",
                "https://api.deepseek.com",
                "deepseek-v4-flash",
                "credential-deepseek"
        );
        ProviderProfilesState state = new ProviderProfilesState(
                Arrays.asList(openAi, deepSeek),
                deepSeek.getCredentialAlias()
        );

        String document = codec.encode(state);
        ProviderProfilesState decoded = codec.decode(document);

        assertEquals(2, decoded.getProfiles().size());
        assertEquals("credential-deepseek", decoded.getActiveCredentialAlias());
        assertEquals("gpt-5-mini", decoded.getProfiles().get(0).getModel());
        assertFalse(document.contains("secret-value"));
        assertFalse(document.toLowerCase().contains("api_key"));
    }

    @Test(expected = ProviderProfileStoreException.class)
    public void malformedDocumentIsRejectedWithoutPartialRecovery() {
        codec.decode("{\"version\":1,\"profiles\":[{\"providerId\":\"openai\"}]}");
    }

    @Test(expected = ProviderProfileStoreException.class)
    public void activeAliasMustReferenceStoredProfile() {
        codec.decode(
                "{\"version\":1,\"activeCredentialAlias\":\"missing\",\"profiles\":[]}"
        );
    }

    private static ProviderProfile profile(
            String providerId,
            String endpoint,
            String model,
            String alias
    ) {
        return ProviderProfile.remote(providerId, endpoint, model, alias);
    }
}
