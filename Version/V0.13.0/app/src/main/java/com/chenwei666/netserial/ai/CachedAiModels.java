package com.chenwei666.netserial.ai;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class CachedAiModels {
    private final List<String> models;
    private final long fetchedAtMillis;

    public CachedAiModels(List<String> models, long fetchedAtMillis) {
        this.models = Collections.unmodifiableList(new ArrayList<>(models));
        this.fetchedAtMillis = fetchedAtMillis;
    }

    public List<String> getModels() { return models; }
    public long getFetchedAtMillis() { return fetchedAtMillis; }
    public boolean isEmpty() { return models.isEmpty(); }
}
