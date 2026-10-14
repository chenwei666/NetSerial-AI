package com.chenwei666.netserial.remote;

import java.util.Arrays;

public final class SshCredentials implements AutoCloseable {
    private final char[] secret;
    private final char[] jumpPassword;

    public SshCredentials(char[] secret, char[] jumpPassword) {
        this.secret = secret == null ? new char[0] : secret.clone();
        this.jumpPassword = jumpPassword == null ? new char[0] : jumpPassword.clone();
    }

    public char[] copySecret() { return secret.clone(); }
    public char[] copyJumpPassword() { return jumpPassword.clone(); }

    @Override public void close() {
        Arrays.fill(secret, '\0');
        Arrays.fill(jumpPassword, '\0');
    }
}
