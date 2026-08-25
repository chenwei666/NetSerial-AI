package com.chenwei666.netserial.memory;

import com.chenwei666.netserial.terminal.SensitiveTextRedactor;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/** Provider-neutral memory with explicit user writes, scope checks, expiry, and secret rejection. */
public final class MemoryVault {
    public interface Persistence {
        String read();
        void write(String document);
    }

    private static final int MAX_RECORDS = 500;
    private static final Type LIST_TYPE = new TypeToken<List<MemoryRecord>>() { }.getType();
    private final Persistence persistence;
    private final Gson gson = new Gson();
    private final SensitiveTextRedactor redactor = new SensitiveTextRedactor();

    public MemoryVault(Persistence persistence) {
        this.persistence = Objects.requireNonNull(persistence, "persistence");
    }

    public synchronized List<MemoryRecord> list(long now) {
        List<MemoryRecord> records = decode();
        boolean changed = false;
        for (int index = records.size() - 1; index >= 0; index--) {
            if (records.get(index).isExpired(now)) {
                records.remove(index);
                changed = true;
            }
        }
        if (changed) persist(records);
        Collections.sort(records, new Comparator<MemoryRecord>() {
            @Override public int compare(MemoryRecord first, MemoryRecord second) {
                return Long.compare(second.getCreatedAt(), first.getCreatedAt());
            }
        });
        return Collections.unmodifiableList(records);
    }

    public synchronized void add(MemoryRecord record) {
        Objects.requireNonNull(record, "record");
        if (redactor.containsSensitiveMaterial(record.getContent())) {
            throw new IllegalArgumentException("sensitive material cannot be stored in AI memory");
        }
        List<MemoryRecord> records = decode();
        if (records.size() >= MAX_RECORDS) {
            throw new IllegalStateException("AI memory limit reached");
        }
        records.add(record);
        persist(records);
    }

    public synchronized void delete(String id) {
        List<MemoryRecord> records = decode();
        for (int index = records.size() - 1; index >= 0; index--) {
            if (records.get(index).getId().equals(id)) records.remove(index);
        }
        persist(records);
    }

    public synchronized List<MemoryRecord> recall(MemoryScope scope, String subjectId,
                                                   int limit, long now) {
        if (limit < 1 || limit > 20) throw new IllegalArgumentException("invalid recall limit");
        List<MemoryRecord> matches = new ArrayList<>();
        for (MemoryRecord record : list(now)) {
            boolean inScope = record.getScope() == MemoryScope.GLOBAL
                    || (record.getScope() == scope && record.getSubjectId().equals(subjectId));
            if (inScope) matches.add(record);
            if (matches.size() == limit) break;
        }
        return Collections.unmodifiableList(matches);
    }

    public synchronized String exportRedacted(long now) {
        return gson.toJson(list(now));
    }

    public synchronized void replaceAll(List<MemoryRecord> replacements, long now) {
        persist(validateForImport(replacements, now));
    }

    public synchronized List<MemoryRecord> validateForImport(List<MemoryRecord> replacements,
                                                              long now) {
        Objects.requireNonNull(replacements, "replacements");
        if (replacements.size() > MAX_RECORDS) throw new IllegalArgumentException("too many records");
        List<MemoryRecord> validated = new ArrayList<>();
        for (MemoryRecord record : replacements) {
            MemoryRecord safe = new MemoryRecord(record.getId(), record.getScope(),
                    record.getSubjectId(), record.getContent(), record.getSource(),
                    record.getTrust(), record.getCreatedAt(), record.getExpiresAt());
            if (!safe.isExpired(now)) {
                if (redactor.containsSensitiveMaterial(safe.getContent())) {
                    throw new IllegalArgumentException("sensitive material cannot be imported");
                }
                validated.add(safe);
            }
        }
        return Collections.unmodifiableList(validated);
    }

    private List<MemoryRecord> decode() {
        String document = persistence.read();
        if (document == null || document.trim().isEmpty()) return new ArrayList<>();
        try {
            List<MemoryRecord> records = gson.fromJson(document, LIST_TYPE);
            if (records == null) return new ArrayList<>();
            List<MemoryRecord> validated = new ArrayList<>();
            for (MemoryRecord record : records) {
                validated.add(new MemoryRecord(record.getId(), record.getScope(),
                        record.getSubjectId(), record.getContent(), record.getSource(),
                        record.getTrust(), record.getCreatedAt(), record.getExpiresAt()));
            }
            return validated;
        } catch (RuntimeException exception) {
            throw new IllegalStateException("AI memory document is invalid", exception);
        }
    }

    private void persist(List<MemoryRecord> records) {
        persistence.write(gson.toJson(records));
    }
}
