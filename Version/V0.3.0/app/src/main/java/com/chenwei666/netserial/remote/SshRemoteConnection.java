package com.chenwei666.netserial.remote;

import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.UserInfo;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class SshRemoteConnection implements RemoteConnection {
    private final RemoteConnectionConfig config;
    private final RemoteConnectionListener listener;
    private final File knownHostsFile;
    private final ExecutorService worker = Executors.newFixedThreadPool(2);
    private final RemoteTextDecoder decoder;
    private volatile RemoteConnectionState state = RemoteConnectionState.DISCONNECTED;
    private volatile Session session;
    private volatile ChannelShell channel;
    private volatile OutputStream output;

    public SshRemoteConnection(RemoteConnectionConfig config, RemoteConnectionListener listener, File knownHostsFile) {
        this.config = Objects.requireNonNull(config, "config");
        if (config.getProtocol() != RemoteProtocol.SSH) throw new IllegalArgumentException("SSH config required");
        this.listener = Objects.requireNonNull(listener, "listener");
        this.knownHostsFile = Objects.requireNonNull(knownHostsFile, "knownHostsFile");
        this.decoder = new RemoteTextDecoder(config.getCharset());
    }

    @Override
    public synchronized void connect(char[] password) {
        if (state != RemoteConnectionState.DISCONNECTED) {
            if (password != null) Arrays.fill(password, '\0');
            return;
        }
        final char[] passwordCopy = password == null ? new char[0] : password.clone();
        if (password != null) Arrays.fill(password, '\0');
        changeState(RemoteConnectionState.CONNECTING, "connecting");
        worker.execute(() -> connectAndRead(passwordCopy));
    }

    private void connectAndRead(char[] password) {
        try {
            ensureKnownHostsFile();
            JSch jsch = new JSch();
            jsch.setKnownHosts(knownHostsFile.getAbsolutePath());
            Session connectedSession = jsch.getSession(config.getUsername(), config.getHost(), config.getPort());
            connectedSession.setConfig("StrictHostKeyChecking", "ask");
            connectedSession.setConfig("HashKnownHosts", "yes");
            connectedSession.setConfig("PreferredAuthentications", "password,keyboard-interactive");
            connectedSession.setUserInfo(new ConfirmingUserInfo(listener));
            if (password.length > 0) connectedSession.setPassword(new String(password));
            session = connectedSession;
            connectedSession.connect(config.getTimeoutMillis());

            ChannelShell connectedChannel = (ChannelShell) connectedSession.openChannel("shell");
            connectedChannel.setPtyType("xterm", 120, 40, 0, 0);
            InputStream input = connectedChannel.getInputStream();
            output = connectedChannel.getOutputStream();
            channel = connectedChannel;
            connectedChannel.connect(config.getTimeoutMillis());
            changeState(RemoteConnectionState.CONNECTED, "connected");

            byte[] buffer = new byte[4096];
            int count;
            while (state == RemoteConnectionState.CONNECTED && (count = input.read(buffer)) >= 0) {
                if (count > 0) listener.onTextReceived(decoder.decode(Arrays.copyOf(buffer, count)));
            }
        } catch (Exception exception) {
            if (state != RemoteConnectionState.DISCONNECTING) listener.onError(safeMessage(exception));
        } finally {
            Arrays.fill(password, '\0');
            closeResources();
            changeState(RemoteConnectionState.DISCONNECTED, "disconnected");
        }
    }

    @Override
    public void send(byte[] data) {
        byte[] copy = data == null ? new byte[0] : data.clone();
        worker.execute(() -> {
            try {
                synchronized (this) {
                    if (state != RemoteConnectionState.CONNECTED || output == null) throw new IllegalStateException("not connected");
                    output.write(copy);
                    output.flush();
                }
            } catch (Exception exception) {
                listener.onError(safeMessage(exception));
            }
        });
    }

    @Override
    public synchronized void disconnect() {
        if (state == RemoteConnectionState.DISCONNECTED) return;
        changeState(RemoteConnectionState.DISCONNECTING, "disconnecting");
        closeResources();
        worker.shutdownNow();
    }

    private void ensureKnownHostsFile() throws Exception {
        File parent = knownHostsFile.getParentFile();
        if (parent != null && !parent.exists() && !parent.mkdirs()) throw new IllegalStateException("cannot create SSH data directory");
        if (!knownHostsFile.exists() && !knownHostsFile.createNewFile()) throw new IllegalStateException("cannot create known-hosts store");
    }

    private synchronized void closeResources() {
        try { if (output != null) output.close(); } catch (Exception ignored) { }
        if (channel != null) channel.disconnect();
        if (session != null) session.disconnect();
        output = null;
        channel = null;
        session = null;
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

    private static final class ConfirmingUserInfo implements UserInfo {
        private final RemoteConnectionListener listener;
        private ConfirmingUserInfo(RemoteConnectionListener listener) { this.listener = listener; }
        @Override public String getPassphrase() { return null; }
        @Override public String getPassword() { return null; }
        @Override public boolean promptPassword(String message) { return false; }
        @Override public boolean promptPassphrase(String message) { return false; }
        @Override public void showMessage(String message) { listener.onTextReceived("\r\n[SSH] " + message + "\r\n"); }
        @Override public boolean promptYesNo(String message) {
            String lower = message == null ? "" : message.toLowerCase(Locale.ROOT);
            if (lower.contains("changed") || lower.contains("identification has changed")) {
                listener.onError("SSH host key changed; connection blocked. Verify the device before forgetting the saved key.");
                return false;
            }
            return listener.confirmUnknownSshHost(message == null ? "Unknown SSH host key" : message);
        }
    }
}
