package com.chenwei666.netserial.ai;

public final class RequestCancellation {
    private boolean cancelled;
    private Runnable cancelAction;

    public void cancel() {
        Runnable action;
        synchronized (this) {
            if (cancelled) {
                return;
            }
            cancelled = true;
            action = cancelAction;
        }
        if (action != null) {
            action.run();
        }
    }

    public synchronized boolean isCancelled() {
        return cancelled;
    }

    /** Mark cancellation immediately; potentially blocking transport cleanup runs off the UI thread. */
    public void cancelAsync(java.util.concurrent.Executor executor) {
        Runnable action;
        synchronized (this) {
            if (cancelled) return;
            cancelled = true;
            action = cancelAction;
        }
        if (action != null) executor.execute(action);
    }

    void setCancelAction(Runnable action) {
        boolean runImmediately;
        synchronized (this) {
            cancelAction = action;
            runImmediately = cancelled && cancelAction != null;
        }
        if (runImmediately) {
            action.run();
        }
    }

    synchronized void clearCancelAction() {
        cancelAction = null;
    }
}
