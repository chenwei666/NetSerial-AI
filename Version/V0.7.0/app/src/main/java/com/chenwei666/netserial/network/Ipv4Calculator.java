package com.chenwei666.netserial.network;

public final class Ipv4Calculator {
    public Ipv4Network calculate(String cidr) {
        if (cidr == null) throw new IllegalArgumentException("CIDR is required");
        String[] parts = cidr.trim().split("/", -1);
        if (parts.length != 2) throw new IllegalArgumentException("CIDR must contain a prefix");
        int prefix;
        try { prefix = Integer.parseInt(parts[1]); }
        catch (NumberFormatException exception) { throw new IllegalArgumentException("invalid prefix", exception); }
        if (prefix < 0 || prefix > 32) throw new IllegalArgumentException("prefix must be 0 to 32");
        long address = parse(parts[0]);
        long mask = prefix == 0 ? 0 : (0xffffffffL << (32 - prefix)) & 0xffffffffL;
        long network = address & mask;
        long broadcast = network | (~mask & 0xffffffffL);
        long total = 1L << (32 - prefix);
        long first = prefix >= 31 ? network : network + 1;
        long last = prefix >= 31 ? broadcast : broadcast - 1;
        return new Ipv4Network(format(network), format(broadcast), format(first), format(last),
                format(mask), total);
    }

    private static long parse(String value) {
        String[] octets = value.split("\\.", -1);
        if (octets.length != 4) throw new IllegalArgumentException("invalid IPv4 address");
        long result = 0;
        for (String octet : octets) {
            if (octet.isEmpty() || (octet.length() > 1 && octet.startsWith("0"))) {
                throw new IllegalArgumentException("invalid IPv4 address");
            }
            int parsed;
            try { parsed = Integer.parseInt(octet); }
            catch (NumberFormatException exception) { throw new IllegalArgumentException("invalid IPv4 address", exception); }
            if (parsed < 0 || parsed > 255) throw new IllegalArgumentException("invalid IPv4 address");
            result = (result << 8) | parsed;
        }
        return result;
    }

    private static String format(long value) {
        return ((value >>> 24) & 255) + "." + ((value >>> 16) & 255) + "."
                + ((value >>> 8) & 255) + "." + (value & 255);
    }
}
