package com.chenwei666.netserial.topology;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Parses common detailed LLDP/CDP text without executing discovery commands. */
public final class TopologyParser {
    private static final Pattern REMOTE = Pattern.compile("(?im)^(?:System Name|Device ID|SysName)\\s*[:=]\\s*(\\S.+?)\\s*$");
    private static final Pattern LOCAL_PORT = Pattern.compile("(?im)^(?:Local (?:Port id|Interface)|Local Intf|Interface)\\s*[:=]\\s*([^,\\s]+)");
    private static final Pattern REMOTE_PORT = Pattern.compile("(?im)^(?:Port ID(?: \\(outgoing port\\))?|Port id)\\s*[:=]\\s*(\\S.+?)\\s*$");

    public TopologyGraph parse(String localNode, String capture) {
        String local = safe(localNode, "local");
        String text = capture == null ? "" : capture;
        if (text.length() > 500_000) text = text.substring(text.length() - 500_000);
        Map<String, TopologyNode> nodes = new LinkedHashMap<>();
        nodes.put(local, new TopologyNode(local, local));
        List<TopologyLink> links = new ArrayList<>();
        for (String block : text.split("(?:\\r?\\n){2,}")) {
            String remote = match(REMOTE, block);
            String localPort = match(LOCAL_PORT, block);
            String remotePort = match(REMOTE_PORT, block);
            if (remote == null || localPort == null || remotePort == null) continue;
            remote = safe(remote, "neighbor");
            nodes.put(remote, new TopologyNode(remote, remote));
            TopologyLink candidate = new TopologyLink(local, safe(localPort, "unknown"), remote,
                    safe(remotePort, "unknown"));
            if (!contains(links, candidate)) links.add(candidate);
        }
        return new TopologyGraph(new ArrayList<>(nodes.values()), links);
    }

    private static String match(Pattern pattern, String value) {
        Matcher matcher = pattern.matcher(value);
        return matcher.find() ? matcher.group(1).trim() : null;
    }

    private static boolean contains(List<TopologyLink> values, TopologyLink candidate) {
        for (TopologyLink value : values) {
            if (value.getLocalNode().equals(candidate.getLocalNode())
                    && value.getLocalPort().equals(candidate.getLocalPort())
                    && value.getRemoteNode().equals(candidate.getRemoteNode())
                    && value.getRemotePort().equals(candidate.getRemotePort())) return true;
        }
        return false;
    }

    private static String safe(String value, String fallback) {
        String result = value == null ? "" : value.trim().replaceAll("[\\r\\n\\t]", " ");
        if (result.isEmpty()) result = fallback;
        return result.length() > 128 ? result.substring(0, 128) : result;
    }
}
