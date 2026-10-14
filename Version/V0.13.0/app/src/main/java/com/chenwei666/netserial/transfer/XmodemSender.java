package com.chenwei666.netserial.transfer;

import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/** XMODEM-128 sender with checksum/CRC negotiation, retries, cancellation, and bounded waits. */
public final class XmodemSender {
    public interface Writer { void write(byte[] value) throws Exception; }
    public interface Listener { void onProgress(int sent, int total); }

    static final int SOH = 0x01;
    static final int EOT = 0x04;
    static final int ACK = 0x06;
    static final int NAK = 0x15;
    static final int CAN = 0x18;
    static final int CRC_REQUEST = 0x43;
    private static final int BLOCK_SIZE = 128;
    private static final int MAX_RETRIES = 10;
    private final BlockingQueue<Integer> responses = new LinkedBlockingQueue<>(256);
    private volatile boolean cancelled;

    public void accept(byte[] incoming) {
        if (incoming == null) return;
        for (byte value : incoming) responses.offer(value & 0xff);
    }

    public void cancel() {
        cancelled = true;
        responses.offer(CAN);
    }

    public void send(byte[] source, Writer writer, Listener listener) throws Exception {
        Objects.requireNonNull(source, "source");
        Objects.requireNonNull(writer, "writer");
        if (source.length == 0 || source.length > 16 * 1024 * 1024) {
            throw new IllegalArgumentException("file size");
        }
        responses.clear();
        cancelled = false;
        int negotiation = await(30);
        boolean crc = negotiation == CRC_REQUEST;
        if (!crc && negotiation != NAK) throw new IllegalStateException("receiver did not start XMODEM");
        int offset = 0;
        int block = 1;
        while (offset < source.length) {
            int length = Math.min(BLOCK_SIZE, source.length - offset);
            byte[] payload = new byte[BLOCK_SIZE];
            Arrays.fill(payload, (byte) 0x1A);
            System.arraycopy(source, offset, payload, 0, length);
            byte[] packet = packet(block, payload, crc);
            boolean acknowledged = false;
            for (int retry = 0; retry < MAX_RETRIES && !acknowledged; retry++) {
                ensureActive();
                writer.write(packet);
                int response = await(10);
                if (response == ACK) acknowledged = true;
                else if (response != NAK && response != CRC_REQUEST) failResponse(response);
            }
            if (!acknowledged) throw new IllegalStateException("XMODEM retry limit");
            offset += length;
            block = (block + 1) & 0xff;
            if (listener != null) listener.onProgress(offset, source.length);
        }
        for (int retry = 0; retry < MAX_RETRIES; retry++) {
            ensureActive();
            writer.write(new byte[]{(byte) EOT});
            int response = await(10);
            if (response == ACK) return;
            if (response != NAK) failResponse(response);
        }
        throw new IllegalStateException("XMODEM EOT not acknowledged");
    }

    static byte[] packet(int block, byte[] payload, boolean crc) {
        if (payload == null || payload.length != BLOCK_SIZE) throw new IllegalArgumentException("payload");
        byte[] result = new byte[3 + BLOCK_SIZE + (crc ? 2 : 1)];
        result[0] = SOH;
        result[1] = (byte) block;
        result[2] = (byte) (0xff - (block & 0xff));
        System.arraycopy(payload, 0, result, 3, BLOCK_SIZE);
        if (crc) {
            int value = crc16(payload);
            result[result.length - 2] = (byte) (value >> 8);
            result[result.length - 1] = (byte) value;
        } else {
            int checksum = 0;
            for (byte value : payload) checksum = (checksum + (value & 0xff)) & 0xff;
            result[result.length - 1] = (byte) checksum;
        }
        return result;
    }

    static int crc16(byte[] bytes) {
        int crc = 0;
        for (byte current : bytes) {
            crc ^= (current & 0xff) << 8;
            for (int bit = 0; bit < 8; bit++) {
                crc = (crc & 0x8000) != 0 ? ((crc << 1) ^ 0x1021) : crc << 1;
                crc &= 0xffff;
            }
        }
        return crc;
    }

    private int await(int seconds) throws Exception {
        ensureActive();
        Integer value = responses.poll(seconds, TimeUnit.SECONDS);
        if (value == null) throw new IllegalStateException("XMODEM timeout");
        if (value == CAN) throw new IllegalStateException("XMODEM cancelled by receiver");
        return value;
    }

    private void ensureActive() {
        if (cancelled || Thread.currentThread().isInterrupted()) throw new IllegalStateException("XMODEM cancelled");
    }

    private static void failResponse(int response) {
        throw new IllegalStateException("unexpected XMODEM response: " + response);
    }
}
