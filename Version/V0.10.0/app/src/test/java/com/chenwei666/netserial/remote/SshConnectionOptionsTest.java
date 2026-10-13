package com.chenwei666.netserial.remote;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import java.util.Arrays;

public class SshConnectionOptionsTest {
    @Test public void privateKeyIsDefensivelyCopied() {
        byte[] key = new byte[]{1, 2, 3};
        SshConnectionOptions options = new SshConnectionOptions(SshAuthenticationMode.PRIVATE_KEY,
                key, new JumpHostConfig("bastion.example.com", 22, "operator"), 30_000);
        Arrays.fill(key, (byte) 0);
        assertEquals(1, options.copyPrivateKey()[0]);
    }

    @Test(expected = IllegalArgumentException.class)
    public void passwordModeRejectsPrivateKeyMaterial() {
        new SshConnectionOptions(SshAuthenticationMode.PASSWORD, new byte[]{1}, null, 30_000);
    }
}
