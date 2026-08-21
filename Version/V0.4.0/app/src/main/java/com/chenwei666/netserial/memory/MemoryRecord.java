package com.chenwei666.netserial.memory;

import java.util.Objects;
import java.util.UUID;

public final class MemoryRecord {
    private final String id;
    private final MemoryScope scope;
    private final String subjectId;
    private final String content;
    private final String source;
    private final MemoryTrust trust;
    private final long createdAt;
    private final long expiresAt;

    public MemoryRecord(String id, MemoryScope scope, String subjectId, String content,
                        String source, MemoryTrust trust, long createdAt, long expiresAt) {
        this.id = requireText(id, "id", 64);
        this.scope = Objects.requireNonNull(scope, "scope");
        this.subjectId = requireText(subjectId, "subjectId", 128);
        this.content = requireText(content, "content", 1024);
        this.source = requireText(source, "source", 128);
        this.trust = Objects.requireNonNull(trust, "trust");
        if (createdAt <= 0 || (expiresAt != 0 && expiresAt <= createdAt)) {
            throw new IllegalArgumentException("invalid memory timestamps");
        }
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
    }

    public static MemoryRecord userVerified(MemoryScope scope, String subjectId,
                                             String content, long now, long expiresAt) {
        return new MemoryRecord(UUID.randomUUID().toString(), scope, subjectId, content,
                "user", MemoryTrust.USER_VERIFIED, now, expiresAt);
    }

    public String getId() { return id; }
    public MemoryScope getScope() { return scope; }
    public String getSubjectId() { return subjectId; }
    public String getContent() { return content; }
    public String getSource() { return source; }
    public MemoryTrust getTrust() { return trust; }
    public long getCreatedAt() { return createdAt; }
    public long getExpiresAt() { return expiresAt; }
    public boolean isExpired(long now) { return expiresAt != 0 && expiresAt <= now; }

    private static String requireText(String value, String field, int limit) {
        String normalized = Objects.requireNonNull(value, field).trim();
        if (normalized.isEmpty() || normalized.length() > limit) {
            throw new IllegalArgumentException(field + " is empty or too long");
        }
        return normalized;
    }
}
