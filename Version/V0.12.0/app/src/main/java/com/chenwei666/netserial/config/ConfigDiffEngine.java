package com.chenwei666.netserial.config;

import java.util.ArrayList;
import java.util.List;

public final class ConfigDiffEngine {
    private static final long MAX_LCS_CELLS = 1_000_000L;

    public ConfigDiff compare(String before, String after) {
        List<String> left = lines(before);
        List<String> right = lines(after);
        if ((long) left.size() * (long) right.size() <= MAX_LCS_CELLS) {
            return orderedLcsDiff(left, right);
        }
        return boundedGreedyDiff(left, right);
    }

    private static ConfigDiff orderedLcsDiff(List<String> left, List<String> right) {
        int[][] lengths = new int[left.size() + 1][right.size() + 1];
        for (int i = left.size() - 1; i >= 0; i--) {
            for (int j = right.size() - 1; j >= 0; j--) {
                lengths[i][j] = left.get(i).equals(right.get(j))
                        ? lengths[i + 1][j + 1]
                        : Math.max(lengths[i + 1][j], lengths[i][j + 1]);
            }
        }
        List<String> removed = new ArrayList<>();
        List<String> added = new ArrayList<>();
        int i = 0;
        int j = 0;
        while (i < left.size() && j < right.size()) {
            if (left.get(i).equals(right.get(j))) {
                i++;
                j++;
            } else if (lengths[i + 1][j] >= lengths[i][j + 1]) {
                removed.add(left.get(i++));
            } else {
                added.add(right.get(j++));
            }
        }
        while (i < left.size()) removed.add(left.get(i++));
        while (j < right.size()) added.add(right.get(j++));
        return new ConfigDiff(added, removed);
    }

    private static ConfigDiff boundedGreedyDiff(List<String> left, List<String> right) {
        final int lookAhead = 200;
        List<String> removed = new ArrayList<>();
        List<String> added = new ArrayList<>();
        int i = 0;
        int j = 0;
        while (i < left.size() && j < right.size()) {
            if (left.get(i).equals(right.get(j))) {
                i++;
                j++;
                continue;
            }
            int nextLeft = find(left, i + 1, right.get(j), lookAhead);
            int nextRight = find(right, j + 1, left.get(i), lookAhead);
            if (nextLeft >= 0 && (nextRight < 0 || nextLeft - i <= nextRight - j)) {
                while (i < nextLeft) removed.add(left.get(i++));
            } else if (nextRight >= 0) {
                while (j < nextRight) added.add(right.get(j++));
            } else {
                removed.add(left.get(i++));
                added.add(right.get(j++));
            }
        }
        while (i < left.size()) removed.add(left.get(i++));
        while (j < right.size()) added.add(right.get(j++));
        return new ConfigDiff(added, removed);
    }

    private static int find(List<String> lines, int start, String expected, int limit) {
        int end = Math.min(lines.size(), start + limit);
        for (int index = start; index < end; index++) {
            if (lines.get(index).equals(expected)) return index;
        }
        return -1;
    }

    private static List<String> lines(String value) {
        String normalized = value == null ? "" : value;
        List<String> result = new ArrayList<>();
        if (normalized.isEmpty()) return result;
        java.util.Collections.addAll(result, normalized.split("\n", -1));
        return result;
    }
}
