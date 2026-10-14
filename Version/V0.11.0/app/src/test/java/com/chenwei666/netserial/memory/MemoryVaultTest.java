package com.chenwei666.netserial.memory;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class MemoryVaultTest {
    @Test
    public void recallsGlobalAndMatchingScopeOnly() {
        InMemoryPersistence persistence = new InMemoryPersistence();
        MemoryVault vault = new MemoryVault(persistence);
        long now = 1_000L;
        vault.add(new MemoryRecord("global", MemoryScope.GLOBAL, "all", "Prefer Chinese output",
                "user", MemoryTrust.USER_VERIFIED, now, 0));
        vault.add(new MemoryRecord("h3c", MemoryScope.VENDOR, "H3C_COMWARE", "Use display first",
                "user", MemoryTrust.USER_VERIFIED, now + 1, 0));
        vault.add(new MemoryRecord("cisco", MemoryScope.VENDOR, "CISCO_IOS", "Use show first",
                "user", MemoryTrust.USER_VERIFIED, now + 2, 0));

        assertEquals(2, vault.recall(MemoryScope.VENDOR, "H3C_COMWARE", 10, now + 3).size());
    }

    @Test
    public void rejectsSecretsAndPurgesExpiredRecords() {
        MemoryVault vault = new MemoryVault(new InMemoryPersistence());
        long now = 2_000L;
        try {
            vault.add(new MemoryRecord("secret", MemoryScope.GLOBAL, "all", "password=demo123",
                    "user", MemoryTrust.USER_VERIFIED, now, 0));
            fail("Expected secret rejection");
        } catch (IllegalArgumentException expected) {
            // Expected.
        }
        vault.add(new MemoryRecord("expired", MemoryScope.GLOBAL, "all", "Temporary fact",
                "user", MemoryTrust.USER_VERIFIED, now, now + 1));
        assertEquals(0, vault.list(now + 2).size());
    }

    private static final class InMemoryPersistence implements MemoryVault.Persistence {
        private String document;
        @Override public String read() { return document; }
        @Override public void write(String document) { this.document = document; }
    }
}
