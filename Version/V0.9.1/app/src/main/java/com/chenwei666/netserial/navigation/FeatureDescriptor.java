package com.chenwei666.netserial.navigation;

import java.util.Locale;

public final class FeatureDescriptor {
    private final FeatureId id;
    private final FeatureCategory category;
    private final int titleResource;
    private final int summaryResource;
    private final String keywords;

    public FeatureDescriptor(FeatureId id, FeatureCategory category, int titleResource,
                             int summaryResource, String keywords) {
        if (id == null || category == null || titleResource == 0 || summaryResource == 0) {
            throw new IllegalArgumentException("Feature metadata is incomplete");
        }
        this.id = id;
        this.category = category;
        this.titleResource = titleResource;
        this.summaryResource = summaryResource;
        this.keywords = normalize(keywords);
    }

    public FeatureId getId() { return id; }
    public FeatureCategory getCategory() { return category; }
    public int getTitleResource() { return titleResource; }
    public int getSummaryResource() { return summaryResource; }

    boolean matches(String query, FeatureTextResolver resolver) {
        String normalized = normalize(query);
        return normalized.isEmpty()
                || normalize(resolver.resolve(titleResource)).contains(normalized)
                || normalize(resolver.resolve(summaryResource)).contains(normalized)
                || keywords.contains(normalized);
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }
}
