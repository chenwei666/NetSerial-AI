package com.chenwei666.netserial.network;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class AddressProbeResult {
    private final String host;
    private final List<AddressProbeEntry> addresses;

    public AddressProbeResult(String host, List<AddressProbeEntry> addresses) {
        this.host = Objects.requireNonNull(host, "host");
        this.addresses = Collections.unmodifiableList(new ArrayList<>(
                Objects.requireNonNull(addresses, "addresses")));
    }

    public String getHost() { return host; }
    public List<AddressProbeEntry> getAddresses() { return addresses; }
}
