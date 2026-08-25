package com.chenwei666.netserial.completion;

import java.util.Objects;

public final class CompletionSuggestion {
    private final String insertion;
    private final CompletionSource source;

    public CompletionSuggestion(String insertion, CompletionSource source) {
        this.insertion = Objects.requireNonNull(insertion, "insertion");
        this.source = Objects.requireNonNull(source, "source");
    }

    public String getInsertion() {
        return insertion;
    }

    public CompletionSource getSource() {
        return source;
    }
}
