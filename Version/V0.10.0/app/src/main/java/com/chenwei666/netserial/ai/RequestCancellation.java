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
