package com.chenwei666.netserial.terminal;

import java.util.Objects;

/** Bounded terminal text buffer that prevents an unbounded TextView from exhausting memory. */
public final class TerminalTextBuffer {
    private final int maximumCharacters;
    private final StringBuilder content = new StringBuilder();

    public TerminalTextBuffer(int maximumCharacters) {
        if (maximumCharacters < 1024) {
            throw new IllegalArgumentException("maximumCharacters must be at least 1024");
        }
        this.maximumCharacters = maximumCharacters;
    }

    public synchronized String append(CharSequence value) {
        content.append(Objects.requireNonNull(value, "value"));
        int overflow = content.length() - maximumCharacters;
        if (overflow > 0) {
            int boundary = content.indexOf("\n", overflow);
            content.delete(0, boundary >= 0 ? boundary + 1 : overflow);
        }
        return content.toString();
    }

    public synchronized String snapshot() {
        return content.toString();
    }

    public synchronized String tail(int maximumLength) {
        if (maximumLength < 0) {
            throw new IllegalArgumentException("maximumLength must not be negative");
        }
        int start = Math.max(0, content.length() - maximumLength);
        return content.substring(start);
    }

    public synchronized void clear() {
        content.setLength(0);
    }
}
