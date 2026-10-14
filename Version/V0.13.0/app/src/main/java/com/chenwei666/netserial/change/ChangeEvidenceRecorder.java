package com.chenwei666.netserial.change;

import android.content.Context;

import com.chenwei666.netserial.terminal.AnsiTextSanitizer;
import com.chenwei666.netserial.terminal.SensitiveTextRedactor;

public final class ChangeEvidenceRecorder {
    private final ChangeTaskStore store;
    private final SensitiveTextRedactor redactor = new SensitiveTextRedactor();
    private final AnsiTextSanitizer ansi = new AnsiTextSanitizer();

    public ChangeEvidenceRecorder(Context context) {
        store = new ChangeTaskStore(context);
    }

    public void record(ChangeEventType type, String target, String detail) {
        ChangeTask task = store.loadActive();
        if (task == null || task.getStatus() != ChangeTaskStatus.ACTIVE) return;
        if (!matchesDevice(target, task.getDeviceName())) return;
        String safe = redactor.redact(ansi.sanitize(detail == null ? "" : detail));
        String safeTarget = redactor.redact(ansi.sanitize(target == null ? "" : target));
        if (safe.length() > 8_000) safe = safe.substring(safe.length() - 8_000);
        try {
            store.save(task.append(new ChangeEvent(System.currentTimeMillis(), type,
                    safeTarget, safe)));
        } catch (RuntimeException ignored) {
            // Evidence recording must never interrupt a live terminal session.
        }
    }

    private static boolean matchesDevice(String target, String deviceName) {
        if (target == null || deviceName == null || deviceName.trim().isEmpty()) return false;
        if (target.trim().equalsIgnoreCase(deviceName.trim())) return true;
        String[] tokens = target.split("·", -1);
        for (String token : tokens) {
            if (token.trim().equalsIgnoreCase(deviceName.trim())) return true;
        }
        return false;
    }
}
