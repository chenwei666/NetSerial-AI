package de.kai_morich.simple_usb_terminal;

import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;

import com.chenwei666.netserial.agent.AgentTaskRunner;
import com.chenwei666.netserial.ai.RequestCancellation;
import com.chenwei666.netserial.terminal.AnsiTextSanitizer;
import com.chenwei666.netserial.terminal.SensitiveTextRedactor;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.color.MaterialColors;

import java.util.concurrent.CancellationException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicBoolean;

/** Foreground-only execution UI. A supplied opaque session token is required; never picks a device. */
public final class AiAgentActivity extends ThemedActivity {
    public static final String EXTRA_SESSION = "agent_session";
    private static final int SETTINGS = 3101;
    private final ExecutorService worker = Executors.newSingleThreadExecutor();
    private final ExecutorService control = Executors.newSingleThreadExecutor();
    private final SensitiveTextRedactor redactor = new SensitiveTextRedactor();
    private final AnsiTextSanitizer ansi = new AnsiTextSanitizer();
    private volatile int generation;
    private volatile AgentTaskRunner runner;
    private RequestCancellation cancellation;
    private EditText goal;
    private Button start;
    private Button stop;
    private CheckBox useMemory;
    private View manageMemory;
    private TextView status;
    private LinearLayout events;
    private ScrollView scroll;
    private AlertDialog pendingDialog;
    private boolean busy;
    private boolean validSession;
    private String sessionId;

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
        setContentView(R.layout.activity_ai_agent);
        Toolbar toolbar = findViewById(R.id.agent_toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(view -> finish());
        goal = findViewById(R.id.agent_goal);
        start = findViewById(R.id.agent_start);
        stop = findViewById(R.id.agent_stop);
        status = findViewById(R.id.agent_status);
        events = findViewById(R.id.agent_events);
        scroll = findViewById(R.id.agent_scroll);
        useMemory = findViewById(R.id.agent_use_memory);
        manageMemory = findViewById(R.id.agent_memory_manage);
        boolean memorySupported = android.os.Build.VERSION.SDK_INT >= 23;
        useMemory.setChecked(memorySupported && getSharedPreferences("ai_chat_ui_v1", MODE_PRIVATE)
                .getBoolean("use_confirmed_memory", true));
        useMemory.setEnabled(memorySupported);
        manageMemory.setEnabled(memorySupported);
        useMemory.setOnCheckedChangeListener((button, checked) -> {
            if (!busy && android.os.Build.VERSION.SDK_INT >= 23) getSharedPreferences("ai_chat_ui_v1", MODE_PRIVATE)
                    .edit().putBoolean("use_confirmed_memory", checked).apply();
        });
        String memorySubject = new com.chenwei666.netserial.device.DeviceProfileStore(this).load().getName();
        ChatMemoryController memoryController = new ChatMemoryController(this,
                "Default switch".equals(memorySubject) ? "" : memorySubject, () -> { });
        manageMemory.setOnClickListener(view -> { if (!busy) memoryController.showManager(); });
        sessionId = getIntent().getStringExtra(EXTRA_SESSION);
        validSession = sessionId != null && !sessionId.trim().isEmpty() && state == null;
        if (!validSession) {
            ((TextView) findViewById(R.id.agent_target)).setText(R.string.agent_no_session);
            status.setText(state == null ? R.string.agent_no_session : R.string.agent_recreated);
        }
        start.setEnabled(validSession);
        start.setOnClickListener(view -> startTask());
        stop.setOnClickListener(view -> stopTask());
        View content = findViewById(R.id.agent_content);
        // Keep text readable on wide tablets without shrinking phone layouts.
        content.post(() -> {
            if (content.getWidth() > dp(920)) {
                android.view.ViewGroup.LayoutParams params = content.getLayoutParams();
                params.width = dp(920);
                content.setLayoutParams(params);
            }
        });
    }

