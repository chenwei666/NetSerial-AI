package com.chenwei666.netserial.commands;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.Collections;

public class CommandUsageHistoryTest {
    @Test public void favoriteCanBeAddedAndRemoved() {
        CommandUsageHistory history = CommandUsageHistory.empty();
        assertTrue(history.toggleFavorite("H3C|USER|display version"));
        assertTrue(history.isFavorite("H3C|USER|display version"));
        assertFalse(history.toggleFavorite("H3C|USER|display version"));
    }

    @Test public void recentUseIsDeduplicatedAndNewestFirst() {
        CommandUsageHistory history = new CommandUsageHistory(Collections.emptyList(), Collections.emptyList());
        history.recordUse("one"); history.recordUse("two"); history.recordUse("one");
        assertEquals(2, history.getRecent().size());
        assertEquals("one", history.getRecent().get(0));
    }

    @Test public void recentHistoryIsBounded() {
        CommandUsageHistory history = CommandUsageHistory.empty();
        for (int index = 0; index < 50; index++) history.recordUse("command-" + index);
        assertEquals(CommandUsageHistory.MAX_RECENT, history.getRecent().size());
    }
}
