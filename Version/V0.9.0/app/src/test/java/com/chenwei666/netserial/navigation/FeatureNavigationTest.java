package com.chenwei666.netserial.navigation;

import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.*;

public class FeatureNavigationTest {
    @Test public void registryHasUniqueSearchableFeatures() {
        FeatureRegistry registry = FeatureRegistry.createDefault();
        assertEquals(15, registry.all().size());
        assertEquals(8, registry.forCategory(FeatureCategory.TOOLBOX).size());
        assertEquals(FeatureId.AI_COPILOT,
                registry.search("diagnose", id -> "").get(0).getId());
    }

    @Test public void usageBoundsFavoritesAndRecentAndIgnoresCorruption() {
        MemoryPersistence persistence = new MemoryPersistence();
        FeatureUsageManager manager = new FeatureUsageManager(persistence);
        for (FeatureId id : Arrays.asList(FeatureId.AI_COPILOT, FeatureId.COMMAND_LIBRARY,
                FeatureId.NETWORK_TOOLS, FeatureId.OPERATIONS_CENTER, FeatureId.CHANGE_TASK)) {
            manager.toggleFavorite(id);
            manager.recordOpen(id);
        }
        assertEquals(4, manager.favorites().size());
        assertFalse(manager.favorites().contains(FeatureId.AI_COPILOT));
        assertEquals(5, manager.recent().size());
        persistence.document = "version=1\nfavorites=NOT_REAL,AI_COPILOT,AI_COPILOT\nrecent=BROKEN";
        assertEquals(Arrays.asList(FeatureId.AI_COPILOT), manager.favorites());
        assertTrue(manager.recent().isEmpty());
    }

    private static final class MemoryPersistence implements FeatureUsagePersistence {
        String document = "";
        @Override public String read() { return document; }
        @Override public void write(String document) { this.document = document; }
    }
}
