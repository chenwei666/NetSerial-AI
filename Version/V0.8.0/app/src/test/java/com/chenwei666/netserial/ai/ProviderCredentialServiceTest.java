package com.chenwei666.netserial.ai;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class ProviderCredentialServiceTest {

    @Test
    public void profilesUseIndependentCredentialAliases() {
        ProviderCredentialService service = new ProviderCredentialService(
                new SecureCredentialVault(
                        new TestCredentialComponents.XorSecretCipher(),
                        new TestCredentialComponents.InMemoryRecordStore()
                )
        );
        ProviderProfile first = profile("provider-first", "credential-first");
        ProviderProfile second = profile("provider-second", "credential-second");
        char[] firstValue = "first-example".toCharArray();
        char[] secondValue = "second-example".toCharArray();

        service.save(first, firstValue);
        service.save(second, secondValue);

        assertTrue(service.hasCredential(first));
        assertTrue(service.hasCredential(second));
        service.withCredential(first, credential -> {
            assertArrayEquals(firstValue, credential);
            return null;
        });
        service.withCredential(second, credential -> {
            assertArrayEquals(secondValue, credential);
            return null;
        });

        service.delete(first);

        assertFalse(service.hasCredential(first));
        assertTrue(service.hasCredential(second));
    }

    private static ProviderProfile profile(String providerId, String credentialAlias) {
        return ProviderProfile.remote(
                providerId,
                "https://ai.example.com/v1",
                "network-ops-model",
                credentialAlias
        );
    }

}
