package com.chenwei666.netserial.network;

public final class Ipv4Network {
    private final String network;
    private final String broadcast;
    private final String firstUsable;
    private final String lastUsable;
    private final String netmask;
    private final long totalAddresses;

    Ipv4Network(String network, String broadcast, String firstUsable, String lastUs,
                String netmask, long totalAddresses) {
        this.network = network;
        this.broadcast = broadcast;
        this.firstUsable = firstUsable;
        this.lastUsable = lastUs;
        this.netmask = netmask;
        this.totalAddresses = totalAddresses;
    }

    public String getNetwork() { return network; }
    public String getBroadcast() { return broadcast; }
    public String getFirstUsable() { return firstUsable; }
    public String getLastUsable() { return lastUsable; }
    public String getNetmask() { return netmask; }
    public long getTotalAddresses() { return totalAddresses; }
}
