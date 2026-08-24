package com.chenwei666.netserial.topology;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class TopologyGraph {
    private final List<TopologyNode> nodes;
    private final List<TopologyLink> links;
    TopologyGraph(List<TopologyNode> nodes, List<TopologyLink> links) {
        this.nodes = Collections.unmodifiableList(new ArrayList<>(nodes));
        this.links = Collections.unmodifiableList(new ArrayList<>(links));
    }
    public List<TopologyNode> getNodes() { return nodes; }
    public List<TopologyLink> getLinks() { return links; }
}
