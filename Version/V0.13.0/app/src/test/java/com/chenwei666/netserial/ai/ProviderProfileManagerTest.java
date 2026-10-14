package com.chenwei666.netserial.ai;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

public class ProviderProfileManagerTest {

    @Test
    public void firstProfileBecomesActiveAndUpdatesInPlace() {
        InMemoryPersistence persistence = new InMemoryPersistence();
        ProviderProfileManager manager = new ProviderProfileManager(persistence);
        ProviderProfile original = profile(
                "openai",
                "https://api.openai.com/v1",
                "gpt-5-mini",
                "credential-main"
        );

        ProviderProfilesState created = manager.upsert(original);
        ProviderProfilesState updated = manager.upsert(profile(
                "openai",
                "https://api.openai.com/v1",
                "gpt-5",
                "credential-main"
        ));

        assertEquals("credential-main", created.getActiveCredentialAlias());
        assertEquals(1, updated.getProfiles().size());
        assertEquals("gpt-5", updated.getProfiles().get(0).getModel());
    }

    @Test
    public void activeSelectionAndDeletionRemainConsistent() {
        ProviderProfileManager manager = new ProviderProfileManager(
                new InMemoryPersistence()
        );
        ProviderProfile first = profile(
                "openai",
                "https://api.openai.com/v1",
                "gpt-5-mini",
                "credential-first"
        );
        ProviderProfile second = profile(
                "deepseek",
                "https://api.deepseek.com",
                "deepseek-v4-flash",
                "credential-second"
        );
        manager.upsert(first);
        manager.upsert(second);

        manager.setActive(second.getCredentialAlias());
        ProviderProfilesState afterSecondDeleted = manager.delete(
                second.getCredentialAlias()
        );
        ProviderProfilesState empty = manager.delete(first.getCredentialAlias());

        assertEquals("credential-first", afterSecondDeleted.getActiveCredentialAlias());
        assertEquals(1, afterSecondDeleted.getProfiles().size());
        assertNull(empty.getActiveCredentialAlias());
        assertEquals(0, empty.getProfiles().size());
    }

    @Test
    public void replacementRotatesAliasAndPreservesActiveSelection() {
        ProviderProfileManager manager = new ProviderProfileManager(
                new InMemoryPersistence()
        );
        ProviderProfile original = profile(
                "openai",
                "https://api.openai.com/v1",
                "gpt-5-mini",
                "credential-old"
        );
        manager.upsert(original);
        ProviderProfile replacement = profile(
                "deepseek",
                "https://api.deepseek.com",
                "deepseek-v4-flash",
                "credential-new"
        );

        ProviderProfilesState state = manager.replace(
                original.getCredentialAlias(),
                replacement
        );

        assertEquals(1, state.getProfiles().size());
        assertEquals("credential-new", state.getActiveCredentialAlias());
        assertEquals("deepseek", state.getProfiles().get(0).getProviderId());
    }

    @Test(expected = ProviderProfileStoreException.class)
    public void cannotActivateMissingProfile() {
        new ProviderProfileManager(new InMemoryPersistence()).setActive("missing");
    }

    @Test(expected = ProviderProfileStoreException.class)
    public void unknownStoredProviderIsRejected() {
        InMemoryPersistence persistence = new InMemoryPersistence();
        persistence.document = "{\"version\":1,\"profiles\":[{"
                + "\"providerId\":\"unknown\","
                + "\"endpoint\":\"https://ai.example.com/v1\","
                + "\"model\":\"model\","
                + "\"credentialAlias\":\"credential\"}]}";

        new ProviderProfileManager(persistence).load();
    }

    private static ProviderProfile profile(
            String providerId,
            String endpoint,
            String model,
            String alias
    ) {
        return ProviderProfile.remote(providerId, endpoint, model, alias);
    }

    private static final class InMemoryPersistence implements ProviderProfilePersistence {
        private String document;

        @Override
        public String read() {
            return document;
        }

        @Override
        public void write(String document) {
            this.document = document;
        }
    }
}
