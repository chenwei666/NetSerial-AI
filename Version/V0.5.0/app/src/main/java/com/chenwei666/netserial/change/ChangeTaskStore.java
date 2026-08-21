package com.chenwei666.netserial.change;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class ChangeTaskStore {
    private static final String NAME = "change_task_v1";
    private static final String KEY_DOCUMENT = "task_history_document";
    private static final String LEGACY_KEY = "active_document";
    private static final int MAX_TASKS = 20;
    private final SharedPreferences preferences;
    private final Gson gson = new Gson();

    public ChangeTaskStore(Context context) {
        preferences = Objects.requireNonNull(context, "context").getApplicationContext()
                .getSharedPreferences(NAME, Context.MODE_PRIVATE);
    }

    /** Returns the active task, or the most recent closed task for viewing and export. */
    public ChangeTask load() {
        TaskDocument document = loadDocument();
        ChangeTask active = find(document.tasks, document.activeId);
        return active != null ? active : document.tasks.isEmpty()
                ? null : document.tasks.get(document.tasks.size() - 1);
    }

    public ChangeTask loadActive() {
        TaskDocument document = loadDocument();
        ChangeTask task = find(document.tasks, document.activeId);
        return task != null && task.getStatus() == ChangeTaskStatus.ACTIVE ? task : null;
    }

    public List<ChangeTask> list() {
        return Collections.unmodifiableList(new ArrayList<>(loadDocument().tasks));
    }

    public void save(ChangeTask task) {
        Objects.requireNonNull(task, "task");
        TaskDocument current = loadDocument();
        List<ChangeTask> tasks = new ArrayList<>(current.tasks);
        boolean replaced = false;
        for (int index = 0; index < tasks.size(); index++) {
            if (tasks.get(index).getId().equals(task.getId())) {
                tasks.set(index, task);
                replaced = true;
                break;
            }
        }
        if (!replaced) tasks.add(task);
        while (tasks.size() > MAX_TASKS) {
            int remove = tasks.get(0).getStatus() == ChangeTaskStatus.ACTIVE && tasks.size() > 1 ? 1 : 0;
            tasks.remove(remove);
        }
        String activeId = task.getStatus() == ChangeTaskStatus.ACTIVE ? task.getId()
                : task.getId().equals(current.activeId) ? null : current.activeId;
        persist(new TaskDocument(1, activeId, tasks));
    }

    public void clear() {
        if (!preferences.edit().remove(KEY_DOCUMENT).remove(LEGACY_KEY).commit()) {
            throw new IllegalStateException("unable to clear change tasks");
        }
    }

    private TaskDocument loadDocument() {
        String raw = preferences.getString(KEY_DOCUMENT, null);
        if (raw != null && !raw.trim().isEmpty()) {
            try {
                return validate(gson.fromJson(raw, TaskDocument.class));
            } catch (RuntimeException ignored) { }
        }
        String legacy = preferences.getString(LEGACY_KEY, null);
        if (legacy != null && !legacy.trim().isEmpty()) {
            try {
                ChangeTask task = validateTask(gson.fromJson(legacy, ChangeTask.class));
                List<ChangeTask> migrated = new ArrayList<>();
                migrated.add(task);
                TaskDocument document = new TaskDocument(1,
                        task.getStatus() == ChangeTaskStatus.ACTIVE ? task.getId() : null, migrated);
                persist(document);
                return document;
            } catch (RuntimeException ignored) { }
        }
        return new TaskDocument(1, null, new ArrayList<>());
    }

    private TaskDocument validate(TaskDocument document) {
        if (document == null || document.schemaVersion != 1 || document.tasks == null
                || document.tasks.size() > MAX_TASKS) throw new IllegalArgumentException("invalid task document");
        List<ChangeTask> validated = new ArrayList<>();
        for (ChangeTask task : document.tasks) validated.add(validateTask(task));
        return new TaskDocument(1, document.activeId, validated);
    }

    private static ChangeTask validateTask(ChangeTask task) {
        if (task == null) throw new IllegalArgumentException("invalid task");
        return new ChangeTask(task.getId(), task.getTicketNumber(), task.getSite(), task.getDeviceName(),
                task.getOperatorName(), task.getGoal(), task.getPrecheckPlan(), task.getCommandPlan(),
                task.getVerificationPlan(), task.getRollbackPlan(), task.getWindowStartMillis(),
                task.getWindowEndMillis(), task.getStatus(), task.getEvents());
    }

    private static ChangeTask find(List<ChangeTask> tasks, String id) {
        if (id == null) return null;
        for (ChangeTask task : tasks) if (id.equals(task.getId())) return task;
        return null;
    }

    private void persist(TaskDocument document) {
        if (!preferences.edit().putString(KEY_DOCUMENT, gson.toJson(document)).remove(LEGACY_KEY).commit()) {
            throw new IllegalStateException("unable to persist change tasks");
        }
    }

    private static final class TaskDocument {
        private final int schemaVersion;
        private final String activeId;
        private final List<ChangeTask> tasks;
        private TaskDocument(int schemaVersion, String activeId, List<ChangeTask> tasks) {
            this.schemaVersion = schemaVersion;
            this.activeId = activeId;
            this.tasks = new ArrayList<>(tasks);
        }
    }
}
