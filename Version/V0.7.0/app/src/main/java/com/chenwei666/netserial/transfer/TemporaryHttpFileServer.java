package com.chenwei666.netserial.transfer;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** A tokenized, GET-only, single-file HTTP server with bounded lifetime/downloads. */
public final class TemporaryHttpFileServer implements AutoCloseable {
    private static final long MAX_FILE_BYTES = 2L * 1024 * 1024 * 1024;
    private final TemporaryTransferPolicy policy;
    private final File file;
    private final String publishedName;
    private final String token;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private volatile boolean closed;
    private ServerSocket server;
    private long expiresAt;
    private int downloads;

    public TemporaryHttpFileServer(TemporaryTransferPolicy policy, File file) {
        this.policy = policy;
        this.file = requireFile(file);
        this.publishedName = safeName(file.getName());
        this.token = randomToken();
    }

    public synchronized String start() throws Exception {
        if (server != null) throw new IllegalStateException("Server already started");
        server = new ServerSocket(0, 2, policy.getBindAddress());
        server.setSoTimeout(1_000);
        expiresAt = System.currentTimeMillis() + policy.getLifetimeMillis();
        executor.submit(this::acceptLoop);
        String host = policy.getBindAddress().getHostAddress();
        if (host.contains(":")) host = "[" + host + "]";
        return "http://" + host + ":" + server.getLocalPort() + "/" + token + "/" + publishedName;
    }

    public int getDownloads() { return downloads; }

    private void acceptLoop() {
        while (!closed && System.currentTimeMillis() < expiresAt && downloads < policy.getMaxDownloads()) {
            try (Socket socket = server.accept()) {
                socket.setSoTimeout(5_000);
                handle(socket);
            } catch (SocketTimeoutException ignored) {
            } catch (Exception ignored) {
            }
        }
        close();
    }

    private void handle(Socket socket) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.US_ASCII));
        String request = reader.readLine();
        for (String line; (line = reader.readLine()) != null && !line.isEmpty();) { }
        String expected = "GET /" + token + "/" + publishedName + " HTTP/";
        BufferedOutputStream output = new BufferedOutputStream(socket.getOutputStream());
        if (request == null || !request.startsWith(expected)) {
            output.write("HTTP/1.1 404 Not Found\r\nConnection: close\r\nContent-Length: 0\r\n\r\n"
                    .getBytes(StandardCharsets.US_ASCII));
            output.flush();
            return;
        }
        String headers = "HTTP/1.1 200 OK\r\nContent-Type: application/octet-stream\r\n"
                + "Content-Length: " + file.length() + "\r\nConnection: close\r\n"
                + "Cache-Control: no-store\r\nX-Content-Type-Options: nosniff\r\n\r\n";
        output.write(headers.getBytes(StandardCharsets.US_ASCII));
        try (BufferedInputStream input = new BufferedInputStream(new FileInputStream(file))) {
            byte[] buffer = new byte[32 * 1024];
            for (int count; (count = input.read(buffer)) >= 0;) output.write(buffer, 0, count);
        }
        output.flush();
        downloads++;
    }

    @Override public synchronized void close() {
        if (closed) return;
        closed = true;
        try { if (server != null) server.close(); } catch (Exception ignored) { }
        executor.shutdownNow();
    }

    private static File requireFile(File file) {
        if (file == null || !file.isFile() || !file.canRead() || file.length() < 0 || file.length() > MAX_FILE_BYTES) {
            throw new IllegalArgumentException("Invalid transfer file");
        }
        return file;
    }

    private static String safeName(String value) {
        String result = value == null ? "" : value.replaceAll("[^A-Za-z0-9._-]", "_");
        if (result.isEmpty()) result = "download.bin";
        return result.length() > 96 ? result.substring(result.length() - 96) : result;
    }

    private static String randomToken() {
        byte[] bytes = new byte[16];
        new SecureRandom().nextBytes(bytes);
        StringBuilder result = new StringBuilder();
        for (byte value : bytes) result.append(String.format(Locale.ROOT, "%02x", value & 0xff));
        return result.toString();
    }
}
