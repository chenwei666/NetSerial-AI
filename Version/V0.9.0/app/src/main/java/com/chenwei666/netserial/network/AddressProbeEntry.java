package com.chenwei666.netserial.network;

import java.util.Objects;

public final class AddressProbeEntry {
    private final String address;
    private final String family;
    private final boolean loopback;
    private final boolean linkLocal;
    private final boolean privateAddress;
    private final boolean multicast;
    private final boolean anyLocal;

    public AddressProbeEntry(String address, String family, boolean loopback,
                             boolean linkLocal, boolean privateAddress,
                             boolean multicast, boolean anyLocal) {
        this.address = Objects.requireNonNull(address, "address");
        this.family = Objects.requireNonNull(family, "family");
        this.loopback = loopback;
        this.linkLocal = linkLocal;
        this.privateAddress = privateAddress;
        this.multicast = multicast;
        this.anyLocal = anyLocal;
    }

    public String getAddress() { return address; }
    public String getFamily() { return family; }
    public boolean isLoopback() { return loopback; }
    public boolean isLinkLocal() { return linkLocal; }
    public boolean isPrivateAddress() { return privateAddress; }
    public boolean isMulticast() { return multicast; }
    public boolean isAnyLocal() { return anyLocal; }
}
