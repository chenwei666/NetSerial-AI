package com.chenwei666.netserial.config;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;
import java.util.Objects;

public final class ConfigSnapshot {
    private final String label;
    private final long capturedAtMillis;
    private final String normalizedText;
    private final String sha256;

    public ConfigSnapshot(String label, long capturedAtMillis, String normalizedText) {
        this.label = Objects.requireNonNull(label, "label").trim();
        if (this.label.isEmpty() || this.label.length() > 128) throw new IllegalArgumentException("invalid label");
        if (capturedAtMillis <= 0) throw new IllegalArgumentException("invalid timestamp");
        this.capturedAtMillis = capturedAtMillis;
        this.normalizedText = Objects.requireNonNull(normalizedText, "normalizedText");
        if (normalizedText.length() > 1_000_000) throw new IllegalArgumentException("configuration is too large");
        this.sha256 = sha256(normalizedText);
    }

    public String getLabel() { return label; }
    public long getCapturedAtMillis() { return capturedAtMillis; }
    public String getNormalizedText() { return normalizedText; }
    public String getSha256() { return sha256; }

    private static String sha256(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte item : digest) hex.append(String.format(Locale.ROOT, "%02x", item & 0xff));
            return hex.toString();
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 unavailable", exception);
        }
    }
}
