package com.chenwei666.netserial.commands;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public final class CommandUsageHistory {
    public static final int MAX_RECENT = 30;
    public static final int MAX_FAVORITES = 200;
    private static final int MAX_ID_LENGTH = 512;

    private final LinkedHashSet<String> favorites;
    private final ArrayList<String> recent;

    public CommandUsageHistory(Collection<String> favorites, Collection<String> recent) {
        this.favorites = validatedSet(favorites, MAX_FAVORITES);
        this.recent = validatedRecent(recent);
    }

    public static CommandUsageHistory empty() {
        return new CommandUsageHistory(Collections.emptyList(), Collections.emptyList());
    }

    public boolean toggleFavorite(String commandId) {
        String id = validateId(commandId);
        if (favorites.remove(id)) return false;
        if (favorites.size() >= MAX_FAVORITES) throw new IllegalStateException("favorite limit reached");
        favorites.add(id);
        return true;
    }

    public void recordUse(String commandId) {
        String id = validateId(commandId);
        recent.remove(id);
        recent.add(0, id);
        while (recent.size() > MAX_RECENT) recent.remove(recent.size() - 1);
    }

    public void clearRecent() { recent.clear(); }
    public boolean isFavorite(String commandId) { return favorites.contains(commandId); }
    public boolean isRecent(String commandId) { return recent.contains(commandId); }
    public int recentRank(String commandId) { return recent.indexOf(commandId); }
    public Set<String> getFavorites() { return Collections.unmodifiableSet(favorites); }
    public List<String> getRecent() { return Collections.unmodifiableList(recent); }

    public static String idOf(CommonCommand command) {
        Objects.requireNonNull(command, "command");
        return command.getVendor().name() + "|" + command.getMode().name() + "|" + command.getCommand();
    }

    private static LinkedHashSet<String> validatedSet(Collection<String> values, int maximum) {
        if (values == null || values.size() > maximum) return new LinkedHashSet<>();
        LinkedHashSet<String> result = new LinkedHashSet<>();
        try {
            for (String value : values) result.add(validateId(value));
        } catch (RuntimeException exception) {
            return new LinkedHashSet<>();
        }
        return result;
    }

    private static ArrayList<String> validatedRecent(Collection<String> values) {
        LinkedHashSet<String> deduplicated = validatedSet(values, MAX_RECENT);
        return new ArrayList<>(deduplicated);
    }

    private static String validateId(String value) {
        String normalized = Objects.requireNonNull(value, "commandId").trim();
        if (normalized.isEmpty() || normalized.length() > MAX_ID_LENGTH) {
            throw new IllegalArgumentException("invalid command id");
        }
        return normalized;
    }
}
