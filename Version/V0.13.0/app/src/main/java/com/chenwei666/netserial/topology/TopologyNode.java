package com.chenwei666.netserial.topology;

import java.util.Objects;

public final class TopologyNode {
    private final String id;
    private final String label;

    public TopologyNode(String id, String label) {
        this.id = require(id, 128);
        this.label = require(label, 128);
    }
    public String getId() { return id; }
    public String getLabel() { return label; }

    private static String require(String value, int max) {
        String result = Objects.requireNonNull(value, "value").trim();
        if (result.isEmpty() || result.length() > max) throw new IllegalArgumentException("Invalid node");
        return result;
    }
}
