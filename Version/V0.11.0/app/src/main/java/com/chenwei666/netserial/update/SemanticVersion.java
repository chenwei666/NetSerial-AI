package com.chenwei666.netserial.update;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public final class SemanticVersion implements Comparable<SemanticVersion> {
    private final List<Integer> parts;

    private SemanticVersion(List<Integer> parts) { this.parts = parts; }

    public static SemanticVersion parse(String value) {
        String normalized = Objects.requireNonNull(value, "value").trim().toLowerCase(Locale.ROOT);
        if (normalized.startsWith("v")) normalized = normalized.substring(1);
        int suffix = normalized.indexOf('-');
        if (suffix >= 0) normalized = normalized.substring(0, suffix);
        String[] tokens = normalized.split("\\.");
        if (tokens.length < 2 || tokens.length > 4) throw new IllegalArgumentException("invalid version");
        List<Integer> result = new ArrayList<>();
        for (String token : tokens) {
            if (!token.matches("\\d{1,6}")) throw new IllegalArgumentException("invalid version");
            result.add(Integer.parseInt(token));
        }
        return new SemanticVersion(result);
    }

    @Override public int compareTo(SemanticVersion other) {
        int max = Math.max(parts.size(), other.parts.size());
        for (int index = 0; index < max; index++) {
            int left = index < parts.size() ? parts.get(index) : 0;
            int right = index < other.parts.size() ? other.parts.get(index) : 0;
            if (left != right) return left < right ? -1 : 1;
        }
        return 0;
    }
}
