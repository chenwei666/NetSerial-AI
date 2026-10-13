package com.chenwei666.netserial.network;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class NetworkIdentifierExtractor {
    private static final Pattern IPV4 = Pattern.compile("(?<![0-9])(?:[0-9]{1,3}\\.){3}[0-9]{1,3}(?:/[0-9]{1,2})?(?![0-9])");
    private static final Pattern MAC = Pattern.compile("(?i)(?<![0-9a-f])(?:[0-9a-f]{2}[:-]){5}[0-9a-f]{2}|(?<![0-9a-f])(?:[0-9a-f]{4}\\.){2}[0-9a-f]{4}(?![0-9a-f])");
    private static final Pattern INTERFACE = Pattern.compile("(?i)\\b(?:GE|XGE|Eth|Ethernet|GigabitEthernet|Ten-GigabitEthernet|FortyGigE|HundredGigE|Vlan-interface|Vlanif|Port-Channel|Bridge-Aggregation)[0-9/.-]+\\b");

    public String extract(String text) {
        String source = text == null ? "" : text;
        Set<String> values = new LinkedHashSet<>();
        collect(IPV4, source, values);
        collect(MAC, source, values);
        collect(INTERFACE, source, values);
        return String.join("\n", values);
    }

    private static void collect(Pattern pattern, String source, Set<String> values) {
        Matcher matcher = pattern.matcher(source);
        while (matcher.find() && values.size() < 200) values.add(matcher.group());
    }
}
