package com.chenwei666.netserial.terminal;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Process-local bridge from existing terminals to read-only tools. It never
 * stores data on disk and never exposes a command execution callback.
 */
public final class ActiveTerminalSessionStore {
    private static final int DEFAULT_MAXIMUM_SESSIONS = 4;
    private static final int DEFAULT_MAXIMUM_CHARACTERS = 100_000;
    private static final ActiveTerminalSessionStore SHARED =
            new ActiveTerminalSessionStore(DEFAULT_MAXIMUM_SESSIONS,
                    DEFAULT_MAXIMUM_CHARACTERS);

    private final int maximumSessions;
    private final int maximumCharacters;
    private final Map<String, SessionRecord> sessions = new LinkedHashMap<>();
    private final AnsiTextSanitizer ansi = new AnsiTextSanitizer();
    private final SensitiveTextRedactor redactor = new SensitiveTextRedactor();
    private long sequence;

    public ActiveTerminalSessionStore(int maximumSessions, int maximumCharacters) {
        if (maximumSessions < 1 || maximumSessions > 16) {
            throw new IllegalArgumentException("maximumSessions must be between 1 and 16");
        }
        if (maximumCharacters < 1024 || maximumCharacters > 500_000) {
            throw new IllegalArgumentException(
                    "maximumCharacters must be between 1024 and 500000");
        }
        this.maximumSessions = maximumSessions;
        this.maximumCharacters = maximumCharacters;
    }

    public static ActiveTerminalSessionStore shared() {
        return SHARED;
    }

    public synchronized void connected(String sessionId, TerminalSessionTransport transport,
                                       String targetLabel, String initialText, long nowMillis) {
        validateSessionId(sessionId);
        Objects.requireNonNull(transport, "transport");
        SessionRecord record = new SessionRecord(transport, sanitizeLabel(targetLabel),
                maximumCharacters);
        record.connected = true;
        record.updatedAtMillis = validTime(nowMillis);
        record.sequence = ++sequence;
        record.buffer.append(sanitizeText(initialText));
        sessions.remove(sessionId);
        sessions.put(sessionId, record);
        trimSessions();
    }

    public synchronized void append(String sessionId, CharSequence text, long nowMillis) {
        SessionRecord record = sessions.get(sessionId);
        if (record == null || !record.connected || text == null || text.length() == 0) return;
        record.buffer.append(sanitizeText(text.toString()));
        record.updatedAtMillis = validTime(nowMillis);
        record.sequence = ++sequence;
    }

    public synchronized void clear(String sessionId, long nowMillis) {
        SessionRecord record = sessions.get(sessionId);
        if (record == null || !record.connected) return;
        record.buffer.clear();
        record.updatedAtMillis = validTime(nowMillis);
        record.sequence = ++sequence;
    }

    public synchronized void disconnected(String sessionId, long nowMillis) {
        SessionRecord record = sessions.get(sessionId);
        if (record == null) return;
        record.connected = false;
        record.updatedAtMillis = validTime(nowMillis);
        record.sequence = ++sequence;
    }

    public synchronized ActiveTerminalSnapshot latestConnected() {
        String selectedId = null;
        SessionRecord selected = null;
        for (Map.Entry<String, SessionRecord> entry : sessions.entrySet()) {
            SessionRecord candidate = entry.getValue();
            if (!candidate.connected) continue;
            if (selected == null || candidate.sequence > selected.sequence) {
                selectedId = entry.getKey();
                selected = candidate;
            }
        }
        if (selected == null) return null;
        return new ActiveTerminalSnapshot(selectedId, selected.transport, selected.targetLabel,
                selected.buffer.snapshot(), selected.updatedAtMillis);
    }

    private void trimSessions() {
        while (sessions.size() > maximumSessions) {
            String oldestId = null;
            long oldestSequence = Long.MAX_VALUE;
            for (Map.Entry<String, SessionRecord> entry : sessions.entrySet()) {
                if (entry.getValue().sequence < oldestSequence) {
                    oldestId = entry.getKey();
                    oldestSequence = entry.getValue().sequence;
                }
            }
            if (oldestId == null) return;
            sessions.remove(oldestId);
        }
    }

    private String sanitizeText(String value) {
        return redactor.redact(ansi.sanitize(value == null ? "" : value));
    }

    private String sanitizeLabel(String value) {
        String label = sanitizeText(value).replace('\r', ' ').replace('\n', ' ').trim();
        if (label.isEmpty()) label = "Connected terminal";
        return label.length() <= 256 ? label : label.substring(0, 256);
    }

    private static void validateSessionId(String value) {
        if (value == null || value.trim().isEmpty() || value.length() > 128) {
            throw new IllegalArgumentException("invalid sessionId");
        }
    }

    private static long validTime(long value) {
        if (value < 0) throw new IllegalArgumentException("nowMillis must not be negative");
        return value;
    }

    private static final class SessionRecord {
        private final TerminalSessionTransport transport;
        private final String targetLabel;
        private final TerminalTextBuffer buffer;
        private boolean connected;
        private long updatedAtMillis;
        private long sequence;

        private SessionRecord(TerminalSessionTransport transport, String targetLabel,
                              int maximumCharacters) {
            this.transport = transport;
            this.targetLabel = targetLabel;
            this.buffer = new TerminalTextBuffer(maximumCharacters);
        }
    }
}
