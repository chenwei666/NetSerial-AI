package com.chenwei666.netserial.guided.network;

import org.junit.Test;

import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.*;

public class BoundedDnsResolverTest {
    @Test public void repeatedTimeoutsCannotCreateUnlimitedStuckDnsWorkers() throws Exception {
        ThreadPoolExecutor pool = BoundedDnsResolver.workers();
        CountDownLatch release = new CountDownLatch(1);
        BoundedDnsResolver resolver = new BoundedDnsResolver(pool, host -> {
            // Deliberately ignores interruption, like some native DNS resolver implementations.
            while (release.getCount() != 0) {
                try { release.await(); } catch (InterruptedException ignored) { }
            }
            return new InetAddress[0];
        });
        try {
            long started = System.nanoTime();
            for (int attempt = 0; attempt < 2; attempt++) {
                try { resolver.resolve("example.test", 50); fail(); }
                catch (SocketTimeoutException expected) { }
            }
            for (int attempt = 0; attempt < 20; attempt++) {
                try { resolver.resolve("example.test", 50); fail(); }
                catch (IOException expected) { assertTrue(expected.getMessage().contains("busy")); }
            }
            assertEquals(2, pool.getLargestPoolSize());
            assertEquals(0, pool.getQueue().size());
            assertTrue(TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - started) < 1500);
        } finally {
            release.countDown();
            pool.shutdownNow();
            assertTrue(pool.awaitTermination(2, TimeUnit.SECONDS));
        }
    }

    @Test public void interruptionReturnsWithoutWaitingForNativeResolver() throws Exception {
        ThreadPoolExecutor pool = BoundedDnsResolver.workers();
        CountDownLatch started = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        AtomicBoolean interrupted = new AtomicBoolean();
        BoundedDnsResolver resolver = new BoundedDnsResolver(pool, host -> {
            started.countDown();
            while (release.getCount() != 0) {
                try { release.await(); } catch (InterruptedException ignored) { }
            }
            return new InetAddress[0];
        });
        Thread worker = new Thread(() -> {
            try { resolver.resolve("example.test", 3000); }
            catch (InterruptedException expected) { interrupted.set(true); }
            catch (IOException unexpected) { throw new AssertionError(unexpected); }
        });
        try {
            worker.start();
            assertTrue(started.await(2, TimeUnit.SECONDS));
            worker.interrupt();
            worker.join(1000);
            assertFalse(worker.isAlive());
            assertTrue(interrupted.get());
        } finally {
            release.countDown();
            pool.shutdownNow();
            assertTrue(pool.awaitTermination(2, TimeUnit.SECONDS));
        }
    }

    @Test public void returnsAnswersWithoutExternalNetwork() throws Exception {
        ThreadPoolExecutor pool = BoundedDnsResolver.workers();
        BoundedDnsResolver resolver = new BoundedDnsResolver(pool, host ->
                new InetAddress[] {InetAddress.getByAddress(new byte[] {(byte) 192, 0, 2, 1})});
        try {
            assertEquals("192.0.2.1", resolver.resolve("example.test", 1000)[0].getHostAddress());
        } finally {
            pool.shutdownNow();
            assertTrue(pool.awaitTermination(2, TimeUnit.SECONDS));
        }
    }
}
