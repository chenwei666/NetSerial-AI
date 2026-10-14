package com.chenwei666.netserial.terminal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class ActiveTerminalSessionStoreTest {
    @Test
    public void returnsMostRecentlyActiveConnectedSession() {
        ActiveTerminalSessionStore store = new ActiveTerminalSessionStore(4, 2048);
        store.connected("usb", TerminalSessionTransport.USB, "USB switch", "H3C", 10);
        store.connected("ssh", TerminalSessionTransport.SSH, "SSH switch", "Cisco", 20);
        store.append("usb", " Comware 7", 30);

        ActiveTerminalSnapshot snapshot = store.latestConnected();

        assertEquals("usb", snapshot.getSessionId());
        assertEquals(TerminalSessionTransport.USB, snapshot.getTransport());
        assertEquals("H3C Comware 7", snapshot.getText());
    }

    @Test
    public void excludesDisconnectedSessions() {
        ActiveTerminalSessionStore store = new ActiveTerminalSessionStore(2, 1024);
        store.connected("ssh", TerminalSessionTransport.SSH, "core", "Cisco IOS", 10);
        store.disconnected("ssh", 20);

        assertNull(store.latestConnected());
    }

    @Test
    public void keepsOnlyBoundedRedactedPlainText() {
        ActiveTerminalSessionStore store = new ActiveTerminalSessionStore(1, 1024);
        store.connected("telnet", TerminalSessionTransport.TELNET, "legacy",
                "\u001B[31mversion\u001B[0m\n", 10);
        for (int index = 0; index < 80; index++) {
            store.append("telnet", "line-" + index + "-abcdefghijklmnop\n", 11 + index);
        }
        store.append("telnet", "password=example-sensitive-value\n", 100);

        String text = store.latestConnected().getText();

        assertFalse(text.contains("\u001B"));
        assertFalse(text.contains("example-sensitive-value"));
        assertTrue(text.contains("password=[REDACTED]"));
        assertTrue(text.length() <= 1024);
        assertTrue(text.contains("line-79"));
    }

    @Test
    public void evictsLeastRecentlyActiveSessionAtCapacity() {
        ActiveTerminalSessionStore store = new ActiveTerminalSessionStore(2, 1024);
        store.connected("first", TerminalSessionTransport.USB, "first", "one", 10);
        store.connected("second", TerminalSessionTransport.SSH, "second", "two", 20);
        store.append("first", " recent", 30);
        store.connected("third", TerminalSessionTransport.TELNET, "third", "three", 40);
        store.disconnected("third", 50);
        store.disconnected("first", 60);

        assertNull(store.latestConnected());
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsNegativeTimestamps() {
        ActiveTerminalSessionStore store = new ActiveTerminalSessionStore(1, 1024);
        store.connected("usb", TerminalSessionTransport.USB, "switch", "", -1);
    }
}
