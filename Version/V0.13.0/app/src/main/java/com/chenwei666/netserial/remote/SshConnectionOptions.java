package com.chenwei666.netserial.remote;

import java.util.Arrays;
import java.util.Objects;

public final class SshConnectionOptions {
    private final SshAuthenticationMode authenticationMode;
    private final byte[] privateKey;
    private final JumpHostConfig jumpHost;
    private final int keepAliveMillis;

    public SshConnectionOptions(SshAuthenticationMode authenticationMode, byte[] privateKey,
                                JumpHostConfig jumpHost, int keepAliveMillis) {
        this.authenticationMode = Objects.requireNonNull(authenticationMode, "authenticationMode");
        this.privateKey = privateKey == null ? new byte[0] : Arrays.copyOf(privateKey, privateKey.length);
        if (authenticationMode == SshAuthenticationMode.PRIVATE_KEY
                && (this.privateKey.length == 0 || this.privateKey.length > 256_000)) {
            throw new IllegalArgumentException("private key must contain 1 to 256000 bytes");
        }
        if (authenticationMode == SshAuthenticationMode.PASSWORD && this.privateKey.length != 0) {
            throw new IllegalArgumentException("password mode must not contain a private key");
        }
        if (keepAliveMillis < 0 || keepAliveMillis > 300_000) {
            throw new IllegalArgumentException("invalid keepalive interval");
        }
        this.jumpHost = jumpHost;
        this.keepAliveMillis = keepAliveMillis;
    }

    public static SshConnectionOptions passwordOnly() {
        return new SshConnectionOptions(SshAuthenticationMode.PASSWORD, null, null, 30_000);
    }

    public SshAuthenticationMode getAuthenticationMode() { return authenticationMode; }
    public byte[] copyPrivateKey() { return Arrays.copyOf(privateKey, privateKey.length); }
    void clearPrivateKey() { Arrays.fill(privateKey, (byte) 0); }
    public JumpHostConfig getJumpHost() { return jumpHost; }
    public int getKeepAliveMillis() { return keepAliveMillis; }
}
