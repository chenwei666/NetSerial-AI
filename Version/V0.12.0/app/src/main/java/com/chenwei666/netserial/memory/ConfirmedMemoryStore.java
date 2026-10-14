package com.chenwei666.netserial.memory;

import com.chenwei666.netserial.terminal.SensitiveTextRedactor;
import com.google.gson.Gson;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

/** Separate schema: legacy/imported/automatically-created memories are never consulted. */
public final class ConfirmedMemoryStore {
    public static final int MAX_RECORDS = 100;
    public static final int MAX_CONTENT = 1024;
    public static final int MAX_TOTAL_CONTENT = 32768;
    public static final long LIFETIME_MS = 180L * 24 * 60 * 60 * 1000;
    private static final Object LOCK = new Object(); // Serializes every instance in the app process.
    private static final String SOURCE = "user-confirmed-v1";
    private static final Pattern EXTRA_SECRETS = Pattern.compile(
            "(?i)(?:\\bsk-[a-z0-9_-]{8,}|\\b(?:ghp|github_pat)_[a-z0-9_]+|"
            + "-----BEGIN[^\\r\\n]*PRIVATE KEY|://[^\\s/:]+:[^\\s/@]+@|"
            + "(?:密码|口令|密钥|令牌)\\s*(?:是|为|[:：=])\\s*\\S+|"
            + "\\b(?:password|passwd|api[_ -]?key|secret|token)\\s+\\S+)");
    private final MemoryVault.Persistence persistence;
    private final Gson gson = new Gson();
    private final SensitiveTextRedactor redactor = new SensitiveTextRedactor();

    public ConfirmedMemoryStore(MemoryVault.Persistence persistence) {
        if (persistence == null) throw new NullPointerException("persistence");
        this.persistence = persistence;
    }

    public List<MemoryRecord> list(String subject, long now) {
        synchronized (LOCK) {
            List<MemoryRecord> visible = new ArrayList<>();
            for (MemoryRecord record : read()) {
                if (!record.isExpired(now) && matches(record, subject)) visible.add(record);
            }
            Collections.sort(visible, (a, b) -> Long.compare(b.getCreatedAt(), a.getCreatedAt()));
            return Collections.unmodifiableList(visible);
        }
    }

    public List<MemoryRecord> recall(String subject, int limit, long now) {
        if (limit < 1 || limit > 10) throw new IllegalArgumentException("invalid recall limit");
        List<MemoryRecord> visible = list(subject, now);
        return Collections.unmodifiableList(new ArrayList<>(visible.subList(0, Math.min(limit, visible.size()))));
    }

    /** An update is a single replacement write; failed validation never deletes the old record. */
    public void save(String id, MemoryScope scope, String subject, String content, long now) {
        if (scope != MemoryScope.GLOBAL && scope != MemoryScope.DEVICE) throw new IllegalArgumentException("scope");
        if (now <= 0 || now > Long.MAX_VALUE - LIFETIME_MS) throw new IllegalArgumentException("time");
        String normalized = content == null ? "" : content.trim();
        if (normalized.isEmpty() || normalized.length() > MAX_CONTENT || sensitive(normalized))
            throw new IllegalArgumentException("unsafe or oversized memory");
        if (scope == MemoryScope.DEVICE && (subject == null || subject.trim().isEmpty()))
            throw new IllegalArgumentException("device required");
        MemoryRecord replacement = new MemoryRecord(id == null ? UUID.randomUUID().toString() : id,
                scope, scope == MemoryScope.GLOBAL ? "all" : subject, normalized, SOURCE,
                MemoryTrust.USER_VERIFIED, now, now + LIFETIME_MS);
        synchronized (LOCK) {
            List<MemoryRecord> records = read();
            boolean found = id == null;
            for (int i = records.size() - 1; i >= 0; i--) {
                MemoryRecord existing = records.get(i);
                if (existing.getId().equals(id)) {
                    if (!matches(existing, subject)) throw new IllegalArgumentException("wrong device");
                    records.remove(i);
                    found = true;
                } else if (existing.isExpired(now)) records.remove(i);
            }
            if (!found) throw new IllegalStateException("record changed or deleted");
            records.add(replacement);
            validate(records);
            persistence.write(gson.toJson(new Document(records)));
        }
    }

    public void delete(String id, String subject) {
        synchronized (LOCK) {
            List<MemoryRecord> records = read();
            boolean changed = false;
            for (int i = records.size() - 1; i >= 0; i--) {
                MemoryRecord record = records.get(i);
                if (record.getId().equals(id)) {
                    if (!matches(record, subject)) throw new IllegalArgumentException("wrong device");
                    records.remove(i);
                    changed = true;
                }
            }
            if (changed) persistence.write(gson.toJson(new Document(records)));
        }
    }

    private boolean matches(MemoryRecord record, String subject) {
        return record.getScope() == MemoryScope.GLOBAL || record.getSubjectId().equals(subject);
    }

    private boolean sensitive(String content) {
        return redactor.containsSensitiveMaterial(content) || EXTRA_SECRETS.matcher(content).find();
    }

    private List<MemoryRecord> read() {
        String raw = persistence.read();
        if (raw == null) return new ArrayList<>();
        try {
            if (raw.isEmpty() || raw.length() > 262144) throw new IllegalStateException("document size");
            Document document = gson.fromJson(raw, Document.class);
            if (document == null || document.schema != 1 || document.records == null)
                throw new IllegalStateException("document schema");
            validate(document.records);
            return new ArrayList<>(document.records);
        } catch (RuntimeException error) {
            // Fail closed, including on mutation: never replace corrupt storage with an empty list.
            throw new IllegalStateException("Unable to read confirmed memory", error);
        }
    }

    private void validate(List<MemoryRecord> records) {
        if (records.size() > MAX_RECORDS) throw new IllegalArgumentException("memory count limit");
        int total = 0;
        Set<String> ids = new HashSet<>();
        for (MemoryRecord record : records) {
            MemoryRecord checked = new MemoryRecord(record.getId(), record.getScope(), record.getSubjectId(),
                    record.getContent(), record.getSource(), record.getTrust(), record.getCreatedAt(), record.getExpiresAt());
            if ((checked.getScope() != MemoryScope.GLOBAL && checked.getScope() != MemoryScope.DEVICE)
                    || (checked.getScope() == MemoryScope.GLOBAL && !"all".equals(checked.getSubjectId()))
                    || checked.getTrust() != MemoryTrust.USER_VERIFIED || !SOURCE.equals(checked.getSource())
                    || checked.getExpiresAt() <= checked.getCreatedAt()
                    || checked.getExpiresAt() - checked.getCreatedAt() > LIFETIME_MS
                    || !ids.add(checked.getId()) || sensitive(checked.getContent()))
                throw new IllegalArgumentException("unconfirmed or unsafe record");
            total += checked.getContent().length();
        }
        if (total > MAX_TOTAL_CONTENT) throw new IllegalArgumentException("memory size limit");
    }

    private static final class Document {
        int schema = 1;
        List<MemoryRecord> records;
        Document(List<MemoryRecord> records) { this.records = records; }
    }
}
