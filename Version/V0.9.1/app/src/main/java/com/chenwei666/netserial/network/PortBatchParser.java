package com.chenwei666.netserial.network;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/** Parses an explicitly entered, bounded single-target TCP port list. */
public final class PortBatchParser {
    public static final int MAX_PORTS = 16;

    public List<Integer> parse(String value) {
        if (value == null || value.trim().isEmpty() || value.length() > 160) {
            throw new IllegalArgumentException("port list is required");
        }
        Set<Integer> ports = new LinkedHashSet<>();
        for (String token : value.split("[,\\s]+")) {
            if (token.isEmpty()) continue;
            if (token.contains("-")) addRange(token, ports);
            else addPort(token, ports);
            if (ports.size() > MAX_PORTS) {
                throw new IllegalArgumentException("port list exceeds safe limit");
            }
        }
        if (ports.isEmpty()) throw new IllegalArgumentException("port list is required");
        return Collections.unmodifiableList(new ArrayList<>(ports));
    }

    private static void addRange(String token, Set<Integer> ports) {
        String[] bounds = token.split("-", -1);
        if (bounds.length != 2) throw new IllegalArgumentException("invalid port range");
        int first = parsePort(bounds[0]);
        int last = parsePort(bounds[1]);
        if (last < first || last - first + 1 > MAX_PORTS) {
            throw new IllegalArgumentException("invalid port range");
        }
        for (int port = first; port <= last; port++) ports.add(port);
    }

    private static void addPort(String token, Set<Integer> ports) {
        ports.add(parsePort(token));
    }

    private static int parsePort(String value) {
        try {
            int port = Integer.parseInt(value.trim());
            if (port < 1 || port > 65_535) throw new IllegalArgumentException("invalid port");
            return port;
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("invalid port", exception);
        }
    }
}
