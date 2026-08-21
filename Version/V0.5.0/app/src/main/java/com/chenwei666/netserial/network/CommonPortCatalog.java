package com.chenwei666.netserial.network;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class CommonPortCatalog {
    public List<PortReference> list() {
        return Collections.unmodifiableList(Arrays.asList(
                new PortReference(22, "TCP", "SSH"), new PortReference(23, "TCP", "Telnet"),
                new PortReference(49, "TCP/UDP", "TACACS+"), new PortReference(53, "TCP/UDP", "DNS"),
                new PortReference(67, "UDP", "DHCP server"), new PortReference(68, "UDP", "DHCP client"),
                new PortReference(80, "TCP", "HTTP"), new PortReference(123, "UDP", "NTP"),
                new PortReference(161, "UDP", "SNMP"), new PortReference(162, "UDP", "SNMP trap"),
                new PortReference(443, "TCP", "HTTPS / RESTCONF"),
                new PortReference(514, "UDP", "Syslog"), new PortReference(6514, "TCP", "Syslog TLS"),
                new PortReference(830, "TCP", "NETCONF over SSH")));
    }
}
