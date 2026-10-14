package com.chenwei666.netserial.memory;

import org.junit.Test;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;
import static org.junit.Assert.*;

public class ConfirmedMemoryStoreTest {
    private static final long NOW = 100000;
    private static final class Storage implements MemoryVault.Persistence {
        String data;
        boolean failWrite;
        int writes;
        @Override public String read() { return data; }
        @Override public void write(String value) {
            if (failWrite) throw new IllegalStateException("disk failure");
            data = value;
            writes++;
        }
    }
    private void rejected(Runnable action) {
        try { action.run(); fail("Expected rejection"); }
        catch (IllegalArgumentException | IllegalStateException expected) { }
    }
    @Test public void onlyGlobalAndExactCurrentDeviceAreRecalled() {
        Storage data = new Storage();
        ConfirmedMemoryStore store = new ConfirmedMemoryStore(data);
        store.save(null, MemoryScope.GLOBAL, "", "Explain in Chinese", NOW);
        store.save(null, MemoryScope.DEVICE, "switch-a", "Port 1 is an uplink", NOW + 1);
        store.save(null, MemoryScope.DEVICE, "switch-b", "Port 2 is an uplink", NOW + 2);
        assertEquals(2, store.recall("switch-a", 10, NOW + 3).size());
        assertEquals(1, store.recall("", 10, NOW + 3).size());
        assertEquals("Port 1 is an uplink", store.recall("switch-a", 10, NOW + 3).get(0).getContent());
    }
    @Test public void readingAndExpiryDoNotWrite() {
        Storage data = new Storage();
        ConfirmedMemoryStore store = new ConfirmedMemoryStore(data);
        assertTrue(store.list("a", NOW).isEmpty());
        assertEquals(0, data.writes);
        store.save(null, MemoryScope.GLOBAL, "", "Simple steps", NOW);
        assertTrue(store.list("a", NOW + ConfirmedMemoryStore.LIFETIME_MS).isEmpty());
        assertEquals(1, data.writes);
    }
    @Test public void returnsImmutableSendTimeSnapshot() {
        ConfirmedMemoryStore store = new ConfirmedMemoryStore(new Storage());
        store.save(null, MemoryScope.GLOBAL, "", "Simple steps", NOW);
        List<MemoryRecord> snapshot = store.recall("a", 10, NOW + 1);
        store.delete(snapshot.get(0).getId(), "a");
        assertEquals(1, snapshot.size());
        assertTrue(store.recall("a", 10, NOW + 2).isEmpty());
        try { snapshot.clear(); fail(); } catch (UnsupportedOperationException expected) { }
    }
    @Test public void noSilentTruncation() {
        ConfirmedMemoryStore store = new ConfirmedMemoryStore(new Storage());
        rejected(() -> store.save(null, MemoryScope.GLOBAL, "", repeat('a', 1025), NOW));
        assertTrue(store.list("a", NOW).isEmpty());
        store.save(null, MemoryScope.GLOBAL, "", repeat('a', 1024), NOW);
        assertEquals(1024, store.list("a", NOW).get(0).getContent().length());
    }
    @Test public void rejectsCommonCredentials() {
        String[] secrets = { "password=secret123", "api_key: example123", "密码是123456", "令牌：abc123",
                "sk-abcdefghijk", "ghp_abcd123", "https://user:pass@example.test", "-----BEGIN RSA PRIVATE KEY-----",
                "password simple abc123", "snmp-agent community read simple public" };
        ConfirmedMemoryStore store = new ConfirmedMemoryStore(new Storage());
        for (String secret : secrets) rejected(() -> store.save(null, MemoryScope.GLOBAL, "", secret, NOW));
        assertTrue(store.list("a", NOW).isEmpty());
    }
    @Test public void failedUpdatePreservesOriginal() {
        Storage data = new Storage();
        ConfirmedMemoryStore store = new ConfirmedMemoryStore(data);
        store.save(null, MemoryScope.DEVICE, "a", "Original", NOW);
        String id = store.list("a", NOW).get(0).getId();
        String original = data.data;
        rejected(() -> store.save(id, MemoryScope.DEVICE, "a", "password=hidden", NOW + 1));
        assertEquals(original, data.data);
        data.failWrite = true;
        rejected(() -> store.save(id, MemoryScope.DEVICE, "a", "Updated", NOW + 1));
        assertEquals(original, data.data);
    }
    @Test public void editingIsSingleWriteAndKeepsId() {
        Storage data = new Storage();
        ConfirmedMemoryStore store = new ConfirmedMemoryStore(data);
        store.save(null, MemoryScope.GLOBAL, "", "Original", NOW);
        String id = store.list("a", NOW).get(0).getId();
        store.save(id, MemoryScope.DEVICE, "a", "Updated", NOW + 1);
        assertEquals(2, data.writes);
        assertEquals(id, store.list("a", NOW + 1).get(0).getId());
        assertTrue(store.list("b", NOW + 1).isEmpty());
    }
    @Test public void forbidsCrossDeviceEditAndDelete() {
        Storage data = new Storage();
        ConfirmedMemoryStore store = new ConfirmedMemoryStore(data);
        store.save(null, MemoryScope.DEVICE, "a", "Original", NOW);
        String id = store.list("a", NOW).get(0).getId();
        rejected(() -> store.save(id, MemoryScope.GLOBAL, "b", "Hijack", NOW + 1));
        rejected(() -> store.delete(id, "b"));
        assertEquals(1, store.list("a", NOW + 1).size());
    }
    @Test public void deletedRecordIsNotResurrectedByStaleEditor() {
        ConfirmedMemoryStore store = new ConfirmedMemoryStore(new Storage());
        store.save(null, MemoryScope.GLOBAL, "", "Original", NOW);
        String id = store.list("a", NOW).get(0).getId();
        store.delete(id, "a");
        rejected(() -> store.save(id, MemoryScope.GLOBAL, "a", "Updated", NOW + 1));
    }
    @Test public void corruptOrLegacyDocumentsAreNeverOverwritten() {
        String[] documents = { "", "null", "{}", "[]", "{broken", "{\"schema\":2,\"records\":[]}",
                "{\"schema\":1,\"records\":[null]}" };
        for (String document : documents) {
            Storage data = new Storage(); data.data = document;
            ConfirmedMemoryStore store = new ConfirmedMemoryStore(data);
            rejected(() -> store.save(null, MemoryScope.GLOBAL, "", "New", NOW));
            rejected(() -> store.delete("a", "a"));
            rejected(() -> store.list("a", NOW));
            assertEquals(document, data.data);
            assertEquals(0, data.writes);
        }
    }
    @Test public void forgedUnconfirmedRecordFailsClosed() {
        Storage data = new Storage();
        ConfirmedMemoryStore store = new ConfirmedMemoryStore(data);
        store.save(null, MemoryScope.GLOBAL, "", "Original", NOW);
        data.data = data.data.replace("user-confirmed-v1", "auto-chat");
        rejected(() -> store.list("a", NOW));
        assertEquals(1, data.writes);
    }
    @Test public void limitsRecordCountAndRecall() {
        ConfirmedMemoryStore store = new ConfirmedMemoryStore(new Storage());
        for (int i = 0; i < 100; i++) store.save(null, MemoryScope.GLOBAL, "", "Note " + i, NOW + i);
        rejected(() -> store.save(null, MemoryScope.GLOBAL, "", "Overflow", NOW + 101));
        assertEquals(10, store.recall("a", 10, NOW + 102).size());
        assertEquals(3, store.recall("a", 3, NOW + 102).size());
        rejected(() -> store.recall("a", 11, NOW));
    }
    @Test public void limitsTotalContent() {
        ConfirmedMemoryStore store = new ConfirmedMemoryStore(new Storage());
        for (int i = 0; i < 32; i++) store.save(null, MemoryScope.GLOBAL, "", repeat('x', 1024), NOW + i);
        rejected(() -> store.save(null, MemoryScope.GLOBAL, "", "Overflow", NOW + 33));
        assertEquals(32, store.list("a", NOW + 33).size());
    }
    @Test public void mutationPrunesExpiredButDoesNotRecallThem() {
        ConfirmedMemoryStore store = new ConfirmedMemoryStore(new Storage());
        store.save(null, MemoryScope.GLOBAL, "", "Old", NOW);
        store.save(null, MemoryScope.GLOBAL, "", "New", NOW + ConfirmedMemoryStore.LIFETIME_MS);
        assertEquals(1, store.list("a", NOW + ConfirmedMemoryStore.LIFETIME_MS).size());
    }
    @Test public void multipleInstancesDoNotLoseConcurrentWrites() throws Exception {
        Storage data = new Storage();
        ConfirmedMemoryStore first = new ConfirmedMemoryStore(data), second = new ConfirmedMemoryStore(data);
        CountDownLatch start = new CountDownLatch(1);
        AtomicReference<Throwable> failure = new AtomicReference<>();
        Thread one = writer(first, "One", start, failure), two = writer(second, "Two", start, failure);
        one.start(); two.start(); start.countDown(); one.join(); two.join();
        if (failure.get() != null) throw new AssertionError(failure.get());
        assertEquals(40, first.list("a", NOW + 100).size());
    }
    private Thread writer(ConfirmedMemoryStore store, String prefix, CountDownLatch start, AtomicReference<Throwable> failure) {
        return new Thread(() -> { try { start.await(); for (int i = 0; i < 20; i++) store.save(null, MemoryScope.GLOBAL, "", prefix + i, NOW + i); }
            catch (Throwable error) { failure.set(error); } });
    }
    @Test public void rejectsUnknownScopeMissingDeviceAndBadClock() {
        ConfirmedMemoryStore store = new ConfirmedMemoryStore(new Storage());
        rejected(() -> store.save(null, MemoryScope.VENDOR, "a", "Note", NOW));
        rejected(() -> store.save(null, MemoryScope.DEVICE, "", "Note", NOW));
        rejected(() -> store.save(null, MemoryScope.GLOBAL, "", "Note", 0));
        rejected(() -> store.save(null, MemoryScope.GLOBAL, "", "Note", Long.MAX_VALUE));
    }
    private String repeat(char value, int count) { char[] chars = new char[count]; java.util.Arrays.fill(chars, value); return new String(chars); }
}
