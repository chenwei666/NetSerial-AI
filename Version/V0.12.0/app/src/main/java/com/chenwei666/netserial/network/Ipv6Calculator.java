package com.chenwei666.netserial.network;

import java.math.BigInteger;
import java.net.Inet6Address;
import java.net.InetAddress;

public final class Ipv6Calculator {
    public Ipv6Network calculate(String cidr) {
        if (cidr == null) throw new IllegalArgumentException("CIDR is required");
        String[] parts = cidr.trim().split("/", -1);
        if (parts.length != 2 || !parts[0].contains(":")) throw new IllegalArgumentException("invalid IPv6 CIDR");
        int prefix;
        try { prefix = Integer.parseInt(parts[1]); }
        catch (NumberFormatException exception) { throw new IllegalArgumentException("invalid prefix", exception); }
        if (prefix < 0 || prefix > 128) throw new IllegalArgumentException("prefix must be 0 to 128");
        try {
            InetAddress parsed = InetAddress.getByName(parts[0]);
            if (!(parsed instanceof Inet6Address)) throw new IllegalArgumentException("invalid IPv6 address");
            byte[] network = parsed.getAddress();
            int fullBytes = prefix / 8;
            int remainingBits = prefix % 8;
            if (remainingBits != 0) network[fullBytes] &= (byte) (0xff << (8 - remainingBits));
            int start = remainingBits == 0 ? fullBytes : fullBytes + 1;
            for (int index = start; index < network.length; index++) network[index] = 0;
            String address = InetAddress.getByAddress(network).getHostAddress();
            int zone = address.indexOf('%');
            if (zone >= 0) address = address.substring(0, zone);
            return new Ipv6Network(address + "/" + prefix,
                    BigInteger.ONE.shiftLeft(128 - prefix).toString());
        } catch (IllegalArgumentException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new IllegalArgumentException("invalid IPv6 address", exception);
        }
    }
}
