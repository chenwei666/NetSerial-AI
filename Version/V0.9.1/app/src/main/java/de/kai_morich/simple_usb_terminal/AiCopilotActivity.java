package de.kai_morich.simple_usb_terminal;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;

import com.chenwei666.netserial.ai.AiChatConversation;
import com.chenwei666.netserial.ai.AiChatHistoryRepository;
import com.chenwei666.netserial.ai.AiChatHistoryState;
import com.chenwei666.netserial.ai.AiChatMessage;
import com.chenwei666.netserial.ai.AiChatProviderCandidate;
import com.chenwei666.netserial.ai.AiChatProviderFactory;
import com.chenwei666.netserial.ai.AiChatRequest;
import com.chenwei666.netserial.ai.AiChatResult;
import com.chenwei666.netserial.ai.AiChatRole;
import com.chenwei666.netserial.ai.AiFailoverException;
import com.chenwei666.netserial.ai.AiProviderError;
import com.chenwei666.netserial.ai.AiProviderException;
import com.chenwei666.netserial.ai.AiSuggestedCommand;
import com.chenwei666.netserial.ai.CredentialVaultFactory;
import com.chenwei666.netserial.ai.EncryptedAiChatHistoryRepository;
import com.chenwei666.netserial.ai.FailoverAiChatService;
import com.chenwei666.netserial.ai.ProviderCredentialService;
import com.chenwei666.netserial.ai.ProviderProfile;
import com.chenwei666.netserial.ai.ProviderProfileManager;
import com.chenwei666.netserial.ai.ProviderProfilesState;
import com.chenwei666.netserial.ai.RequestCancellation;
import com.chenwei666.netserial.ai.SharedPreferencesProviderProfilePersistence;
import com.chenwei666.netserial.ai.UnavailableCredentialVault;
import com.chenwei666.netserial.change.ChangeTask;
import com.chenwei666.netserial.change.ChangeTaskStore;
import com.chenwei666.netserial.device.DeviceProfile;
import com.chenwei666.netserial.device.DeviceProfileStore;
import com.chenwei666.netserial.memory.MemoryRecord;
import com.chenwei666.netserial.memory.MemoryScope;
import com.chenwei666.netserial.memory.MemoryVault;
import com.chenwei666.netserial.memory.SharedPreferencesMemoryPersistence;
import com.chenwei666.netserial.terminal.AnsiTextSanitizer;
import com.chenwei666.netserial.terminal.SensitiveTextRedactor;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Full in-app AI conversation UI. Device commands remain review-only drafts. */
public final class AiCopilotActivity extends ThemedActivity {
    public static final String EXTRA_TERMINAL_CONTEXT = "terminal_context";
    public static final String EXTRA_DIAGNOSTIC_TEXT = "diagnostic_text";
    public static final String EXTRA_SELECTED_COMMAND = "selected_command";
    private static final long MEMORY_RETENTION_MILLIS = 180L * 24L * 60L * 60L * 1000L;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final SensitiveTextRedactor redactor = new SensitiveTextRedactor();
    private final AnsiTextSanitizer ansi = new AnsiTextSanitizer();
    private EditText input;
    private CheckBox includeTerminal;
    private CheckBox remember;
    private Button send;
    private Button stop;
    private ProgressBar progress;
    private TextView status;
    private TextView providerLabel;
    private LinearLayout messageContainer;
    private ScrollView messageScroll;
    private DeviceProfile device;
    private String safeTerminalContext = "";
    private AiChatHistoryState history = AiChatHistoryState.empty();
    private AiChatHistoryRepository historyRepository;
    private RequestCancellation cancellation;
    private int requestGeneration;
    private boolean busy;

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
        setContentView(R.layout.activity_ai_copilot);
        Toolbar toolbar = findViewById(R.id.ai_copilot_toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(view -> finish());

        input = findViewById(R.id.ai_copilot_intent);
        includeTerminal = findViewById(R.id.ai_copilot_include_terminal);
        remember = findViewById(R.id.ai_copilot_remember);
        send = findViewById(R.id.ai_copilot_generate);
        stop = findViewById(R.id.ai_chat_stop);
        progress = findViewById(R.id.ai_copilot_progress);
        status = findViewById(R.id.ai_copilot_status);
        providerLabel = findViewById(R.id.ai_chat_provider);
        messageContainer = findViewById(R.id.ai_chat_messages);
        messageScroll = findViewById(R.id.ai_chat_scroll);

        device = new DeviceProfileStore(this).load();
        safeTerminalContext = sanitize(getIntent().getStringExtra(EXTRA_TERMINAL_CONTEXT), 12_000);
        TextView context = findViewById(R.id.ai_copilot_context);
        context.setText(getString(R.string.ai_copilot_device_context, device.getName(),
                device.getVendor().name(), device.getCliMode().name()));
        initializeHistory();
        ensureConversation();
        refreshProviderLabel();
        renderConversation();

        String diagnostic = sanitize(getIntent().getStringExtra(EXTRA_DIAGNOSTIC_TEXT), 8_000);
        if (!diagnostic.isEmpty()) {
            input.setText(getString(R.string.ai_diagnostic_prompt, diagnostic));
            includeTerminal.setChecked(true);
        }
        send.setOnClickListener(view -> sendMessage());
        stop.setOnClickListener(view -> cancelActiveRequest());
    }

