package com.chenwei666.netserial.navigation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;

/** Owns bounded favorites/recent history and a versioned, fail-closed document format. */
public final class FeatureUsageManager {
    private static final int MAX_FAVORITES = 4;
    private static final int MAX_RECENT = 6;
    private final FeatureUsagePersistence persistence;

    public FeatureUsageManager(FeatureUsagePersistence persistence) {
        if (persistence == null) throw new IllegalArgumentException("Persistence required");
        this.persistence = persistence;
    }

    public synchronized List<FeatureId> favorites() { return state().favorites; }
    public synchronized List<FeatureId> recent() { return state().recent; }

    public synchronized boolean toggleFavorite(FeatureId id) {
        State state = state();
        List<FeatureId> favorites = new ArrayList<>(state.favorites);
        boolean added;
        if (favorites.remove(id)) {
            added = false;
        } else {
            while (favorites.size() >= MAX_FAVORITES) favorites.remove(0);
            favorites.add(id);
            added = true;
        }
        save(favorites, state.recent);
        return added;
    }

    public synchronized void recordOpen(FeatureId id) {
        State state = state();
        List<FeatureId> recent = new ArrayList<>(state.recent);
        recent.remove(id);
        recent.add(0, id);
        while (recent.size() > MAX_RECENT) recent.remove(recent.size() - 1);
        save(state.favorites, recent);
    }

    private State state() {
        String document = persistence.read();
        List<FeatureId> favorites = new ArrayList<>();
        List<FeatureId> recent = new ArrayList<>();
        if (document != null) {
            for (String line : document.split("\\n")) {
                if (line.startsWith("favorites=")) parse(line.substring(10), favorites, MAX_FAVORITES);
                if (line.startsWith("recent=")) parse(line.substring(7), recent, MAX_RECENT);
            }
        }
        return new State(favorites, recent);
    }

    private void save(List<FeatureId> favorites, List<FeatureId> recent) {
        persistence.write("version=1\nfavorites=" + join(favorites) + "\nrecent=" + join(recent));
    }

    private static void parse(String value, List<FeatureId> target, int max) {
        LinkedHashSet<FeatureId> unique = new LinkedHashSet<>();
        for (String token : value.split(",")) {
            if (token.trim().isEmpty()) continue;
            try { unique.add(FeatureId.valueOf(token.trim())); } catch (IllegalArgumentException ignored) { }
            if (unique.size() >= max) break;
        }
        target.addAll(unique);
    }

    private static String join(List<FeatureId> values) {
        StringBuilder result = new StringBuilder();
        for (FeatureId value : values) {
            if (result.length() > 0) result.append(',');
            result.append(value.name());
        }
        return result.toString();
    }

    private static final class State {
        final List<FeatureId> favorites;
        final List<FeatureId> recent;
        State(List<FeatureId> favorites, List<FeatureId> recent) {
            this.favorites = Collections.unmodifiableList(new ArrayList<>(favorites));
            this.recent = Collections.unmodifiableList(new ArrayList<>(recent));
        }
    }
}
