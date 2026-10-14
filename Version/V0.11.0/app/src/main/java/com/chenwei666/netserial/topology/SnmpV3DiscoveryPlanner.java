package com.chenwei666.netserial.topology;

import com.chenwei666.netserial.network.Ipv4Calculator;
import com.chenwei666.netserial.network.Ipv4Network;

import java.util.Arrays;

/** Builds a bounded SNMPv3 inventory plan; credentials remain in the encrypted vault. */
public final class SnmpV3DiscoveryPlanner {
    public SnmpV3QueryPlan plan(String cidr, String securityName, String authenticationProtocol,
                                String privacyProtocol) {
        Ipv4Network network = new Ipv4Calculator().calculate(cidr);
        if (network.getTotalAddresses() > 256) throw new IllegalArgumentException("Discovery scope exceeds 256 addresses");
        String user = token(securityName, 32, "security name");
        String auth = protocol(authenticationProtocol, new String[]{"SHA", "SHA-224", "SHA-256", "SHA-384", "SHA-512"});
        String privacy = protocol(privacyProtocol, new String[]{"AES-128", "AES-192", "AES-256"});
        return new SnmpV3QueryPlan(cidr.trim(), user, auth, privacy, Arrays.asList(
                "1.3.6.1.2.1.1.1.0", "1.3.6.1.2.1.1.5.0", "1.3.6.1.2.1.2.2.1.2",
                "1.0.8802.1.1.2.1.4.1.1.9", "1.0.8802.1.1.2.1.4.1.1.7"));
    }

    private static String token(String value, int max, String field) {
        String result = value == null ? "" : value.trim();
        if (result.isEmpty() || result.length() > max || !result.matches("[A-Za-z0-9._-]+")) {
            throw new IllegalArgumentException("Invalid " + field);
        }
        return result;
    }

    private static String protocol(String value, String[] allowed) {
        String result = value == null ? "" : value.trim().toUpperCase(java.util.Locale.ROOT);
        for (String item : allowed) if (item.equals(result)) return result;
        throw new IllegalArgumentException("Unsupported SNMPv3 protocol");
    }
}
