package com.chenwei666.netserial.agent;

import com.chenwei666.netserial.ai.RequestCancellation;
import com.chenwei666.netserial.terminal.AnsiTextSanitizer;
import com.chenwei666.netserial.terminal.SensitiveTextRedactor;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

/** Process-local capability for one exact live connection. Never selects a latest device. */
public final class AgentSessionBridge {
    public static final String EXTRA_AGENT_SESSION = "agent_session";
    private static final AgentSessionBridge INSTANCE = new AgentSessionBridge();
    private static final int LIMIT = 65536;
    private static final int MAX_SESSIONS = 16;
    public static final String SENSITIVE_OUTPUT_WITHHELD = "[SENSITIVE_OUTPUT_WITHHELD: CLI prompt returned; output not verified]";
    private static final String SENSITIVE_UI_NOTICE = "\r\n[Sensitive output hidden. Reconnect before reading results / 敏感回显已隐藏，请重新连接后读取结果]\r\n";
    private static final Pattern PROMPT = Pattern.compile("(?:^|[\\r\\n])(?:<[A-Za-z0-9_.:/-]{1,100}>|\\[[*~]?[A-Za-z0-9_.:/() -]{1,100}\\]|[A-Za-z0-9_.:/-]{1,100}(?:\\([A-Za-z0-9_.:/ -]{1,80}\\))?[>#])\\s*$");
    private static final Pattern STOP = Pattern.compile("(?im)(--\\s*more\\s*--|----\\s*more|press\\s+(?:any|enter|return|space)|\\[(?:y/n|yes/no|confirm)\\]|(?:password|username|login)\\s*:|are you sure|continue\\s*\\?|确认|是否继续)");
    private static final Pattern ERROR = Pattern.compile("(?im)(?:^|[\\r\\n])\\s*(?:%\\s*(?:error|invalid|incomplete|ambiguous|unknown|unrecognized|wrong|failed|permission|access denied)|(?:error|invalid command|unknown command|unrecognized command|incomplete command|ambiguous command|permission denied|access denied|command not found|错误)\\b)");
    public interface Sender { void send(String command) throws Exception; }
    public interface ManualWrite { void write() throws Exception; }
    private final Map<String, Session> sessions = new LinkedHashMap<>();
    private final long timeoutMillis;
    private final long quietMillis;
    public AgentSessionBridge() { this(15000, 300); }
    AgentSessionBridge(long timeoutMillis, long quietMillis) {
        this.timeoutMillis = timeoutMillis; this.quietMillis = quietMillis;
    }
    public static AgentSessionBridge shared() { return INSTANCE; }
    private static final class Session {
        final String id, token, target;
        final Sender sender;
        final StringBuilder history = new StringBuilder();
        final StringBuilder response = new StringBuilder();
        Lease lease;
        boolean connected = true, tainted, running;
        boolean outputSuppressed, suppressionNoticeSent, readyAfterSensitive;
        int manualPending;
        long lastReceive;
        Session(String id, String target, Sender sender) {
            this.id = id; this.target = target; this.sender = sender;
            token = UUID.randomUUID().toString();
        }
    }
    public synchronized void register(String id, String target, Sender sender) {
        if (id == null || id.isEmpty() || target == null || sender == null) throw new IllegalArgumentException("session");
        disconnect(id);
        if (sessions.size() >= MAX_SESSIONS) {
            Session retired = null;
            for (Session candidate : sessions.values()) {
                if (candidate.lease == null && !candidate.running && candidate.manualPending == 0) { retired = candidate; break; }
            }
            if (retired == null) throw new IllegalStateException("Too many active terminal sessions");
            disconnect(retired.id);
        }
        sessions.put(id, new Session(id, target, sender));
    }
    public synchronized String sessionToken(String id) {
        Session s = sessions.get(id);
        return s != null && s.connected && !s.tainted ? s.token : null;
    }
    public synchronized void disconnect(String id) {
        Session s = sessions.remove(id);
        if (s != null) { s.connected = false; s.tainted = true; wipe(s.history); wipe(s.response); }
        notifyAll();
    }
    /** Errors and ambiguous completion require reconnect, never automatic replay. */
    public synchronized void invalidate(String id) {
        Session s = sessions.get(id);
        if (s != null) s.tainted = true;
        notifyAll();
    }
    public synchronized void receive(String id, String text) {
        Session s = sessions.get(id);
        if (s == null || !s.connected || text == null || text.isEmpty()) return;
        s.lastReceive = now();
        if (!s.outputSuppressed) {
            s.history.append(text);
            if (s.history.length() > LIMIT) s.history.delete(0, s.history.length() - LIMIT);
        } else if (!s.running) {
            // A late echo invalidates readiness. Never retain its secret-bearing fragments.
            s.readyAfterSensitive = false;
        }
        if (s.running) {
            if (s.response.length() + text.length() > LIMIT) s.tainted = true;
            else s.response.append(text);
        }
        notifyAll();
    }
    /** Transport callbacks use their captured epoch, not a mutable current connection id. */
    public synchronized void receiveToken(String token, String text) {
        if (token == null) return;
        for (Session s : sessions.values()) if (s.token.equals(token)) { receive(s.id, text); return; }
    }
    /** Call at the raw transport boundary BEFORE enqueuing data for terminal/UI/evidence. */
    public synchronized String maskIncomingToken(String token, String text) {
        if (token == null || text == null || text.isEmpty()) return "";
        for (Session s : sessions.values()) if (s.token.equals(token) && s.connected) {
            if (!s.outputSuppressed) return text;
            if (s.suppressionNoticeSent) return "";
            s.suppressionNoticeSent = true;
            return SENSITIVE_UI_NOTICE;
        }
        return ""; // Retired epochs must not surface delayed data.
    }
    public synchronized Lease acquire(String token) throws IOException {
        Session s = null;
        for (Session candidate : sessions.values()) if (candidate.token.equals(token)) s = candidate;
        if (s == null || !s.connected || s.tainted) throw new IOException("Session expired or uncertain; reconnect and reopen AI from the terminal.");
        if (s.outputSuppressed) throw new IOException("Sensitive output is hidden; reconnect before starting another AI task.");
        if (s.lease != null) throw new IOException("Another task owns this session.");
        if (s.manualPending != 0) throw new IOException("Manual write pending; wait for CLI completion.");
        s.lease = new Lease(s);
        return s.lease;
    }
    /** Serialize every manual write against acquisition, including control keys and transfers. */
    public synchronized void manualWrite(String id, ManualWrite action) throws Exception {
        Session s = sessions.get(id);
        if (s != null && (s.lease != null || s.running)) throw new IOException("AI task active. Stop it before using the terminal.");
        if (s != null) s.history.setLength(0);
        action.write();
    }
    public synchronized ManualPermit reserveManual(String id) throws IOException {
        Session s = sessions.get(id);
        if (s == null || !s.connected || s.lease != null || s.running) throw new IOException("Session unavailable or AI task active.");
        s.manualPending++;
        s.history.setLength(0);
        return new ManualPermit(s);
    }
    public final class ManualPermit implements AutoCloseable {
        private final Session session;
        private boolean closed;
        private ManualPermit(Session s) { session = s; }
        public void write(ManualWrite action) throws Exception {
            synchronized (AgentSessionBridge.this) {
                if (closed || !session.connected || sessions.get(session.id) != session) throw new IOException("Connection changed");
                action.write();
            }
        }
        public void close() {
            synchronized (AgentSessionBridge.this) {
                if (!closed) { closed = true; session.manualPending--; }
                AgentSessionBridge.this.notifyAll();
            }
        }
    }
    public synchronized boolean isLeased(String id) {
        Session s = sessions.get(id); return s != null && s.lease != null;
    }
    public final class Lease implements AutoCloseable {
        private final Session session;
        private boolean closed;
        private Lease(Session session) { this.session = session; }
        public String getTarget() { return session.target; }
        public String initialOutput() {
            synchronized (AgentSessionBridge.this) {
                return session.outputSuppressed ? "[SENSITIVE_OUTPUT_WITHHELD: reconnect required for readback]" : redact(clean(session.history.toString()));
            }
        }
        public boolean isOutputSuppressed() {
            synchronized (AgentSessionBridge.this) { return session.outputSuppressed; }
        }
        /** Local-template credentials only. Input secret is caller-owned; the scoped copy is wiped. */
        public String executeSensitive(String rawCommand, String redactedCommand, char[] secret,
                                       RequestCancellation cancel) throws Exception {
            char[] scopedSecret = secret == null ? new char[0] : secret.clone();
            try {
                if (scopedSecret.length == 0 || rawCommand == null || redactedCommand == null
                        || !contains(rawCommand, scopedSecret) || contains(redactedCommand, scopedSecret))
                    throw new IOException("Invalid local sensitive command");
                synchronized (AgentSessionBridge.this) {
                    if (!valid() || session.running || cancel == null || cancel.isCancelled())
                        throw new IOException("Task cancelled or session unavailable");
                    if (!session.outputSuppressed) session.readyAfterSensitive = ready(session);
                    session.outputSuppressed = true;
                    wipe(session.history);
                }
                return execute(rawCommand, cancel);
            } finally {
                Arrays.fill(scopedSecret, '\0');
            }
        }
        public boolean isValid() {
            synchronized (AgentSessionBridge.this) { return valid(); }
        }
        private boolean valid() {
            return !closed && session.connected && !session.tainted && session.lease == this
                    && sessions.get(session.id) == session;
        }
        /** Blocking worker-thread method. Sender must synchronously perform, not queue, its write. */
        public String execute(String command, RequestCancellation cancel) throws Exception {
            if (command == null || command.trim().isEmpty() || command.length() > 2048
                    || Pattern.compile("[\\x00-\\x1f\\x7f]").matcher(command).find())
                throw new IOException("Exactly one printable CLI command is required.");
            synchronized (AgentSessionBridge.this) {
                if (!valid() || cancel == null || cancel.isCancelled()) throw new IOException("Task cancelled or session expired.");
                if (session.running) throw new IOException("A command is already running.");
                if (!ready(session))
                    throw new IOException("No ready CLI prompt. Finish login/paging manually, then reopen the task.");
                // An idle prompt must stay quiet before starting; never overlap a manual command.
                if (now() - session.lastReceive < quietMillis) {
                    long until = now() + timeoutMillis;
                    while (now() - session.lastReceive < quietMillis) {
                        if (!valid() || cancel.isCancelled() || now() >= until) throw new IOException("Session is busy or cancelled.");
                        AgentSessionBridge.this.wait(Math.min(50, quietMillis));
                    }
                    if (!ready(session)) throw new IOException("CLI is not ready.");
                }
                // wait() released the monitor: stop or a concurrent execute may have won meanwhile.
                if (!valid() || cancel.isCancelled()) throw new IOException("Task cancelled or session expired.");
                if (session.running) throw new IOException("A command is already running.");
                session.response.setLength(0);
                session.running = true;
            }
            long deadline = now() + timeoutMillis;
            try {
                // This command is now in flight. Never hold the state monitor over I/O: stop/close
                // must remain responsive, and cannot retract a write already handed to transport.
                session.sender.send(command);
                synchronized (AgentSessionBridge.this) {
                    while (true) {
                        if (!valid() || cancel.isCancelled()) throw new IOException("Stopped; the last command may already have changed the device.");
                        String output = clean(session.response.toString());
                        if (STOP.matcher(output).find()) throw new IOException("Interactive prompt or pagination detected; automatic execution stopped.");
                        if (ERROR.matcher(output).find()) throw new IOException("Device rejected command; automatic execution stopped.\n" + (session.outputSuppressed ? "[Sensitive output withheld]" : redact(output)));
                        if (!output.isEmpty() && PROMPT.matcher(output).find() && now() - session.lastReceive >= quietMillis) {
                            session.running = false;
                            session.readyAfterSensitive = session.outputSuppressed;
                            return session.outputSuppressed ? SENSITIVE_OUTPUT_WITHHELD : redact(output);
                        }
                        if (now() >= deadline) throw new IOException("Timed out waiting for a complete CLI response; reconnect before another AI task.");
                        AgentSessionBridge.this.wait(Math.min(50, quietMillis));
                    }
                }
            } catch (Exception failure) {
                synchronized (AgentSessionBridge.this) { session.tainted = true; session.running = false; }
                if (failure instanceof InterruptedException) Thread.currentThread().interrupt();
                if (session.outputSuppressed) throw new IOException("Sensitive operation stopped; output withheld. The device may have changed. Reconnect before readback.");
                throw failure;
            } finally {
                synchronized (AgentSessionBridge.this) { if (session.outputSuppressed) wipe(session.response); }
            }
        }
        @Override public void close() {
            synchronized (AgentSessionBridge.this) {
                if (session.running) session.tainted = true;
                closed = true;
                if (session.lease == this) session.lease = null;
                AgentSessionBridge.this.notifyAll();
            }
        }
    }
    private static String clean(String value) { return new AnsiTextSanitizer().sanitize(value); }
    private static boolean ready(Session s) {
        if (s.outputSuppressed) return s.readyAfterSensitive;
        String prior = clean(s.history.toString());
        return PROMPT.matcher(prior).find() && !STOP.matcher(lastLines(prior)).find();
    }
    private static boolean contains(String value, char[] needle) {
        for (int start = 0; start <= value.length() - needle.length; start++) {
            int index = 0;
            while (index < needle.length && value.charAt(start + index) == needle[index]) index++;
            if (index == needle.length) return true;
        }
        return false;
    }
    private static void wipe(StringBuilder value) {
        for (int i = 0; i < value.length(); i++) value.setCharAt(i, '\0');
        value.setLength(0);
    }
    private static String redact(String value) { return new SensitiveTextRedactor().redact(value); }
    private static String lastLines(String value) {
        int start = value.lastIndexOf('\n'); return start < 0 ? value : value.substring(start + 1);
    }
    private static long now() { return System.nanoTime() / 1000000L; }
}
