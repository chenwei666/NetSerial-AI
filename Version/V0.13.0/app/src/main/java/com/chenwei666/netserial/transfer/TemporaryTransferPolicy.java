package com.chenwei666.netserial.transfer;

import java.net.InetAddress;

public final class TemporaryTransferPolicy {
    private final InetAddress bindAddress;
    private final long lifetimeMillis;
    private final int maxDownloads;

    public TemporaryTransferPolicy(InetAddress bindAddress, long lifetimeMillis, int maxDownloads) {
        if (bindAddress == null || !(bindAddress.isLoopbackAddress() || bindAddress.isSiteLocalAddress())) {
            throw new IllegalArgumentException("Only loopback or private addresses are allowed");
        }
        if (lifetimeMillis < 1_000 || lifetimeMillis > 30 * 60_000L) throw new IllegalArgumentException("Invalid lifetime");
        if (maxDownloads < 1 || maxDownloads > 20) throw new IllegalArgumentException("Invalid download limit");
        this.bindAddress = bindAddress;
        this.lifetimeMillis = lifetimeMillis;
        this.maxDownloads = maxDownloads;
    }
    public InetAddress getBindAddress() { return bindAddress; }
    public long getLifetimeMillis() { return lifetimeMillis; }
    public int getMaxDownloads() { return maxDownloads; }
}
