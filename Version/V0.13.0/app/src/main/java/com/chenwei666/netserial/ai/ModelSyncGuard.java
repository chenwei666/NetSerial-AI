package com.chenwei666.netserial.ai;

/** Main-thread generation guard that prevents stale model-catalog callbacks updating a new profile. */
public final class ModelSyncGuard {
    private long generation;

    public long begin() {
        return ++generation;
    }

    public void invalidate() {
        generation++;
    }

    public boolean isCurrent(long candidate) {
        return candidate == generation;
    }
}
