package com.chenwei666.netserial.guided.network;

import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/** A stuck platform resolver must not block the UI worker or spawn unlimited DNS threads. */
final class BoundedDnsResolver {
    interface Lookup { InetAddress[] lookup(String host) throws IOException; }

    // Shared across screens/runners: cancellation cannot reliably interrupt native DNS resolution.
    private static final ThreadPoolExecutor SHARED_WORKERS = workers();
    private final ThreadPoolExecutor executor;
    private final Lookup lookup;

    BoundedDnsResolver() { this(SHARED_WORKERS, InetAddress::getAllByName); }

    BoundedDnsResolver(ThreadPoolExecutor executor, Lookup lookup) {
        this.executor = executor;
        this.lookup = lookup;
    }

    static ThreadPoolExecutor workers() {
        return new ThreadPoolExecutor(0, 2, 30, TimeUnit.SECONDS,
                new SynchronousQueue<>(), task -> {
                    Thread thread = new Thread(task, "guided-dns");
                    thread.setDaemon(true);
                    return thread;
                }, new ThreadPoolExecutor.AbortPolicy());
    }

    InetAddress[] resolve(String host, int timeoutMillis) throws IOException, InterruptedException {
        if (Thread.currentThread().isInterrupted()) throw new InterruptedException("cancelled");
        Future<InetAddress[]> pending;
        try {
            pending = executor.submit(() -> lookup.lookup(host));
        } catch (RejectedExecutionException busy) {
            throw new IOException("DNS resolver is busy; retry after existing lookups finish", busy);
        }
        try {
            return pending.get(timeoutMillis, TimeUnit.MILLISECONDS);
        } catch (TimeoutException timeout) {
            throw new SocketTimeoutException("DNS lookup timed out");
        } catch (ExecutionException failure) {
            Throwable cause = failure.getCause();
            if (cause instanceof IOException) throw (IOException) cause;
            throw new IOException("DNS lookup unavailable", cause);
        } finally {
            pending.cancel(true);
        }
    }
}
