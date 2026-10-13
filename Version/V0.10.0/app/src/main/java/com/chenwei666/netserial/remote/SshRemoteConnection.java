package com.chenwei666.netserial.remote;

import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.ChannelDirectTCPIP;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SocketFactory;
import com.jcraft.jsch.UIKeyboardInteractive;
import com.jcraft.jsch.UserInfo;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class SshRemoteConnection implements RemoteConnection {
    private final RemoteConnectionConfig config;
    private final RemoteConnectionListener listener;
    private final File knownHostsFile;
    private final SshConnectionOptions options;
    private final ExecutorService readerWorker = Executors.newSingleThreadExecutor();
    private final ExecutorService commandWorker = Executors.newSingleThreadExecutor();
    private final ExecutorService transferWorker = Executors.newSingleThreadExecutor();
    private final RemoteTextDecoder decoder;
    private volatile RemoteConnectionState state = RemoteConnectionState.DISCONNECTED;
    private volatile Session session;
    private volatile ChannelShell channel;
    private volatile OutputStream output;
    private volatile Session jumpSession;

    public SshRemoteConnection(RemoteConnectionConfig config, RemoteConnectionListener listener, File knownHostsFile) {
        this(config, listener, knownHostsFile, SshConnectionOptions.passwordOnly());
    }

    public SshRemoteConnection(RemoteConnectionConfig config, RemoteConnectionListener listener,
                               File knownHostsFile, SshConnectionOptions options) {
        this.config = Objects.requireNonNull(config, "config");
        if (config.getProtocol() != RemoteProtocol.SSH) throw new IllegalArgumentException("SSH config required");
        this.listener = Objects.requireNonNull(listener, "listener");
        this.knownHostsFile = Objects.requireNonNull(knownHostsFile, "knownHostsFile");
        this.options = Objects.requireNonNull(options, "options");
        this.decoder = new RemoteTextDecoder(config.getCharset());
    }

    @Override
    public synchronized void connect(char[] password) {
        connect(new SshCredentials(password, null));
        if (password != null) Arrays.fill(password, '\0');
    }

    public synchronized void connect(SshCredentials credentials) {
        if (state != RemoteConnectionState.DISCONNECTED) {
            if (credentials != null) credentials.close();
            return;
        }
        final SshCredentials credentialCopy = Objects.requireNonNull(credentials, "credentials");
        changeState(RemoteConnectionState.CONNECTING, "connecting");
        readerWorker.execute(() -> connectAndRead(credentialCopy));
    }

    private void connectAndRead(SshCredentials credentials) {
        char[] secret = credentials.copySecret();
        char[] jumpPassword = credentials.copyJumpPassword();
        byte[] privateKey = options.copyPrivateKey();
        byte[] passphrase = new String(secret).getBytes(StandardCharsets.UTF_8);
        try {
            ensureKnownHostsFile();
            JSch jsch = new JSch();
            jsch.setKnownHosts(knownHostsFile.getAbsolutePath());
            if (options.getAuthenticationMode() == SshAuthenticationMode.PRIVATE_KEY) {
                jsch.addIdentity("netserial-session-key", privateKey, null,
                        passphrase.length == 0 ? null : passphrase);
            }
            if (options.getJumpHost() != null) {
                jumpSession = connectJumpHost(options.getJumpHost(), jumpPassword);
            }
            Session connectedSession = jsch.getSession(config.getUsername(), config.getHost(), config.getPort());
            connectedSession.setConfig("StrictHostKeyChecking", "ask");
            connectedSession.setConfig("HashKnownHosts", "yes");
            connectedSession.setConfig("PreferredAuthentications",
                    options.getAuthenticationMode() == SshAuthenticationMode.PRIVATE_KEY
                            ? "publickey" : "password,keyboard-interactive");
            connectedSession.setUserInfo(new ConfirmingUserInfo(listener, secret));
            if (options.getAuthenticationMode() == SshAuthenticationMode.PASSWORD && secret.length > 0) {
                connectedSession.setPassword(new String(secret));
            }
            if (jumpSession != null) {
                connectedSession.setSocketFactory(new JumpSocketFactory(jumpSession,
                        config.getHost(), config.getPort(), config.getTimeoutMillis()));
            }
            if (options.getKeepAliveMillis() > 0) {
                connectedSession.setServerAliveInterval(options.getKeepAliveMillis());
                connectedSession.setServerAliveCountMax(3);
            }
            session = connectedSession;
            connectedSession.connect(config.getTimeoutMillis());

            ChannelShell connectedChannel = (ChannelShell) connectedSession.openChannel("shell");
            connectedChannel.setPtyType("xterm", 120, 40, 0, 0);
            InputStream input = connectedChannel.getInputStream();
            output = connectedChannel.getOutputStream();
            channel = connectedChannel;
            connectedChannel.connect(config.getTimeoutMillis());
            changeState(RemoteConnectionState.CONNECTED, "connected");
            credentials.close();
            Arrays.fill(secret, '\0');
            Arrays.fill(jumpPassword, '\0');
            Arrays.fill(privateKey, (byte) 0);
            Arrays.fill(passphrase, (byte) 0);

            byte[] buffer = new byte[4096];
            int count;
            while (state == RemoteConnectionState.CONNECTED && (count = input.read(buffer)) >= 0) {
                if (count > 0) listener.onTextReceived(decoder.decode(Arrays.copyOf(buffer, count)));
            }
        } catch (Exception exception) {
            if (state != RemoteConnectionState.DISCONNECTING) listener.onError(safeMessage(exception));
        } finally {
            credentials.close();
            Arrays.fill(secret, '\0');
            Arrays.fill(jumpPassword, '\0');
            Arrays.fill(privateKey, (byte) 0);
            Arrays.fill(passphrase, (byte) 0);
            options.clearPrivateKey();
            closeResources();
            changeState(RemoteConnectionState.DISCONNECTED, "disconnected");
        }
    }

    private Session connectJumpHost(JumpHostConfig jump, char[] password) throws Exception {
        JSch jumpJsch = new JSch();
        jumpJsch.setKnownHosts(knownHostsFile.getAbsolutePath());
        Session connected = jumpJsch.getSession(jump.getUsername(), jump.getHost(), jump.getPort());
        connected.setConfig("StrictHostKeyChecking", "ask");
        connected.setConfig("HashKnownHosts", "yes");
        connected.setConfig("PreferredAuthentications", "password,keyboard-interactive");
        connected.setUserInfo(new ConfirmingUserInfo(listener, password));
        if (password.length > 0) connected.setPassword(new String(password));
        connected.connect(config.getTimeoutMillis());
        return connected;
    }

    public void download(String remotePath, OutputStream destination, SftpTransferListener transferListener) {
        transfer(remotePath, destination, null, false, transferListener);
    }

    public void upload(InputStream source, String remotePath, SftpTransferListener transferListener) {
        transfer(remotePath, null, source, true, transferListener);
    }

    private void transfer(String remotePath, OutputStream destination, InputStream source,
                          boolean upload, SftpTransferListener transferListener) {
        String path = validateRemotePath(remotePath);
        Objects.requireNonNull(transferListener, "transferListener");
        transferWorker.execute(() -> {
            ChannelSftp sftp = null;
            try {
                Session active = session;
                if (active == null || !active.isConnected()) throw new IllegalStateException("not connected");
                sftp = (ChannelSftp) active.openChannel("sftp");
                sftp.connect(config.getTimeoutMillis());
                if (upload) sftp.put(Objects.requireNonNull(source, "source"), path);
                else sftp.get(path, Objects.requireNonNull(destination, "destination"));
                transferListener.onComplete();
            } catch (Exception exception) {
                transferListener.onError(safeMessage(exception));
            } finally {
                if (sftp != null) sftp.disconnect();
                try { if (source != null) source.close(); } catch (Exception ignored) { }
                try { if (destination != null) destination.close(); } catch (Exception ignored) { }
            }
        });
    }

    private static String validateRemotePath(String value) {
        String path = Objects.requireNonNull(value, "remotePath").trim();
        if (path.isEmpty() || path.length() > 1024 || path.indexOf('\0') >= 0
                || path.indexOf('\r') >= 0 || path.indexOf('\n') >= 0) {
            throw new IllegalArgumentException("invalid remote path");
        }
        return path;
    }

    @Override
    public void send(byte[] data) {
        byte[] copy = data == null ? new byte[0] : data.clone();
        commandWorker.execute(() -> {
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
        if (state == RemoteConnectionState.DISCONNECTED) {
            shutdownWorkers();
            return;
        }
        changeState(RemoteConnectionState.DISCONNECTING, "disconnecting");
        closeResources();
        shutdownWorkers();
    }

    private void shutdownWorkers() {
        readerWorker.shutdownNow();
        commandWorker.shutdownNow();
        transferWorker.shutdownNow();
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
        if (jumpSession != null) jumpSession.disconnect();
        output = null;
        channel = null;
        session = null;
        jumpSession = null;
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

    private static final class ConfirmingUserInfo implements UserInfo, UIKeyboardInteractive {
        private final RemoteConnectionListener listener;
        private final char[] secret;
        private ConfirmingUserInfo(RemoteConnectionListener listener, char[] secret) {
            this.listener = listener;
            this.secret = secret == null ? new char[0] : secret;
        }
        @Override public String getPassphrase() { return null; }
        @Override public String getPassword() { return secret.length == 0 ? null : new String(secret); }
        @Override public boolean promptPassword(String message) { return secret.length > 0; }
        @Override public boolean promptPassphrase(String message) { return secret.length > 0; }
        @Override public void showMessage(String message) { listener.onTextReceived("\r\n[SSH] " + message + "\r\n"); }
        @Override public boolean promptYesNo(String message) {
            String lower = message == null ? "" : message.toLowerCase(Locale.ROOT);
            if (lower.contains("changed") || lower.contains("identification has changed")) {
                listener.onError("SSH host key changed; connection blocked. Verify the device before forgetting the saved key.");
                return false;
            }
            return listener.confirmUnknownSshHost(message == null ? "Unknown SSH host key" : message);
        }
        @Override public String[] promptKeyboardInteractive(String destination, String name,
                                                            String instruction, String[] prompt,
                                                            boolean[] echo) {
            if (secret.length == 0 || prompt == null) return null;
            String[] response = new String[prompt.length];
            for (int index = 0; index < response.length; index++) response[index] = new String(secret);
            return response;
        }
    }

    private static final class JumpSocketFactory implements SocketFactory {
        private final Session jump;
        private final String targetHost;
        private final int targetPort;
        private final int timeoutMillis;

        private JumpSocketFactory(Session jump, String targetHost, int targetPort, int timeoutMillis) {
            this.jump = jump;
            this.targetHost = targetHost;
            this.targetPort = targetPort;
            this.timeoutMillis = timeoutMillis;
        }

        @Override public Socket createSocket(String host, int port) throws IOException {
            try {
                ChannelDirectTCPIP channel = (ChannelDirectTCPIP) jump.openChannel("direct-tcpip");
                channel.setHost(targetHost);
                channel.setPort(targetPort);
                channel.setOrgIPAddress("127.0.0.1");
                channel.setOrgPort(0);
                InputStream input = channel.getInputStream();
                OutputStream output = channel.getOutputStream();
                channel.connect(timeoutMillis);
                return new ChannelSocket(channel, input, output);
            } catch (Exception exception) {
                throw new IOException("unable to open SSH jump channel", exception);
            }
        }

        @Override public InputStream getInputStream(Socket socket) throws IOException {
            return ((ChannelSocket) socket).input;
        }

        @Override public OutputStream getOutputStream(Socket socket) throws IOException {
            return ((ChannelSocket) socket).output;
        }
    }

    private static final class ChannelSocket extends Socket {
        private final ChannelDirectTCPIP channel;
        private final InputStream input;
        private final OutputStream output;

        private ChannelSocket(ChannelDirectTCPIP channel, InputStream input, OutputStream output) {
            this.channel = channel;
            this.input = input;
            this.output = output;
        }

        @Override public synchronized void close() throws IOException {
            try { output.close(); } finally {
                try { input.close(); } finally { channel.disconnect(); }
            }
        }

        @Override public boolean isConnected() { return channel.isConnected(); }
    }
}
