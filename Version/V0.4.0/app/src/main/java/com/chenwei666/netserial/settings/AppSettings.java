package com.chenwei666.netserial.settings;

import java.nio.charset.Charset;
import java.util.Objects;

public final class AppSettings {
    private final AppLanguage language;
    private final boolean telnetEnabled;
    private final int remoteTimeoutMillis;
    private final int terminalTextSizeSp;
    private final String remoteCharset;
    private final int sshKeepAliveMillis;
    private final int networkProbeTimeoutMillis;

    public AppSettings(AppLanguage language, boolean telnetEnabled, int remoteTimeoutMillis,
                       int terminalTextSizeSp, String remoteCharset) {
        this(language, telnetEnabled, remoteTimeoutMillis, terminalTextSizeSp, remoteCharset,
                30_000, 5_000);
    }

    public AppSettings(AppLanguage language, boolean telnetEnabled, int remoteTimeoutMillis,
                       int terminalTextSizeSp, String remoteCharset, int sshKeepAliveMillis,
                       int networkProbeTimeoutMillis) {
        this.language = Objects.requireNonNull(language, "language");
        this.telnetEnabled = telnetEnabled;
        if (remoteTimeoutMillis < 2_000 || remoteTimeoutMillis > 60_000) throw new IllegalArgumentException("invalid timeout");
        if (terminalTextSizeSp < 10 || terminalTextSizeSp > 28) throw new IllegalArgumentException("invalid terminal text size");
        validateCharset(remoteCharset);
        this.remoteTimeoutMillis = remoteTimeoutMillis;
        this.terminalTextSizeSp = terminalTextSizeSp;
        this.remoteCharset = remoteCharset;
        if (sshKeepAliveMillis < 0 || sshKeepAliveMillis > 300_000) throw new IllegalArgumentException("invalid keepalive");
        if (networkProbeTimeoutMillis < 1_000 || networkProbeTimeoutMillis > 15_000) throw new IllegalArgumentException("invalid probe timeout");
        this.sshKeepAliveMillis = sshKeepAliveMillis;
        this.networkProbeTimeoutMillis = networkProbeTimeoutMillis;
    }

    public static AppSettings defaults() {
        return new AppSettings(AppLanguage.SYSTEM, false, 10_000, 14, "UTF-8");
    }

    public AppLanguage getLanguage() { return language; }
    public boolean isTelnetEnabled() { return telnetEnabled; }
    public int getRemoteTimeoutMillis() { return remoteTimeoutMillis; }
    public int getTerminalTextSizeSp() { return terminalTextSizeSp; }
    public String getRemoteCharset() { return remoteCharset; }
    public int getSshKeepAliveMillis() { return sshKeepAliveMillis; }
    public int getNetworkProbeTimeoutMillis() { return networkProbeTimeoutMillis; }

    private static void validateCharset(String value) {
        try {
            if (value == null || value.length() > 32 || !Charset.isSupported(value)) {
                throw new IllegalArgumentException("invalid remote charset");
            }
        } catch (RuntimeException exception) {
            throw new IllegalArgumentException("invalid remote charset", exception);
        }
    }
}
