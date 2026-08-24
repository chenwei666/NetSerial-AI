package com.chenwei666.netserial.network;

public final class Ipv6Network {
    private final String networkPrefix;
    private final String totalAddresses;

    Ipv6Network(String networkPrefix, String totalAddresses) {
        this.networkPrefix = networkPrefix;
        this.totalAddresses = totalAddresses;
    }

    public String getNetworkPrefix() { return networkPrefix; }
    public String getTotalAddresses() { return totalAddresses; }
}
