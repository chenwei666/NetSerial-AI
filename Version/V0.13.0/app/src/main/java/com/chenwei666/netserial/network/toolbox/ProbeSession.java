package com.chenwei666.netserial.network.toolbox;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/** One bounded user-requested job; cancelling closes only resources registered by this job. */
public final class ProbeSession {
    public static final class DeadlineExceeded extends InterruptedException {
        DeadlineExceeded() { super("Probe deadline reached"); }
    }
    public interface Progress { void update(ToolResult result); }
    private final List<Closeable> resources = new ArrayList<>();
    private final long deadline;
    private final Progress progress;
    private volatile boolean cancelled;
    public ProbeSession(long budgetMs, Progress progress) {
        if (budgetMs < 1 || budgetMs > 120000) throw new IllegalArgumentException("Invalid deadline");
        deadline = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(budgetMs);
        this.progress = progress;
    }
    public synchronized <T extends Closeable> T track(T resource) throws InterruptedException {
        if (cancelled || System.nanoTime() >= deadline || Thread.currentThread().isInterrupted()) {
            close(resource); check();
        }
        resources.add(resource); return resource;
    }
    public synchronized void release(Closeable resource) { resources.remove(resource); }
    public void check() throws InterruptedException {
        if (cancelled || Thread.currentThread().isInterrupted()) throw new InterruptedException("Cancelled");
        if (System.nanoTime() >= deadline) throw new DeadlineExceeded();
    }
    public int remainingMs(int requested) throws InterruptedException {
        check();
        return (int) Math.max(1, Math.min(requested, TimeUnit.NANOSECONDS.toMillis(deadline - System.nanoTime())));
    }
    public void publish(ToolResult result) throws InterruptedException { check(); if (progress != null) progress.update(result); }
    public synchronized void cancel() { cancelled = true; for (Closeable item : resources) close(item); resources.clear(); }
    public boolean isCancelled() { return cancelled; }
    private static void close(Closeable resource) { try { resource.close(); } catch (Exception ignored) { } }
}
