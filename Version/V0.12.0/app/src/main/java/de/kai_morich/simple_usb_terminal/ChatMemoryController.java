package de.kai_morich.simple_usb_terminal;

import android.app.Activity;
import android.os.Build;
import android.text.InputType;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import com.chenwei666.netserial.memory.ConfirmedMemoryStore;
import com.chenwei666.netserial.memory.EncryptedConfirmedMemoryPersistence;
import com.chenwei666.netserial.memory.MemoryRecord;
import com.chenwei666.netserial.memory.MemoryScope;
import java.util.List;

/** Confirmation is the only write entry point. Opening, recalling and viewing never save content. */
public final class ChatMemoryController {
    private final Activity activity;
    private final String subjectId;
    private final Runnable onChanged;
    private ConfirmedMemoryStore store;

    public ChatMemoryController(Activity activity, String subjectId, Runnable onChanged) {
        this.activity = activity;
        this.subjectId = subjectId == null ? "" : subjectId.trim();
        this.onChanged = onChanged;
    }

    private ConfirmedMemoryStore store() {
        if (store == null) store = new ConfirmedMemoryStore(new EncryptedConfirmedMemoryPersistence(activity));
        return store;
    }

    /** Capture exactly once before sending; the caller owns the resulting immutable snapshot. */
    public List<MemoryRecord> recall() {
        return store().recall(subjectId, 10, System.currentTimeMillis());
    }

    public void showManager() {
        if (!supported()) return;
        try {
            List<MemoryRecord> records = store().list(subjectId, System.currentTimeMillis());
            AlertDialog.Builder builder = new AlertDialog.Builder(activity)
                    .setTitle(R.string.chat_memory_title)
                    .setPositiveButton(R.string.chat_memory_add, (dialog, which) -> confirm(""))
                    .setNegativeButton(android.R.string.cancel, null);
            if (records.isEmpty()) builder.setMessage(R.string.chat_memory_empty);
            else {
                String[] labels = new String[records.size()];
                for (int i = 0; i < records.size(); i++) labels[i] = label(records.get(i));
                builder.setItems(labels, (dialog, index) -> actions(records.get(index)));
            }
            secure(builder.create());
        } catch (RuntimeException error) { failure(); }
    }

    /** Uses the supplied send-time snapshot, not the current storage state. */
    public void showUsed(List<MemoryRecord> snapshot) {
        StringBuilder content = new StringBuilder(activity.getString(R.string.chat_memory_used_hint));
        if (snapshot == null || snapshot.isEmpty()) content.append("\n\n").append(activity.getString(R.string.chat_memory_used_empty));
        else for (MemoryRecord record : snapshot) content.append("\n\n• ").append(label(record));
        secure(new AlertDialog.Builder(activity).setTitle(R.string.chat_memory_used)
                .setMessage(content.toString()).setPositiveButton(android.R.string.ok, null).create());
    }

    public void confirm(String draft) { edit(null, draft == null ? "" : draft); }

    private void actions(MemoryRecord record) {
        secure(new AlertDialog.Builder(activity).setTitle(R.string.chat_memory_title)
                .setMessage(label(record))
                .setPositiveButton(R.string.chat_memory_edit, (dialog, which) -> edit(record, record.getContent()))
                .setNeutralButton(R.string.chat_memory_delete, (dialog, which) -> secure(
                        new AlertDialog.Builder(activity).setTitle(R.string.chat_memory_delete_title)
                                .setMessage(R.string.chat_memory_delete_hint)
                                .setNegativeButton(android.R.string.cancel, null)
                                .setPositiveButton(R.string.chat_memory_delete, (confirmation, button) -> {
                                    try {
                                        store().delete(record.getId(), subjectId);
                                        changed();
                                        showManager();
                                    } catch (RuntimeException error) { failure(); }
                                }).create()))
                .setNegativeButton(android.R.string.cancel, null).create());
    }

    private void edit(MemoryRecord existing, String draft) {
        if (!supported()) return;
        LinearLayout layout = new LinearLayout(activity);
        layout.setOrientation(LinearLayout.VERTICAL);
        int padding = Math.round(20 * activity.getResources().getDisplayMetrics().density);
        layout.setPadding(padding, padding / 2, padding, 0);
        TextView hint = new TextView(activity);
        hint.setText(R.string.chat_memory_confirmation_hint);
        layout.addView(hint);
        Spinner scope = new Spinner(activity);
        String[] choices = subjectId.isEmpty()
                ? new String[] { activity.getString(R.string.chat_memory_global) }
                : new String[] { activity.getString(R.string.chat_memory_global), activity.getString(R.string.chat_memory_device) + " · " + subjectId };
        ArrayAdapter<String> adapter = new ArrayAdapter<>(activity, android.R.layout.simple_spinner_item, choices);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        scope.setAdapter(adapter);
        if (existing != null && existing.getScope() == MemoryScope.DEVICE && choices.length > 1) scope.setSelection(1);
        layout.addView(scope);
        EditText input = new EditText(activity);
        input.setSaveEnabled(false);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE
                | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        input.setMinLines(3);
        input.setMaxLines(7);
        input.setHint(R.string.chat_memory_content_hint);
        input.setText(draft); // Do not silently truncate long drafts; saving reports a validation error.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            input.setImportantForAutofill(android.view.View.IMPORTANT_FOR_AUTOFILL_NO_EXCLUDE_DESCENDANTS);
        layout.addView(input);
        AlertDialog dialog = new AlertDialog.Builder(activity)
                .setTitle(existing == null ? R.string.chat_memory_confirm : R.string.chat_memory_edit)
                .setView(layout).setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(R.string.chat_memory_save, null).create();
        secure(dialog);
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(view -> {
            String content = input.getText().toString();
            if (content.trim().isEmpty() || content.trim().length() > ConfirmedMemoryStore.MAX_CONTENT) {
                input.setError(activity.getString(R.string.chat_memory_length_error));
                return;
            }
            try {
                store().save(existing == null ? null : existing.getId(),
                        scope.getSelectedItemPosition() == 1 ? MemoryScope.DEVICE : MemoryScope.GLOBAL,
                        subjectId, content, System.currentTimeMillis());
                input.setText("");
                dialog.dismiss();
                changed();
                Toast.makeText(activity, R.string.chat_memory_saved, Toast.LENGTH_SHORT).show();
            } catch (IllegalArgumentException error) {
                input.setError(activity.getString(R.string.chat_memory_rejected));
            } catch (RuntimeException error) { failure(); }
        });
        dialog.setOnDismissListener(ignored -> input.setText(""));
    }

    private String label(MemoryRecord record) {
        String scope = activity.getString(record.getScope() == MemoryScope.GLOBAL
                ? R.string.chat_memory_global : R.string.chat_memory_device);
        String expiry = android.text.format.DateFormat.getDateFormat(activity).format(new java.util.Date(record.getExpiresAt()));
        return scope + " · " + activity.getString(R.string.chat_memory_expiry, expiry) + "\n" + record.getContent();
    }

    private void changed() { if (onChanged != null) onChanged.run(); }
    private boolean supported() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) return true;
        secure(new AlertDialog.Builder(activity).setTitle(R.string.chat_memory_title)
                .setMessage(R.string.chat_memory_unsupported).setPositiveButton(android.R.string.ok, null).create());
        return false;
    }
    private void failure() {
        Toast.makeText(activity, R.string.chat_memory_failure, Toast.LENGTH_LONG).show();
    }
    private void secure(AlertDialog dialog) {
        if (dialog.getWindow() != null) dialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
        dialog.show();
    }
}
