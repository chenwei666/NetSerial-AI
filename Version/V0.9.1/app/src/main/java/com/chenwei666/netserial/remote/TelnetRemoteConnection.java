package com.chenwei666.netserial.remote;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class TelnetRemoteConnection implements RemoteConnection {
    private final RemoteConnectionConfig config;
    private final RemoteConnectionListener listener;
    private final ExecutorService worker = Executors.newFixedThreadPool(2);
    private final TelnetProtocolCodec codec = new TelnetProtocolCodec();
    private final RemoteTextDecoder decoder;
    private volatile RemoteConnectionState state = RemoteConnectionState.DISCONNECTED;
    private volatile Socket socket;
    private volatile OutputStream output;

    public TelnetRemoteConnection(RemoteConnectionConfig config, RemoteConnectionListener listener) {
        this.config = Objects.requireNonNull(config, "config");
        if (config.getProtocol() != RemoteProtocol.TELNET) throw new IllegalArgumentException("TELNET config required");
        this.listener = Objects.requireNonNull(listener, "listener");
        this.decoder = new RemoteTextDecoder(config.getCharset());
    }

    @Override
    public synchronized void connect(char[] password) {
        if (password != null) Arrays.fill(password, '\0');
        if (state != RemoteConnectionState.DISCONNECTED) return;
        changeState(RemoteConnectionState.CONNECTING, "connecting");
        worker.execute(this::connectAndRead);
    }

    private void connectAndRead() {
        try {
            Socket connectedSocket = new Socket();
            connectedSocket.connect(new InetSocketAddress(config.getHost(), config.getPort()), config.getTimeoutMillis());
            connectedSocket.setKeepAlive(true);
            connectedSocket.setTcpNoDelay(true);
            socket = connectedSocket;
            output = connectedSocket.getOutputStream();
            changeState(RemoteConnectionState.CONNECTED, "connected");
            InputStream input = connectedSocket.getInputStream();
            byte[] buffer = new byte[4096];
            int count;
            while (state == RemoteConnectionState.CONNECTED && (count = input.read(buffer)) >= 0) {
                TelnetFrame frame = codec.process(Arrays.copyOf(buffer, count));
                byte[] negotiation = frame.getResponse();
                if (negotiation.length > 0) writeNow(negotiation);
                byte[] payload = frame.getPayload();
                if (payload.length > 0) listener.onTextReceived(decoder.decode(payload));
            }
        } catch (Exception exception) {
            if (state != RemoteConnectionState.DISCONNECTING) listener.onError(safeMessage(exception));
        } finally {
            closeResources();
            changeState(RemoteConnectionState.DISCONNECTED, "disconnected");
        }
    }

    @Override
    public void send(byte[] data) {
        byte[] copy = data == null ? new byte[0] : data.clone();
        worker.execute(() -> {
            try { writeNow(codec.encodeOutgoing(copy)); }
            catch (Exception exception) { listener.onError(safeMessage(exception)); }
        });
    }

    private synchronized void writeNow(byte[] data) throws Exception {
        if (state != RemoteConnectionState.CONNECTED || output == null) throw new IllegalStateException("not connected");
        output.write(data);
        output.flush();
    }

    @Override
    public synchronized void disconnect() {
        if (state == RemoteConnectionState.DISCONNECTED) {
            worker.shutdownNow();
            return;
        }
        changeState(RemoteConnectionState.DISCONNECTING, "disconnecting");
        closeResources();
        worker.shutdownNow();
    }

    private synchronized void closeResources() {
        try { if (output != null) output.close(); } catch (Exception ignored) { }
        try { if (socket != null) socket.close(); } catch (Exception ignored) { }
        output = null;
        socket = null;
    }

    @Override public RemoteConnectionState getState() { return state; }

    private void changeState(RemoteConnectionState next, String detail) {
        state = next;
        listener.onStateChanged(next, detail);
    }

    private static String safeMessage(Exception exception) {
        String message = exception.getMessage();
        return message == null || message.trim().isEmpty() ? exception.getClass().getSimpleName() : message;
    }
}
