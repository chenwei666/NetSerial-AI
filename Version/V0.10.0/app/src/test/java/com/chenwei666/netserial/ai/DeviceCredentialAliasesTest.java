package com.chenwei666.netserial.ai;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class DeviceCredentialAliasesTest {
    @Test public void namespacesValidAlias() {
        assertEquals("device:core-01/ssh/admin", DeviceCredentialAliases.vaultKey(" core-01/ssh/admin "));
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsInjectionCharacters() {
        DeviceCredentialAliases.vaultKey("core-01\npassword=plain");
    }
}
