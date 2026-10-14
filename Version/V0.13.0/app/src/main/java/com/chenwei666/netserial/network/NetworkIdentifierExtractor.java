package com.chenwei666.netserial.network;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class NetworkIdentifierExtractor {
    private static final Pattern IPV4 = Pattern.compile("(?<![0-9.])(?:[0-9]{1,3}\\.){3}[0-9]{1,3}(?:/[0-9]+)?(?![0-9./])");
    private static final Pattern IPV6 = Pattern.compile("(?i)(?<![0-9a-f:])[0-9a-f]*:[0-9a-f:.]+(?:/[0-9]+)?(?![0-9a-f:/])");
    private static final Pattern MAC = Pattern.compile("(?i)(?<![0-9a-f])(?:[0-9a-f]{2}[:-]){5}[0-9a-f]{2}|(?<![0-9a-f])(?:[0-9a-f]{4}\\.){2}[0-9a-f]{4}(?![0-9a-f])");
    private static final Pattern INTERFACE = Pattern.compile("(?i)\\b(?:GE|XGE|Eth|Ethernet|GigabitEthernet|Ten-GigabitEthernet|FortyGigE|HundredGigE|Vlan-interface|Vlanif|Port-Channel|Bridge-Aggregation)[0-9/.-]+\\b");

    public String extract(String text) {
        String source = text == null ? "" : text;
        Set<String> values = new LinkedHashSet<>();
        Matcher v4 = IPV4.matcher(source);
        while (v4.find() && values.size() < 200) {
            String candidate = v4.group();
            try {
                new Ipv4Calculator().calculate(candidate.contains("/") ? candidate : candidate + "/32");
                values.add(candidate);
            } catch (IllegalArgumentException ignored) { }
        }
        Matcher v6 = IPV6.matcher(source);
        while (v6.find() && values.size() < 200) {
            String candidate = v6.group();
            try {
                String[] parts = candidate.split("/", -1);
                if (parts.length > 2 || (parts.length == 2 && Integer.parseInt(parts[1]) > 128)) continue;
                if (java.net.InetAddress.getByName(parts[0]) instanceof java.net.Inet6Address) values.add(candidate);
            } catch (Exception ignored) { }
        }
        collect(MAC, source, values);
        collect(INTERFACE, source, values);
        return String.join("\n", values);
    }

    private static void collect(Pattern pattern, String source, Set<String> values) {
        Matcher matcher = pattern.matcher(source);
        while (matcher.find() && values.size() < 200) values.add(matcher.group());
    }
}