    @Override public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_ai_chat, menu);
        return true;
    }

    @Override public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.ai_chat_new) newConversation();
        else if (id == R.id.ai_chat_history) showHistory();
        else if (id == R.id.ai_chat_rename) renameConversation();
        else if (id == R.id.ai_chat_retry) retryLastMessage();
        else if (id == R.id.ai_chat_share) shareConversation();
        else if (id == R.id.ai_chat_delete) deleteConversation();
        else if (id == R.id.ai_chat_settings) startActivity(new Intent(this, AiProviderSettingsActivity.class));
        else return super.onOptionsItemSelected(item);
        return true;
    }

    private void sendMessage() {
        if (busy) return;
        String content = sanitize(input.getText().toString(), 8_000);
        if (content.isEmpty()) {
            status.setText(R.string.ai_copilot_intent_required);
            return;
        }
        AiChatConversation conversation = ensureConversation();
        AiChatMessage user = new AiChatMessage(AiChatRole.USER, content, System.currentTimeMillis());
        conversation = conversation.append(user);
        history = history.upsert(conversation);
        input.setText("");
        persistHistory();
        renderConversation();
        submitChat(conversation.getMessages(), content);
    }

    private void retryLastMessage() {
        if (busy) return;
        AiChatConversation conversation = ensureConversation();
        List<AiChatMessage> messages = conversation.getMessages();
        int lastUser = -1;
        for (int index = messages.size() - 1; index >= 0; index--) {
            if (messages.get(index).getRole() == AiChatRole.USER) {
                lastUser = index;
                break;
            }
        }
        if (lastUser < 0) {
            status.setText(R.string.ai_chat_nothing_to_retry);
            return;
        }
        List<AiChatMessage> requestMessages = new ArrayList<>(messages.subList(0, lastUser + 1));
        submitChat(requestMessages, messages.get(lastUser).getContent());
    }

    private void submitChat(List<AiChatMessage> requestMessages, String memoryIntent) {
        List<ProviderProfile> profiles;
        try {
            profiles = orderedProfiles(new ProviderProfileManager(
                    new SharedPreferencesProviderProfilePersistence(this)).load());
        } catch (RuntimeException exception) {
            status.setText(R.string.ai_copilot_profile_required);
            return;
        }
        if (profiles.isEmpty()) {
            status.setText(R.string.ai_copilot_profile_required);
            return;
        }
        String memoryContext = recallMemory();
        String terminalContext = includeTerminal.isChecked() ? safeTerminalContext : "";
        String changeContext = activeChangeContext();
        if (!changeContext.isEmpty()) memoryContext += changeContext;
        AiChatRequest request = new AiChatRequest(requestMessages, device.getVendor(), device.getCliMode(),
                device.getName(), terminalContext, memoryContext, responseLanguage());
        boolean saveMemory = remember.isChecked();
        int generation = ++requestGeneration;
        cancellation = new RequestCancellation();
        setBusy(true);
        status.setText(R.string.ai_chat_thinking);
        RequestCancellation activeCancellation = cancellation;
        executor.submit(() -> executeChat(profiles, request, memoryIntent, saveMemory,
                generation, activeCancellation));
    }

    private void executeChat(List<ProviderProfile> profiles, AiChatRequest request,
                             String memoryIntent, boolean saveMemory, int generation,
                             RequestCancellation activeCancellation) {
        try {
            ProviderCredentialService credentials = new ProviderCredentialService(
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                            ? CredentialVaultFactory.create(this)
                            : new UnavailableCredentialVault());
            List<AiChatProviderCandidate> candidates = new ArrayList<>();
            for (ProviderProfile profile : profiles) {
                candidates.add(new AiChatProviderCandidate(
                        profile.getProviderId() + " / " + profile.getModel(),
                        AiChatProviderFactory.create(profile, credentials)));
            }
            AiChatResult result = new FailoverAiChatService(candidates, 2).chat(request, activeCancellation);
            if (saveMemory) saveMemory(memoryIntent);
            runOnUiThread(() -> completeChat(generation, result));
        } catch (Exception exception) {
            runOnUiThread(() -> failChat(generation, exception));
        }
    }

    private void completeChat(int generation, AiChatResult result) {
        if (generation != requestGeneration || isFinishing() || isDestroyed()) return;
        AiChatConversation conversation = ensureConversation().append(new AiChatMessage(
                AiChatRole.ASSISTANT, result.getResponse().getContent(), System.currentTimeMillis()));
        history = history.upsert(conversation);
        persistHistory();
        setBusy(false);
        providerLabel.setText(getString(R.string.ai_chat_provider_used, result.getProviderAlias(),
                result.getAttempts().size()));
        status.setText(R.string.ai_chat_complete);
        renderConversation();
    }

    private void failChat(int generation, Exception exception) {
        if (generation != requestGeneration || isFinishing() || isDestroyed()) return;
        setBusy(false);
        AiProviderException provider = findProviderException(exception);
        if (provider != null && provider.getError() == AiProviderError.CANCELLED) {
            status.setText(R.string.ai_chat_cancelled);
        } else if (provider != null) {
            status.setText(errorMessage(provider.getError()));
        } else {
            status.setText(R.string.ai_copilot_failed);
        }
    }

    private void cancelActiveRequest() {
        if (!busy) return;
        requestGeneration++;
        if (cancellation != null) cancellation.cancel();
        setBusy(false);
        status.setText(R.string.ai_chat_cancelled);
    }

    private void renderConversation() {
        new AiChatMessageRenderer(this, device, new AiChatMessageRenderer.Actions() {
            @Override public void copy(String text) {
                copyText(text);
            }

            @Override public void reviewCommand(AiSuggestedCommand command) {
                returnCommand(command);
            }
        }).render(ensureConversation(), messageContainer, messageScroll);
    }

    private void returnCommand(AiSuggestedCommand suggestion) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.ai_copilot_load_command)
                .setMessage(suggestion.getCommand() + "\n\n" + suggestion.getRisk().name()
                        + "\n" + getString(R.string.ai_chat_command_review))
                .setNegativeButton(android.R.string.cancel, null)
                .setNeutralButton(R.string.ai_chat_copy, (dialog, which) -> copyText(suggestion.getCommand()))
                .setPositiveButton(R.string.ai_copilot_load_only, (dialog, which) -> {
                    setResult(RESULT_OK, new Intent().putExtra(EXTRA_SELECTED_COMMAND, suggestion.getCommand()));
                    finish();
                }).show();
    }

    private void initializeHistory() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            remember.setChecked(false);
            remember.setEnabled(false);
            status.setText(R.string.ai_chat_history_unavailable);
            return;
        }
        try {
            historyRepository = new EncryptedAiChatHistoryRepository(CredentialVaultFactory.create(this));
            history = historyRepository.load();
        } catch (RuntimeException exception) {
            historyRepository = null;
            history = AiChatHistoryState.empty();
            remember.setChecked(false);
            status.setText(R.string.ai_chat_history_failed);
        }
    }

    private AiChatConversation ensureConversation() {
        AiChatConversation active = history.active();
        if (active != null) return active;
        long now = System.currentTimeMillis();
        AiChatConversation created = AiChatConversation.create(
                UUID.randomUUID().toString(), device.getName(), now);
        history = history.upsert(created);
        return created;
    }

    private void persistHistory() {
        if (!remember.isChecked() || historyRepository == null) return;
        try {
            historyRepository.save(history);
        } catch (RuntimeException exception) {
            status.setText(R.string.ai_chat_history_failed);
        }
    }

    private void newConversation() {
        if (busy) cancelActiveRequest();
        long now = System.currentTimeMillis();
        history = history.upsert(AiChatConversation.create(UUID.randomUUID().toString(),
                device.getName(), now));
        persistHistory();
        renderConversation();
        status.setText(R.string.ai_chat_ready);
    }

    private void showHistory() {
        List<AiChatConversation> values = history.getConversations();
        if (values.isEmpty()) {
            status.setText(R.string.ai_chat_history_empty);
            return;
        }
        String[] labels = new String[values.size()];
        DateFormat format = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
        for (int index = 0; index < values.size(); index++) {
            AiChatConversation item = values.get(index);
            labels[index] = item.getTitle() + "\n" + format.format(item.getUpdatedAtMillis());
        }
        new AlertDialog.Builder(this).setTitle(R.string.ai_chat_history)
                .setItems(labels, (dialog, which) -> {
                    if (busy) cancelActiveRequest();
                    history = history.select(values.get(which).getId());
                    persistHistory();
                    renderConversation();
                }).show();
    }

    private void renameConversation() {
        AiChatConversation current = ensureConversation();
        EditText title = new EditText(this);
        title.setText(current.getTitle());
        title.setSingleLine(true);
        title.setMaxLines(1);
        new AlertDialog.Builder(this).setTitle(R.string.ai_chat_rename)
                .setView(title).setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                    try {
                        history = history.upsert(current.rename(title.getText().toString(),
                                System.currentTimeMillis()));
                        persistHistory();
                    } catch (RuntimeException exception) {
                        status.setText(R.string.ai_chat_title_invalid);
                    }
                }).show();
    }

    private void deleteConversation() {
        AiChatConversation current = ensureConversation();
        new AlertDialog.Builder(this).setTitle(R.string.ai_chat_delete)
                .setMessage(R.string.ai_chat_delete_confirm)
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(R.string.ai_chat_delete, (dialog, which) -> {
                    if (busy) cancelActiveRequest();
                    history = history.delete(current.getId());
                    ensureConversation();
                    persistHistory();
                    renderConversation();
                }).show();
    }

    private void shareConversation() {
        AiChatConversation current = ensureConversation();
        if (current.getMessages().isEmpty()) {
            status.setText(R.string.ai_chat_history_empty);
            return;
        }
        StringBuilder text = new StringBuilder(current.getTitle()).append("\n\n");
        for (AiChatMessage message : current.getMessages()) {
            text.append(message.getRole() == AiChatRole.USER ? "User" : "AI")
                    .append(":\n").append(redactor.redact(message.getContent())).append("\n\n");
        }
        startActivity(Intent.createChooser(new Intent(Intent.ACTION_SEND).setType("text/plain")
                .putExtra(Intent.EXTRA_SUBJECT, current.getTitle())
                .putExtra(Intent.EXTRA_TEXT, text.toString()), getString(R.string.ai_chat_share)));
    }

    private void refreshProviderLabel() {
        try {
            ProviderProfilesState state = new ProviderProfileManager(
                    new SharedPreferencesProviderProfilePersistence(this)).load();
            for (ProviderProfile profile : state.getProfiles()) {
                if (state.isActive(profile)) {
                    providerLabel.setText(getString(R.string.ai_chat_active_provider,
                            profile.getProviderId(), profile.getModel()));
                    return;
                }
            }
        } catch (RuntimeException ignored) {
            // A damaged profile is surfaced by the send path without exposing details.
        }
        providerLabel.setText(R.string.ai_chat_no_provider);
    }

    private String recallMemory() {
        try {
            List<MemoryRecord> records = new MemoryVault(
                    new SharedPreferencesMemoryPersistence(this)).recall(
                    MemoryScope.DEVICE, device.getName(), 5, System.currentTimeMillis());
            StringBuilder builder = new StringBuilder();
            for (MemoryRecord record : records) {
                if (builder.length() > 0) builder.append('\n');
                builder.append("- ").append(record.getContent());
            }
            return builder.toString();
        } catch (RuntimeException exception) {
            return "";
        }
    }

    private void saveMemory(String intent) {
        try {
            long now = System.currentTimeMillis();
            new MemoryVault(new SharedPreferencesMemoryPersistence(this)).add(
                    MemoryRecord.userVerified(MemoryScope.DEVICE, device.getName(),
                            "AI conversation: " + sanitize(intent, 2_000), now,
                            now + MEMORY_RETENTION_MILLIS));
        } catch (RuntimeException ignored) {
            // Chat remains successful when optional local memory reaches its policy limit.
        }
    }

    private String activeChangeContext() {
        ChangeTask task = new ChangeTaskStore(this).loadActive();
        if (task == null || !task.isAuthorizedAt(System.currentTimeMillis(), device.getName())) return "";
        return "\nApproved active change task (user-authored reference data):\n"
                + "Ticket: " + task.getTicketNumber() + "\nGoal: " + task.getGoal()
                + "\nPrecheck: " + task.getPrecheckPlan() + "\nPlanned: " + task.getCommandPlan()
                + "\nVerification: " + task.getVerificationPlan() + "\nRollback: " + task.getRollbackPlan();
    }

    private String responseLanguage() {
        return Locale.getDefault().getLanguage().startsWith("zh") ? "Simplified Chinese" : "English";
    }

    private static List<ProviderProfile> orderedProfiles(ProviderProfilesState state) {
        List<ProviderProfile> result = new ArrayList<>();
        for (ProviderProfile profile : state.getProfiles()) if (state.isActive(profile)) result.add(profile);
        for (ProviderProfile profile : state.getProfiles()) if (!state.isActive(profile)) result.add(profile);
        return result;
    }

    private void setBusy(boolean value) {
        busy = value;
        progress.setVisibility(value ? View.VISIBLE : View.GONE);
        send.setEnabled(!value);
        stop.setEnabled(value);
        input.setEnabled(!value);
    }

    private void copyText(String value) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        clipboard.setPrimaryClip(ClipData.newPlainText("NetSerial AI", redactor.redact(value)));
        Toast.makeText(this, R.string.ai_chat_copied, Toast.LENGTH_SHORT).show();
    }

    private int errorMessage(AiProviderError error) {
        switch (error) {
            case AUTHENTICATION: return R.string.ai_test_error_authentication;
            case RATE_LIMIT: return R.string.ai_test_error_rate_limit;
            case TIMEOUT: return R.string.ai_test_error_timeout;
            case TLS: return R.string.ai_test_error_tls;
            case NETWORK: return R.string.ai_test_error_network;
            case SERVER: return R.string.ai_test_error_server;
            case RESPONSE_TOO_LARGE: return R.string.ai_test_error_response_too_large;
            case INVALID_RESPONSE: return R.string.ai_test_error_response;
            default: return R.string.ai_test_error_unknown;
        }
    }

    private static AiProviderException findProviderException(Throwable value) {
        Throwable current = value;
        for (int depth = 0; current != null && depth < 8; depth++, current = current.getCause()) {
            if (current instanceof AiProviderException) return (AiProviderException) current;
            if (current instanceof AiFailoverException && current.getCause() == null) break;
        }
        return null;
    }

    private String sanitize(String value, int limit) {
        String safe = redactor.redact(ansi.sanitize(value == null ? "" : value)).trim();
        return safe.length() <= limit ? safe : safe.substring(safe.length() - limit);
    }

    @Override protected void onResume() {
        super.onResume();
        if (providerLabel != null) refreshProviderLabel();
    }

    @Override protected void onDestroy() {
        if (cancellation != null) cancellation.cancel();
        executor.shutdownNow();
        super.onDestroy();
    }
}
