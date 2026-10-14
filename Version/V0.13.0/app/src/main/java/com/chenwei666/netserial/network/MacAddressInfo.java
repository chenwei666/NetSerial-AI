package com.chenwei666.netserial.network;

public final class MacAddressInfo {
    private final String normalized;
    private final String oui;
    private final String vendor;
    private final boolean locallyAdministered;
    private final boolean multicast;

    MacAddressInfo(String normalized, String oui, String vendor,
                   boolean locallyAdministered, boolean multicast) {
        this.normalized = normalized;
        this.oui = oui;
        this.vendor = vendor;
        this.locallyAdministered = locallyAdministered;
        this.multicast = multicast;
    }

    public String getNormalized() { return normalized; }
    public String getOui() { return oui; }
    public String getVendor() { return vendor; }
    public boolean isLocallyAdministered() { return locallyAdministered; }
    public boolean isMulticast() { return multicast; }
}
