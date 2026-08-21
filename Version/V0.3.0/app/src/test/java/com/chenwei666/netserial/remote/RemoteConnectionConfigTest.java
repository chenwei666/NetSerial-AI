package com.chenwei666.netserial.remote;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class RemoteConnectionConfigTest {
    @Test
    public void acceptsValidatedSshConfiguration() {
        RemoteConnectionConfig config = new RemoteConnectionConfig(
                RemoteProtocol.SSH, "192.0.2.10", 22, "operator", 10_000, "UTF-8");
        assertEquals("192.0.2.10", config.getHost());
        assertEquals("operator", config.getUsername());
        assertEquals(22, config.getPort());
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsUrlInsteadOfHost() {
        new RemoteConnectionConfig(RemoteProtocol.SSH, "ssh://example.test", 22,
                "operator", 10_000, "UTF-8");
    }

    @Test(expected = IllegalArgumentException.class)
    public void requiresSshUsername() {
        new RemoteConnectionConfig(RemoteProtocol.SSH, "example.test", 22,
                "", 10_000, "UTF-8");
    }

    @Test
    public void permitsEmptyTelnetUsernameBecauseLoginIsInteractive() {
        RemoteConnectionConfig config = new RemoteConnectionConfig(RemoteProtocol.TELNET,
                "example.test", 23, "", 10_000, "GBK");
        assertEquals("", config.getUsername());
    }
}
