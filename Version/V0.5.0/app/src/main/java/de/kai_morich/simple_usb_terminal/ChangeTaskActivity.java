package de.kai_morich.simple_usb_terminal;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.chenwei666.netserial.change.ChangeEvidenceFormatter;
import com.chenwei666.netserial.change.ChangePdfExporter;
import com.chenwei666.netserial.change.ChangeTask;
import com.chenwei666.netserial.change.ChangeTaskStatus;
import com.chenwei666.netserial.change.ChangeTaskStore;
import com.chenwei666.netserial.device.DeviceProfile;
import com.chenwei666.netserial.device.DeviceProfileStore;
import com.chenwei666.netserial.settings.AppLocaleController;

import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.UUID;

public final class ChangeTaskActivity extends ThemedActivity {
    private static final int EXPORT_REQUEST = 5401;
    private static final int EXPORT_PDF_REQUEST = 5402;
    private ChangeTaskStore store;
    private EditText ticket;
    private EditText site;
    private EditText device;
    private EditText operator;
    private EditText duration;
    private EditText goal;
    private EditText precheck;
    private EditText commands;
    private EditText verification;
    private EditText rollback;
    private TextView status;

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
        setContentView(R.layout.activity_change_task);
        Toolbar toolbar = findViewById(R.id.change_toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(view -> finish());
        store = new ChangeTaskStore(this);
        bindViews();
        findViewById(R.id.change_start).setOnClickListener(view -> startTask());
        findViewById(R.id.change_complete).setOnClickListener(view -> closeTask(true));
        findViewById(R.id.change_cancel).setOnClickListener(view -> closeTask(false));
        findViewById(R.id.change_export).setOnClickListener(view -> exportTask());
        findViewById(R.id.change_export_pdf).setOnClickListener(view -> exportPdf());
        render();
    }

    private void bindViews() {
        ticket = findViewById(R.id.change_ticket);
        site = findViewById(R.id.change_site);
        device = findViewById(R.id.change_device);
        operator = findViewById(R.id.change_operator);
        duration = findViewById(R.id.change_duration);
        goal = findViewById(R.id.change_goal);
        precheck = findViewById(R.id.change_precheck);
        commands = findViewById(R.id.change_commands);
        verification = findViewById(R.id.change_verification);
        rollback = findViewById(R.id.change_rollback);
        status = findViewById(R.id.change_status);
    }

    private void render() {
        ChangeTask task = store.load();
        if (task == null) {
            DeviceProfile profile = new DeviceProfileStore(this).load();
            device.setText(profile.getName());
            duration.setText(R.string.change_default_duration);
            status.setText(R.string.change_no_task);
            return;
        }
        ticket.setText(task.getTicketNumber());
        site.setText(task.getSite());
        device.setText(task.getDeviceName());
        operator.setText(task.getOperatorName());
        duration.setText(String.valueOf(Math.max(1,
                (task.getWindowEndMillis() - task.getWindowStartMillis()) / 60_000L)));
        goal.setText(task.getGoal());
        precheck.setText(task.getPrecheckPlan());
        commands.setText(task.getCommandPlan());
        verification.setText(task.getVerificationPlan());
        rollback.setText(task.getRollbackPlan());
        status.setText(getString(R.string.change_status_format, task.getStatus().name(),
                task.getEvents().size()));
    }

    private void startTask() {
        try {
            if (store.loadActive() != null) throw new IllegalStateException("complete or cancel the active task first");
            int minutes = Integer.parseInt(duration.getText().toString().trim());
            if (minutes < 5 || minutes > 24 * 60) throw new IllegalArgumentException();
            long now = System.currentTimeMillis();
            ChangeTask task = new ChangeTask(UUID.randomUUID().toString(), ticket.getText().toString(),
                    site.getText().toString(), device.getText().toString(), operator.getText().toString(),
                    goal.getText().toString(), precheck.getText().toString(), commands.getText().toString(),
                    verification.getText().toString(), rollback.getText().toString(), now,
                    now + minutes * 60_000L, ChangeTaskStatus.DRAFT, new ArrayList<>()).start(now);
            store.save(task);
            status.setText(R.string.change_started);
            render();
        } catch (RuntimeException exception) {
            status.setText(R.string.change_invalid);
        }
    }

    private void closeTask(boolean completed) {
        try {
            ChangeTask task = store.load();
            if (task == null) throw new IllegalStateException();
            store.save(completed ? task.complete(System.currentTimeMillis())
                    : task.cancel(System.currentTimeMillis()));
            render();
        } catch (RuntimeException exception) {
            status.setText(R.string.change_close_failed);
        }
    }

    private void exportTask() {
        if (store.load() == null) {
            status.setText(R.string.change_no_task);
            return;
        }
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT).setType("text/markdown")
                .putExtra(Intent.EXTRA_TITLE, "netserial-change-evidence.md");
        startActivityForResult(intent, EXPORT_REQUEST);
    }

    private void exportPdf() {
        if (store.load() == null) {
            status.setText(R.string.change_no_task);
            return;
        }
        startActivityForResult(new Intent(Intent.ACTION_CREATE_DOCUMENT).setType("application/pdf")
                .putExtra(Intent.EXTRA_TITLE, "netserial-change-evidence.pdf"), EXPORT_PDF_REQUEST);
    }

    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if ((requestCode != EXPORT_REQUEST && requestCode != EXPORT_PDF_REQUEST)
                || resultCode != RESULT_OK || data == null
                || data.getData() == null) return;
        if (requestCode == EXPORT_PDF_REQUEST) writePdf(data.getData());
        else writeEvidence(data.getData());
    }

    private void writeEvidence(Uri uri) {
        try (OutputStream output = getContentResolver().openOutputStream(uri)) {
            ChangeTask task = store.load();
            if (output == null || task == null) throw new IllegalStateException();
            output.write(new ChangeEvidenceFormatter().toMarkdown(task).getBytes(StandardCharsets.UTF_8));
            status.setText(R.string.change_exported);
        } catch (Exception exception) {
            status.setText(R.string.change_export_failed);
        }
    }

    private void writePdf(Uri uri) {
        try (OutputStream output = getContentResolver().openOutputStream(uri)) {
            ChangeTask task = store.load();
            if (output == null || task == null) throw new IllegalStateException();
            new ChangePdfExporter().write(task, output);
            status.setText(R.string.change_exported);
        } catch (Exception exception) {
            status.setText(R.string.change_export_failed);
        }
    }
}
