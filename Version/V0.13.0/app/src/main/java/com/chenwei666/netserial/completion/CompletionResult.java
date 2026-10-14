package com.chenwei666.netserial.completion;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class CompletionResult {
    private final List<CompletionSuggestion> suggestions;

    public CompletionResult(List<CompletionSuggestion> suggestions) {
        this.suggestions = Collections.unmodifiableList(new ArrayList<>(suggestions));
    }

    public List<CompletionSuggestion> getSuggestions() {
        return suggestions;
    }
}
