package com.chenwei666.netserial.update;

import java.util.Objects;

public final class ReleaseInfo {
    private final String tagName;
    private final String releaseName;
    private final String releaseUrl;
    private final String apkUrl;
    private final boolean prerelease;

    public ReleaseInfo(String tagName, String releaseName, String releaseUrl, String apkUrl,
                       boolean prerelease) {
        this.tagName = requireText(tagName, "tagName");
        this.releaseName = releaseName == null ? "" : releaseName.trim();
        this.releaseUrl = requireHttps(releaseUrl, "releaseUrl");
        this.apkUrl = apkUrl == null || apkUrl.trim().isEmpty() ? "" : requireHttps(apkUrl, "apkUrl");
        this.prerelease = prerelease;
    }

    public String getTagName() { return tagName; }
    public String getReleaseName() { return releaseName; }
    public String getReleaseUrl() { return releaseUrl; }
    public String getApkUrl() { return apkUrl; }
    public boolean isPrerelease() { return prerelease; }
    public boolean isNewerThan(String currentVersion) {
        return !prerelease && SemanticVersion.parse(tagName).compareTo(SemanticVersion.parse(currentVersion)) > 0;
    }

    private static String requireText(String value, String field) {
        String normalized = Objects.requireNonNull(value, field).trim();
        if (normalized.isEmpty() || normalized.length() > 256) throw new IllegalArgumentException(field);
        return normalized;
    }

    private static String requireHttps(String value, String field) {
        String normalized = requireText(value, field);
        if (!normalized.startsWith("https://github.com/")) throw new IllegalArgumentException(field);
        return normalized;
    }
}