    @Override public boolean onCreateOptionsMenu(Menu menu) {
        MenuItem settings = menu.add(Menu.NONE, SETTINGS, Menu.NONE, R.string.agent_settings);
        settings.setIcon(R.drawable.ic_nav_settings);
        settings.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        if (settings.getIcon() != null) androidx.core.graphics.drawable.DrawableCompat.setTint(
                settings.getIcon().mutate(), MaterialColors.getColor(findViewById(R.id.agent_root),
                        com.google.android.material.R.attr.colorOnSurface));
        return true;
    }

    @Override public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == SETTINGS) {
            stopTask();
            startActivity(new Intent(this, AiProviderSettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void startTask() {
        if (busy || !validSession) return;
        String task = ansi.sanitize(goal.getText().toString()).trim();
        if (task.isEmpty()) { status.setText(R.string.agent_required); return; }
        if (containsSecret(task)) { status.setText(R.string.agent_no_secrets); return; }
        final int epoch = ++generation;
        final RequestCancellation request = new RequestCancellation();
        cancellation = request;
        busy = true;
        start.setEnabled(false);
        stop.setEnabled(true);
        goal.setEnabled(false);
        useMemory.setEnabled(false);
        manageMemory.setEnabled(false);
        status.setText(R.string.agent_running);
        addEvent(getString(R.string.agent_user_event, task));
        worker.execute(() -> {
            AgentTaskRunner current = null;
            boolean completed = false;
            try {
                if (epoch != generation || request.isCancelled()) return;
                current = new AgentTaskRunner(getApplicationContext(), sessionId, new AgentTaskRunner.Listener() {
                    @Override public void onEvent(String text) {
                        onRunnerEvent(epoch, text);
                    }
                    @Override public String ask(String question, RequestCancellation signal) throws Exception {
                        return awaitAnswer(epoch, signal, null, question);
                    }
                    @Override public boolean confirm(String target, String command, RequestCancellation signal)
                            throws Exception {
                        return awaitAnswer(epoch, signal, target, command) != null;
                    }
                    @Override public char[] requestWebPassword(String username, RequestCancellation signal)
                            throws Exception {
                        return awaitWebPassword(epoch, signal, username);
                    }
                });
                runner = current;
                if (epoch != generation || request.isCancelled()) return;
                current.run(task, request);
                completed = !request.isCancelled();
            } catch (Exception ignored) {
                // Do not render raw exceptions: provider/network errors can contain URLs or credentials.
            } finally {
                if (current != null) {
                    try { current.close(); } catch (RuntimeException ignored) { /* UI must still leave busy state. */ }
                }
                if (runner == current) runner = null;
                final boolean success = completed;
                runOnUiThread(() -> {
                    if (epoch != generation || isFinishing() || isDestroyed()) return;
                    busy = false;
                    cancellation = null;
                    start.setEnabled(validSession);
                    stop.setEnabled(false);
                    goal.setEnabled(true);
                    useMemory.setEnabled(android.os.Build.VERSION.SDK_INT >= 23);
                    manageMemory.setEnabled(android.os.Build.VERSION.SDK_INT >= 23);
                    status.setText(success ? R.string.agent_finished : R.string.agent_failed);
                });
            }
        });
    }

    private boolean active(int epoch, RequestCancellation request) {
        return epoch == generation && !request.isCancelled() && !isFinishing() && !isDestroyed();
    }

    private void onRunnerEvent(int epoch, String text) {
        // Normal close cancels its signal too. Preserve already-emitted receipts, while user stop
        // increments generation immediately and still rejects every stale callback.
        runOnUiThread(() -> {
            if (epoch == generation && !isFinishing() && !isDestroyed()) addEvent(text);
        });
    }

    /** Worker blocks with a deadline, never the UI thread. Cancellation and stale callbacks fail closed. */
    private String awaitAnswer(int epoch, RequestCancellation request, String target, String text) throws Exception {
        CountDownLatch answerReady = new CountDownLatch(1);
        AtomicReference<String> answer = new AtomicReference<>();
        AtomicReference<AlertDialog> ownedDialog = new AtomicReference<>();
        AtomicBoolean settled = new AtomicBoolean();
        runOnUiThread(() -> {
            if (settled.get() || !active(epoch, request)) { answerReady.countDown(); return; }
            status.setText(R.string.agent_waiting);
            final boolean approval = target != null;
            final String safeTarget = safe(target);
            LinearLayout panel = new LinearLayout(this);
            panel.setOrientation(LinearLayout.VERTICAL);
            panel.setPadding(dp(24), dp(8), dp(24), dp(8));
            TextView details = new TextView(this);
            details.setText(approval ? getString(R.string.agent_risk_body, safeTarget, safe(text)) : safe(text));
            details.setTextIsSelectable(true);
            panel.addView(details);
            EditText entry = new EditText(this);
            entry.setHint(approval ? R.string.agent_type_target : R.string.agent_answer_hint);
            entry.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
            entry.setSingleLine(false);
            entry.setSaveEnabled(false);
            entry.setFilters(new android.text.InputFilter[] {new android.text.InputFilter.LengthFilter(2000)});
            if (android.os.Build.VERSION.SDK_INT >= 26) entry.setImportantForAutofill(View.IMPORTANT_FOR_AUTOFILL_NO);
            panel.addView(entry);
            ScrollView dialogScroll = new ScrollView(this);
            dialogScroll.addView(panel);
            AlertDialog dialog = new AlertDialog.Builder(this)
                    .setTitle(approval ? R.string.agent_risk_title : R.string.agent_question)
                    .setView(dialogScroll)
                    .setNegativeButton(R.string.agent_cancel, (d, which) -> answerReady.countDown())
                    .setPositiveButton(approval ? R.string.agent_confirm : R.string.agent_answer_send, null)
                    .create();
            pendingDialog = dialog;
            ownedDialog.set(dialog);
            dialog.setOnDismissListener(d -> answerReady.countDown());
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();
            if (dialog.getWindow() != null) dialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(view -> {
                if (!active(epoch, request)) { dialog.dismiss(); return; }
                String value = ansi.sanitize(entry.getText().toString()).trim();
                if (approval && (safeTarget.isEmpty() || !safeTarget.equals(value))) {
                    entry.setError(getString(R.string.agent_target_mismatch)); return;
                }
                if (value.isEmpty()) { entry.setError(getString(R.string.agent_required)); return; }
                if (!approval && containsSecret(value)) {
                    entry.setError(getString(R.string.agent_no_secrets)); return;
                }
                answer.set(value);
                if (!approval) addEvent(getString(R.string.agent_reply_event, safe(value)));
                answerReady.countDown();
                dialog.dismiss();
            });
        });
        long deadline = SystemClock.elapsedRealtime() + 120_000;
        try {
            while (SystemClock.elapsedRealtime() < deadline) {
                if (epoch != generation || request.isCancelled()) throw new CancellationException();
                if (answerReady.await(250, TimeUnit.MILLISECONDS)) {
                    if (epoch != generation || request.isCancelled()) throw new CancellationException();
                    if (answer.get() == null && target == null) throw new CancellationException();
                    return answer.get();
                }
            }
            throw new CancellationException("Response timed out");
        } finally {
            settled.set(true);
            runOnUiThread(() -> {
                AlertDialog dialog = ownedDialog.get();
                if (dialog != null && dialog.isShowing()) dialog.dismiss();
                if (pendingDialog == dialog) pendingDialog = null;
                if (active(epoch, request)) status.setText(R.string.agent_running);
            });
        }
    }

    private void stopTask() {
        if (!busy) return;
        ++generation;
        busy = false;
        final RequestCancellation request = cancellation;
        final AgentTaskRunner current = runner;
        cancellation = null;
        if (request != null) request.cancelAsync(control);
        if (pendingDialog != null) { pendingDialog.dismiss(); pendingDialog = null; }
        start.setEnabled(false);
        stop.setEnabled(false);
        goal.setEnabled(true);
        useMemory.setEnabled(android.os.Build.VERSION.SDK_INT >= 23);
        manageMemory.setEnabled(android.os.Build.VERSION.SDK_INT >= 23);
        // Re-enter via the terminal after stopping; never reuse a potentially partial CLI transaction.
        validSession = false;
        status.setText(R.string.agent_stopped);
        control.execute(() -> {
            if (current != null) current.close();
        });
    }

    /** Password never becomes a String, event, saved field or model message here. Ownership transfers to runner. */
    private char[] awaitWebPassword(int epoch, RequestCancellation request, String username) throws Exception {
        CountDownLatch ready = new CountDownLatch(1);
        AtomicReference<char[]> answer = new AtomicReference<>();
        AtomicReference<AlertDialog> ownedDialog = new AtomicReference<>();
        AtomicReference<EditText> ownedInput = new AtomicReference<>();
        AtomicBoolean settled = new AtomicBoolean();
        runOnUiThread(() -> {
            if (settled.get() || !active(epoch, request)) { ready.countDown(); return; }
            status.setText(R.string.agent_password_waiting);
            LinearLayout panel = new LinearLayout(this);
            panel.setOrientation(LinearLayout.VERTICAL);
            panel.setPadding(dp(24), dp(8), dp(24), dp(8));
            TextView notice = new TextView(this);
            notice.setText(getString(R.string.agent_password_notice, safe(username)));
            panel.addView(notice);
            EditText entry = new androidx.appcompat.widget.AppCompatEditText(this) {
                @Override public boolean onTextContextMenuItem(int id) {
                    if (id == android.R.id.copy || id == android.R.id.cut || id == android.R.id.selectAll) return false;
                    return super.onTextContextMenuItem(id);
                }
            };
            entry.setHint(R.string.agent_password_hint);
            entry.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD
                    | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
            entry.setTransformationMethod(android.text.method.PasswordTransformationMethod.getInstance());
            entry.setImeOptions(android.view.inputmethod.EditorInfo.IME_ACTION_DONE
                    | android.view.inputmethod.EditorInfo.IME_FLAG_NO_EXTRACT_UI
                    | android.view.inputmethod.EditorInfo.IME_FLAG_NO_PERSONALIZED_LEARNING);
            entry.setSaveEnabled(false);
            entry.setSaveFromParentEnabled(false);
            entry.setTextIsSelectable(false);
            entry.setLongClickable(false);
            entry.setFilters(new android.text.InputFilter[]{new android.text.InputFilter.LengthFilter(64)});
            if (android.os.Build.VERSION.SDK_INT >= 26) entry.setImportantForAutofill(View.IMPORTANT_FOR_AUTOFILL_NO);
            if (android.os.Build.VERSION.SDK_INT >= 30) entry.setImportantForContentCapture(View.IMPORTANT_FOR_CONTENT_CAPTURE_NO);
            panel.addView(entry);
            ownedInput.set(entry);
            AlertDialog dialog = new AlertDialog.Builder(this).setTitle(R.string.agent_password_title)
                    .setView(panel).setNegativeButton(R.string.agent_cancel, (d, which) -> ready.countDown())
                    .setPositiveButton(R.string.agent_password_continue, null).create();
            pendingDialog = dialog;
            ownedDialog.set(dialog);
            dialog.setOnDismissListener(d -> { clearSecretInput(entry); ready.countDown(); });
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();
            if (dialog.getWindow() != null) dialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(view -> {
                if (settled.get() || !active(epoch, request)) { dialog.dismiss(); return; }
                char[] candidate = new char[entry.length()];
                android.text.TextUtils.getChars(entry.getText(), 0, entry.length(), candidate, 0);
                if (!validWebPassword(candidate)) {
                    java.util.Arrays.fill(candidate, '\0');
                    entry.setError(getString(R.string.agent_password_invalid));
                    return;
                }
                synchronized (answer) {
                    if (settled.get() || !active(epoch, request)) {
                        java.util.Arrays.fill(candidate, '\0');
                        dialog.dismiss();
                        return;
                    }
                    answer.set(candidate);
                }
                clearSecretInput(entry);
                ready.countDown();
                dialog.dismiss();
            });
        });
        long deadline = SystemClock.elapsedRealtime() + 120_000;
        try {
            while (SystemClock.elapsedRealtime() < deadline) {
                if (epoch != generation || request.isCancelled()) throw new CancellationException();
                if (ready.await(250, TimeUnit.MILLISECONDS)) {
                    char[] result = answer.getAndSet(null);
                    if (result == null || epoch != generation || request.isCancelled()) {
                        if (result != null) java.util.Arrays.fill(result, '\0');
                        throw new CancellationException();
                    }
                    return result;
                }
            }
            throw new CancellationException("Local credential response timed out");
        } finally {
            synchronized (answer) {
                settled.set(true);
                char[] abandoned = answer.getAndSet(null);
                if (abandoned != null) java.util.Arrays.fill(abandoned, '\0');
            }
            runOnUiThread(() -> {
                EditText entry = ownedInput.get();
                if (entry != null) clearSecretInput(entry);
                AlertDialog dialog = ownedDialog.get();
                if (dialog != null && dialog.isShowing()) dialog.dismiss();
                if (pendingDialog == dialog) pendingDialog = null;
                if (active(epoch, request)) status.setText(R.string.agent_running);
            });
        }
    }

    private static void clearSecretInput(EditText entry) {
        android.text.Editable value = entry.getText();
        // Overwrite visible buffer before releasing it; Android/IME-internal copies are outside this guarantee.
        for (int index = 0; index < value.length(); index++) value.replace(index, index + 1, "\u0000");
        value.clear();
    }

    private static boolean validWebPassword(char[] value) {
        if (value.length < 8 || value.length > 64) return false;
        boolean letter = false;
        boolean digit = false;
        for (char character : value) {
            if (character < 33 || character > 126 || "\"'\\`;?".indexOf(character) >= 0) return false;
            letter |= (character >= 'A' && character <= 'Z') || (character >= 'a' && character <= 'z');
            digit |= character >= '0' && character <= '9';
        }
        return letter && digit;
    }

    private String safe(String text) {
        String value = redactor.redact(ansi.sanitize(text));
        return value.length() > 8000 ? value.substring(0, 8000) + "…" : value;
    }

    private boolean containsSecret(String value) {
        return redactor.containsSensitiveMaterial(value)
                || java.util.regex.Pattern.compile("(?:密码|口令|密钥|凭据)\\s*(?:是|为|[:：=])\\s*\\S+")
                        .matcher(value).find();
    }

    private void addEvent(String text) {
        MaterialCardView card = new MaterialCardView(this);
        card.setRadius(dp(14));
        card.setCardElevation(0);
        card.setCardBackgroundColor(MaterialColors.getColor(findViewById(R.id.agent_root),
                com.google.android.material.R.attr.colorSurfaceContainer));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2);
        params.bottomMargin = dp(10);
        card.setLayoutParams(params);
        TextView label = new TextView(this);
        label.setText(safe(text));
        label.setTextIsSelectable(true);
        label.setTextColor(MaterialColors.getColor(findViewById(R.id.agent_root),
                com.google.android.material.R.attr.colorOnSurface));
        label.setTextSize(14);
        label.setPadding(dp(14), dp(12), dp(14), dp(12));
        card.addView(label);
        while (events.getChildCount() >= 80) events.removeViewAt(0);
        events.addView(card);
        scroll.post(() -> scroll.fullScroll(View.FOCUS_DOWN));
    }

    private int dp(int value) { return Math.round(value * getResources().getDisplayMetrics().density); }

    @Override protected void onStop() { stopTask(); super.onStop(); }

    @Override protected void onDestroy() {
        stopTask();
        if (pendingDialog != null) pendingDialog.dismiss();
        worker.shutdownNow();
        control.shutdown();
        super.onDestroy();
    }
}
