package com.chenwei666.netserial.config;

import java.util.Locale;

/** Classifies normalized baseline drift without treating the heuristic as proof of impact. */
public final class ConfigurationDriftEngine {
    public ConfigDriftAssessment assess(String baseline, String current) {
        ConfigNormalizer normalizer = new ConfigNormalizer();
        ConfigDiff diff = new ConfigDiffEngine().compare(normalizer.normalize(baseline == null ? "" : baseline),
                normalizer.normalize(current == null ? "" : current));
        int total = diff.getAddedLines().size() + diff.getRemovedLines().size();
        int sensitive = 0;
        for (String line : diff.getAddedLines()) if (sensitive(line)) sensitive++;
        for (String line : diff.getRemovedLines()) if (sensitive(line)) sensitive++;
        ConfigDriftSeverity severity = total == 0 ? ConfigDriftSeverity.NONE
                : sensitive > 0 || total > 100 ? ConfigDriftSeverity.HIGH
                : total > 20 ? ConfigDriftSeverity.MEDIUM : ConfigDriftSeverity.LOW;
        return new ConfigDriftAssessment(diff, severity, sensitive, total);
    }

    private static boolean sensitive(String line) {
        String value = line.toLowerCase(Locale.ROOT);
        return contains(value, "aaa", "local-user", "username", "password", "secret", "ssh",
                "stelnet", "telnet", "snmp", "community", "http", "route", "stp", "spanning-tree",
                "vrrp", "acl", "access-list", "shutdown", "vlan");
    }

    private static boolean contains(String value, String... terms) {
        for (String term : terms) if (value.contains(term)) return true;
        return false;
    }
}
