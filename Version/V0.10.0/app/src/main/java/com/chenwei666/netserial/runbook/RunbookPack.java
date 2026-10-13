package com.chenwei666.netserial.runbook;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class RunbookPack {
    private final String id;
    private final String version;
    private final String author;
    private final List<String> commands;

    public RunbookPack(String id, String version, String author, List<String> commands) {
        this.id = token(id, 64);
        this.version = token(version, 32);
        this.author = text(author, 128);
        if (commands == null || commands.isEmpty() || commands.size() > 500) throw new IllegalArgumentException("Invalid commands");
        List<String> safe = new ArrayList<>();
        for (String command : commands) {
            String value = text(command, 512);
            if (containsCredential(value)) throw new IllegalArgumentException("Runbook contains a credential-like command");
            safe.add(value);
        }
        this.commands = Collections.unmodifiableList(safe);
    }
    public String getId() { return id; }
    public String getVersion() { return version; }
    public String getAuthor() { return author; }
    public List<String> getCommands() { return commands; }

    private static boolean containsCredential(String value) {
        return value.toLowerCase(java.util.Locale.ROOT)
                .matches(".*\\b(password|secret|community|api[-_ ]?key)\\s+\\S+.*");
    }
    private static String token(String value, int max) {
        String result = text(value, max);
        if (!result.matches("[A-Za-z0-9._-]+")) throw new IllegalArgumentException("Invalid token");
        return result;
    }
    private static String text(String value, int max) {
        String result = value == null ? "" : value.trim();
        if (result.isEmpty() || result.length() > max || result.contains("\u0000")) throw new IllegalArgumentException("Invalid text");
        return result;
    }
}
