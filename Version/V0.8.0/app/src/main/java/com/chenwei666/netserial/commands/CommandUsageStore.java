package com.chenwei666.netserial.commands;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/** Stores only identifiers from the built-in command catalog; terminal input is never captured. */
public final class CommandUsageStore {
    private static final String NAME = "command_usage_v1";
    private static final Type STRING_LIST = new TypeToken<List<String>>() { }.getType();
    private final SharedPreferences preferences;
    private final Gson gson = new Gson();

    public CommandUsageStore(Context context) {
        preferences = Objects.requireNonNull(context, "context").getApplicationContext()
                .getSharedPreferences(NAME, Context.MODE_PRIVATE);
    }

    public CommandUsageHistory load() {
        try {
            Set<String> favorites = preferences.getStringSet("favorites", new HashSet<>());
            String rawRecent = preferences.getString("recent", "[]");
            List<String> recent = gson.fromJson(rawRecent, STRING_LIST);
            return new CommandUsageHistory(favorites, recent == null ? new ArrayList<>() : recent);
        } catch (RuntimeException exception) {
            return CommandUsageHistory.empty();
        }
    }

    public void save(CommandUsageHistory history) {
        Objects.requireNonNull(history, "history");
        boolean saved = preferences.edit()
                .putStringSet("favorites", new HashSet<>(history.getFavorites()))
                .putString("recent", gson.toJson(history.getRecent()))
                .commit();
        if (!saved) throw new IllegalStateException("Unable to persist command usage");
    }
}
