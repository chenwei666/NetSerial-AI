package com.chenwei666.netserial.transfer;

import java.io.File;
import java.io.FileInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/** Read-only TFTP for small configuration files. Firmware images should use SFTP or HTTP. */
public final class TemporaryTftpReadServer implements AutoCloseable {
    private static final long MAX_FILE_BYTES = 31L * 1024 * 1024;
    private final TemporaryTransferPolicy policy;
    private final File file;
    private final String publishedName;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private volatile boolean closed;
    private DatagramSocket listener;
    private DatagramSocket activeTransferSocket;
    private volatile Thread workerThread;
    private long expiresAt;
    private volatile int downloads;

    public TemporaryTftpReadServer(TemporaryTransferPolicy policy, File file) {
        this.policy = policy;
        if (file == null || !file.isFile() || !file.canRead() || file.length() > MAX_FILE_BYTES) {
            throw new IllegalArgumentException("TFTP file must be readable and no larger than 31 MiB");
        }
        this.file = file;
        this.publishedName = token() + "-" + file.getName().replaceAll("[^A-Za-z0-9._-]", "_");
    }

    public synchronized int start() throws Exception {
        if (listener != null) throw new IllegalStateException("Server already started");
        listener = new DatagramSocket(0, policy.getBindAddress());
        listener.setSoTimeout(1_000);
        expiresAt = System.currentTimeMillis() + policy.getLifetimeMillis();
        executor.submit(this::listen);
        return listener.getLocalPort();
    }

    public String getPublishedName() { return publishedName; }
    public int getDownloads() { return downloads; }

    private void listen() {
        workerThread = Thread.currentThread();
        try {
            byte[] buffer = new byte[1_024];
            while (!closed && System.currentTimeMillis() < expiresAt
                    && downloads < policy.getMaxDownloads()) {
                DatagramPacket request = new DatagramPacket(buffer, buffer.length);
                try {
                    listener.receive(request);
                    if (isValidReadRequest(request)) {
                        transfer(request);
                        downloads++;
                    }
                } catch (SocketTimeoutException ignored) {
                } catch (Exception ignored) {
                    // Invalid or failed requests do not terminate the bounded server.
                }
            }
        } finally {
            synchronized (this) {
                closed = true;
                if (listener != null) listener.close();
                if (activeTransferSocket != null) activeTransferSocket.close();
                executor.shutdown();
                workerThread = null;
            }
        }
    }

    private boolean isValidReadRequest(DatagramPacket packet) {
        byte[] data = packet.getData();
        int length = packet.getLength();
        if (length < 6 || data[0] != 0 || data[1] != 1) return false;
        int end = 2;
        while (end < length && data[end] != 0) end++;
        if (end >= length) return false;
        String filename = new String(data, 2, end - 2, StandardCharsets.US_ASCII);
        int modeStart = end + 1;
        int modeEnd = modeStart;
        while (modeEnd < length && data[modeEnd] != 0) modeEnd++;
        String mode = new String(data, modeStart, Math.max(0, modeEnd - modeStart), StandardCharsets.US_ASCII);
        return publishedName.equals(filename) && "octet".equalsIgnoreCase(mode);
    }

    private void transfer(DatagramPacket request) throws Exception {
        DatagramSocket socket = new DatagramSocket(0, policy.getBindAddress());
        synchronized (this) {
            if (closed) {
                socket.close();
                return;
            }
            activeTransferSocket = socket;
        }
        try (DatagramSocket transferSocket = socket;
             FileInputStream input = new FileInputStream(file)) {
            transferSocket.setSoTimeout(1_500);
            int block = 1;
            byte[] payload = new byte[512];
            int count;
            do {
                count = input.read(payload);
                if (count < 0) count = 0;
                byte[] data = new byte[count + 4];
                data[0] = 0; data[1] = 3; data[2] = (byte) (block >> 8); data[3] = (byte) block;
                System.arraycopy(payload, 0, data, 4, count);
                boolean acknowledged = false;
                for (int retry = 0; retry < 3 && !acknowledged; retry++) {
                    transferSocket.send(new DatagramPacket(data, data.length,
                            request.getAddress(), request.getPort()));
                    try {
                        byte[] ack = new byte[4];
                        DatagramPacket response = new DatagramPacket(ack, ack.length);
                        transferSocket.receive(response);
                        acknowledged = isExpectedAck(request, response, block);
                    } catch (SocketTimeoutException ignored) { }
                }
                if (!acknowledged) return;
                block = (block + 1) & 0xffff;
            } while (count == 512 && !closed);
        } finally {
            synchronized (this) {
                if (activeTransferSocket == socket) activeTransferSocket = null;
            }
        }
    }

    static boolean isExpectedAck(DatagramPacket request, DatagramPacket response, int block) {
        if (request == null || response == null || response.getLength() != 4
                || !request.getAddress().equals(response.getAddress())
                || request.getPort() != response.getPort()) return false;
        byte[] ack = response.getData();
        int offset = response.getOffset();
        return ack[offset] == 0 && ack[offset + 1] == 4
                && (ack[offset + 2] & 0xff) == ((block >> 8) & 0xff)
                && (ack[offset + 3] & 0xff) == (block & 0xff);
    }

    @Override public void close() {
        Thread running;
        synchronized (this) {
            closed = true;
            if (listener != null) listener.close();
            if (activeTransferSocket != null) activeTransferSocket.close();
            executor.shutdownNow();
            running = workerThread;
        }
        if (Thread.currentThread() == running) return;
        try {
            if (!executor.awaitTermination(2, TimeUnit.SECONDS)) {
                throw new IllegalStateException("TFTP server did not stop in time");
            }
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while stopping TFTP server", exception);
        }
    }

    private static String token() {
        byte[] bytes = new byte[8];
        new SecureRandom().nextBytes(bytes);
        StringBuilder result = new StringBuilder();
        for (byte value : bytes) result.append(String.format(Locale.ROOT, "%02x", value & 0xff));
        return result.toString();
    }
}
