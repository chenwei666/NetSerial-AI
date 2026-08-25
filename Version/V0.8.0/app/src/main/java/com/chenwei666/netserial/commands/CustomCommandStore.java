package com.chenwei666.netserial.commands;

import android.content.Context;
import android.content.SharedPreferences;

import com.chenwei666.netserial.device.CliMode;
import com.chenwei666.netserial.device.Vendor;
import com.chenwei666.netserial.safety.RiskLevel;
import com.chenwei666.netserial.terminal.SensitiveTextRedactor;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Stores user-authored command references. Secrets and multi-line scripts are rejected. */
public final class CustomCommandStore {
    private static final String PREFS = "custom_command_pack_v1";
    private static final String KEY = "commands";
    private static final int MAX_COMMANDS = 200;
    private static final Type LIST_TYPE = new TypeToken<List<Record>>() { }.getType();
    private final SharedPreferences preferences;
    private final Gson gson = new Gson();

    public CustomCommandStore(Context context) {
        preferences = Objects.requireNonNull(context, "context")
                .getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public List<CommonCommand> load() {
        List<CommonCommand> result = new ArrayList<>();
        try {
            List<Record> records = gson.fromJson(preferences.getString(KEY, "[]"), LIST_TYPE);
            if (records == null) return result;
            for (Record record : records) {
                if (result.size() >= MAX_COMMANDS) break;
                result.add(record.toCommand());
            }
        } catch (RuntimeException ignored) {
            result.clear();
        }
        return result;
    }

    public void add(Vendor vendor, String command, String description) {
        String safeCommand = validateCommand(command);
        String safeDescription = validateDescription(description);
        List<CommonCommand> current = load();
        for (CommonCommand existing : current) {
            if (existing.getVendor() == vendor && existing.getCommand().equalsIgnoreCase(safeCommand)) {
                throw new IllegalArgumentException("duplicate command");
            }
        }
        if (current.size() >= MAX_COMMANDS) throw new IllegalStateException("custom command limit");
        current.add(new CommonCommand(vendor, CliMode.USER_VIEW, CommandCategory.TROUBLESHOOTING,
                safeCommand, safeDescription, RiskLevel.R2_CONFIGURATION));
        save(current);
    }

    private void save(List<CommonCommand> commands) {
        List<Record> records = new ArrayList<>();
        for (CommonCommand command : commands) records.add(new Record(command));
        if (!preferences.edit().putString(KEY, gson.toJson(records)).commit()) {
            throw new IllegalStateException("save failed");
        }
    }

    private static String validateCommand(String value) {
        String normalized = value == null ? "" : value.trim();
        if (normalized.isEmpty() || normalized.length() > 256 || normalized.contains("\n")
                || normalized.contains("\r") || containsControl(normalized)) {
            throw new IllegalArgumentException("invalid command");
        }
        if (!new SensitiveTextRedactor().redact(normalized).equals(normalized)) {
            throw new IllegalArgumentException("sensitive command");
        }
        return normalized;
    }

    private static String validateDescription(String value) {
        String normalized = value == null ? "" : value.trim();
        if (normalized.isEmpty() || normalized.length() > 256 || containsControl(normalized)) {
            throw new IllegalArgumentException("invalid description");
        }
        return normalized;
    }

    private static boolean containsControl(String value) {
        for (int index = 0; index < value.length(); index++) {
            if (Character.isISOControl(value.charAt(index))) return true;
        }
        return false;
    }

    private static final class Record {
        String vendor;
        String command;
        String description;

        Record(CommonCommand value) {
            vendor = value.getVendor().name();
            command = value.getCommand();
            description = value.getDescription();
        }

        CommonCommand toCommand() {
            return new CommonCommand(Vendor.valueOf(vendor), CliMode.USER_VIEW,
                    CommandCategory.TROUBLESHOOTING, command, description, RiskLevel.R2_CONFIGURATION);
        }
    }
}
