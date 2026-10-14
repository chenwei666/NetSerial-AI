package de.kai_morich.simple_usb_terminal;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.chenwei666.netserial.device.DeviceFingerprint;
import com.chenwei666.netserial.device.DeviceFingerprintEngine;
import com.chenwei666.netserial.device.Vendor;
import com.chenwei666.netserial.guided.DiagnosticFinding;
import com.chenwei666.netserial.guided.GuidedReportFormatter;
import com.chenwei666.netserial.guided.GuidedScenario;
import com.chenwei666.netserial.guided.network.NetworkDiagnosisRunner;
import com.chenwei666.netserial.guided.switches.GuideCommand;
import com.chenwei666.netserial.guided.switches.SwitchGuideEngine;
import com.chenwei666.netserial.terminal.ActiveTerminalSessionStore;
import com.chenwei666.netserial.terminal.ActiveTerminalSnapshot;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputLayout;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/** User-initiated checks with explicit scope; never executes a switch command. */
public final class BeginnerGuideActivity extends ThemedActivity {
    public static final String EXTRA_SCENARIO = "guided_scenario";
    public static final String EXTRA_RESUME = "guided_resume";
    public static final String EXTRA_RETURN_REMOTE = "guided_return_remote";
    private static final Vendor[] VENDORS = { Vendor.GENERIC, Vendor.H3C_COMWARE,
            Vendor.HUAWEI_VRP, Vendor.CISCO_IOS, Vendor.RUIJIE_RGOS };
    private final ExecutorService worker = Executors.newSingleThreadExecutor();
    private final GuidedReportFormatter formatter = new GuidedReportFormatter();
    private final SwitchGuideEngine switches = new SwitchGuideEngine();
    private GuidedScenario scenario;
    private EditText target, port, parameter, evidence;
    private Spinner vendor;
    private TextView progressText, summary, session;
    private ProgressBar progress;
    private LinearLayout results, commands;
    private BeginnerResultRenderer renderer;
    private NetworkDiagnosisRunner runner;
    private Future<?> job;
    private boolean busy, english, importing;
    private int generation;
    private int observedVendor;
    private String report = "", source = "";

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        if (state == null && getIntent().getBooleanExtra(EXTRA_RESUME, false)) {
            state = BeginnerGuideDraftStore.peek();
        } else if (state == null) {
            BeginnerGuideDraftStore.clear();
        }
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
        setContentView(R.layout.activity_beginner_guide);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.guide_root), (view, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            Insets ime = insets.getInsets(WindowInsetsCompat.Type.ime());
            view.setPadding(bars.left, bars.top, bars.right, Math.max(bars.bottom, ime.bottom));
            return insets;
        });
        scenario = GuidedScenario.fromId(getIntent().getStringExtra(EXTRA_SCENARIO));
        english = !getResources().getConfiguration().locale.getLanguage().equals("zh");
        renderer = new BeginnerResultRenderer(this, english);
        Toolbar toolbar = findViewById(R.id.guide_toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(BeginnerTaskCatalog.title(scenario));
        }
        toolbar.setNavigationOnClickListener(view -> finish());
        target = findViewById(R.id.guide_target);
        port = findViewById(R.id.guide_port);
        parameter = findViewById(R.id.guide_parameter);
        evidence = findViewById(R.id.guide_evidence);
        vendor = findViewById(R.id.guide_vendor);
        session = findViewById(R.id.guide_session);
        progressText = findViewById(R.id.guide_progress_text);
        summary = findViewById(R.id.guide_summary);
        progress = findViewById(R.id.guide_progress);
        results = findViewById(R.id.guide_results);
        commands = findViewById(R.id.guide_commands);
        TextView description = findViewById(R.id.guide_description);
        description.setText(BeginnerTaskCatalog.description(scenario));
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item,
                new String[] {getString(R.string.guide_vendor_unknown), "H3C / Comware",
                        "Huawei / VRP", "Cisco / IOS", "Ruijie / RGOS"});
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        vendor.setAdapter(adapter);
        setupScenario();
        if (state != null) {
            target.setText(state.getString("target", ""));
            port.setText(state.getString("port", port.getText().toString()));
            parameter.setText(state.getString("parameter", ""));
            evidence.setText(state.getString("evidence", ""));
            source = state.getString("source", "");
            vendor.setSelection(Math.max(0, Math.min(VENDORS.length - 1, state.getInt("vendor", 0))));
            note(R.string.guide_restored);
        }
        watch(target); watch(port); watch(parameter); watch(evidence);
        observedVendor = vendor.getSelectedItemPosition();
        vendor.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position != observedVendor) {
                    invalidateResult();
                    commands.removeAllViews();
                }
                observedVendor = position;
            }
            @Override public void onNothingSelected(AdapterView<?> parent) { }
        });
        findViewById(R.id.guide_start).setOnClickListener(view -> start());
        findViewById(R.id.guide_cancel).setOnClickListener(view -> cancel());
        findViewById(R.id.guide_capture).setOnClickListener(view -> capture());
        findViewById(R.id.guide_connect).setOnClickListener(view -> navigate("connections"));
        findViewById(R.id.guide_prepare).setOnClickListener(view -> prepareCommands());
        findViewById(R.id.guide_professional).setOnClickListener(view -> professional());
        findViewById(R.id.guide_ai).setOnClickListener(view -> ai(report));
        findViewById(R.id.guide_share).setOnClickListener(view -> review(report, false, () ->
                startActivity(Intent.createChooser(new Intent(Intent.ACTION_SEND).setType("text/plain")
                        .putExtra(Intent.EXTRA_TEXT, report), getString(R.string.guide_share)))));
        findViewById(R.id.guide_backup).setOnClickListener(view -> backup());
        findViewById(R.id.guide_finish).setOnClickListener(view -> {
            BeginnerGuideDraftStore.clear();
            finish();
        });
    }

    private void setupScenario() {
        boolean network = scenario.isNetwork(), ai = scenario == GuidedScenario.ASK_AI;
        visible(R.id.guide_network_fields, network);
        visible(R.id.guide_switch_fields, !network && !ai);
        visible(R.id.guide_evidence_layout, !network);
        visible(R.id.guide_parameter_layout, scenario == GuidedScenario.PORT_LINK || scenario == GuidedScenario.VLAN_GATEWAY);
        text(R.id.guide_scope, network ? R.string.guide_network_scope : ai ? R.string.guide_ai_scope : R.string.guide_switch_scope);
        text(R.id.guide_step, network ? R.string.guide_step_network : ai ? R.string.guide_step_ai : R.string.guide_step_switch);
        text(R.id.guide_start, network ? R.string.guide_start : ai ? R.string.guide_ai_draft : R.string.guide_analyze);
        text(R.id.guide_professional, network ? R.string.guide_network_pro : ai ? R.string.guide_ai_settings : R.string.guide_switch_pro);
        port.setText(scenario == GuidedScenario.SWITCH_LOGIN ? "22" : "443");
        if (scenario == GuidedScenario.VLAN_GATEWAY) {
            TextInputLayout layout = findViewById(R.id.guide_parameter_layout);
            layout.setHint(getString(R.string.guide_vlan));
            layout.setHelperText(getString(R.string.guide_vlan_help));
        }
        if (ai) {
            TextInputLayout layout = findViewById(R.id.guide_evidence_layout);
            layout.setHint(getString(R.string.guide_question));
            layout.setHelperText(getString(R.string.guide_question_help));
        }
    }

    private void start() {
        if (busy) return;
        if (scenario == GuidedScenario.ASK_AI) { ai(evidence.getText().toString()); return; }
        clearResults();
        if (!scenario.isNetwork()) {
            identifyIfUnknown();
            List<DiagnosticFinding> findings = switches.analyze(scenario, selectedVendor(),
                    parameter.getText().toString(), evidence.getText().toString());
            finishResults(findings, source.isEmpty() ? getString(R.string.guide_input_source) : source);
            visible(R.id.guide_backup, scenario == GuidedScenario.CONFIG_BACKUP && evidence.length() > 0);
            return;
        }
        final String host = target.getText().toString().trim(), selectedPort = port.getText().toString().trim();
        final String resultSource = getString(R.string.guide_phone_source, host, selectedPort);
        runner = new NetworkDiagnosisRunner();
        final NetworkDiagnosisRunner activeRunner = runner;
        final int token = ++generation;
        setBusy(true);
        progress.setProgress(0);
        note(R.string.guide_working);
        job = worker.submit(() -> {
            try {
                List<DiagnosticFinding> findings = activeRunner.run(scenario, host, selectedPort,
                        (completed, total, message) -> runOnUiThread(() -> {
                            if (token != generation || isFinishing()) return;
                            progress.setMax(total); progress.setProgress(completed);
                            progressText.setText(message.get(english));
                        }));
                runOnUiThread(() -> {
                    if (token != generation || isFinishing()) return;
                    setBusy(false);
                    finishResults(findings, resultSource);
                });
            } catch (InterruptedException cancelled) {
                Thread.currentThread().interrupt();
                runOnUiThread(() -> completeError(token, R.string.guide_cancelled));
            } catch (IllegalArgumentException invalid) {
                runOnUiThread(() -> completeError(token, R.string.guide_invalid));
            } catch (Exception unavailable) {
                runOnUiThread(() -> completeError(token, R.string.guide_failed));
            }
        });
    }

    private void completeError(int token, int message) {
        if (token != generation || isFinishing()) return;
        setBusy(false); note(message);
    }

    private void finishResults(List<DiagnosticFinding> findings, String resultSource) {
        int[] counts = new int[DiagnosticFinding.Status.values().length];
        for (DiagnosticFinding finding : findings) counts[finding.getStatus().ordinal()]++;
        summary.setText(getString(R.string.guide_summary, findings.size(), counts[0], counts[1], counts[2], counts[3]));
        summary.setVisibility(View.VISIBLE);
        renderer.render(results, findings);
        report = formatter.format(getString(BeginnerTaskCatalog.title(scenario)), resultSource,
                DateFormat.getDateTimeInstance().format(new Date()), findings, english);
        visible(R.id.guide_report_actions, true);
        visible(R.id.guide_progress_text, false);
        summary.requestFocus();
        summary.post(() -> summary.requestRectangleOnScreen(
                new android.graphics.Rect(0, 0, summary.getWidth(), summary.getHeight()), false));
    }

    private void capture() {
        ActiveTerminalSnapshot snapshot = ActiveTerminalSessionStore.shared().latestConnected();
        if (snapshot == null) { note(R.string.guide_no_session); refreshSession(); return; }
        if (snapshot.getText().trim().isEmpty()) { note(R.string.guide_empty_session); return; }
        importing = true;
        evidence.setText(formatter.captureTail(snapshot.getText()));
        importing = false;
        source = getString(R.string.guide_capture_source, snapshot.getTransport().name(),
                snapshot.getTargetLabel(), DateFormat.getDateTimeInstance().format(new Date(snapshot.getUpdatedAtMillis())));
        session.setText(formatter.sanitize(source));
        DeviceFingerprint fingerprint = new DeviceFingerprintEngine().identify(evidence.getText().toString());
        selectVendor(fingerprint.isHighConfidence() ? fingerprint.getVendor() : Vendor.GENERIC);
        invalidateResult();
    }

    private void identifyIfUnknown() {
        if (selectedVendor() != Vendor.GENERIC) return;
        DeviceFingerprint fingerprint = new DeviceFingerprintEngine().identify(evidence.getText().toString());
        if (fingerprint.isHighConfidence()) selectVendor(fingerprint.getVendor());
    }

    private void prepareCommands() {
        identifyIfUnknown();
        commands.removeAllViews();
        try {
            List<GuideCommand> drafts = switches.commands(scenario, selectedVendor(), parameter.getText().toString());
            commands.addView(renderer.text(getString(drafts.isEmpty() ? R.string.guide_commands_empty : R.string.guide_commands_note)));
            for (GuideCommand draft : drafts) {
                commands.addView(renderer.text(draft.getPurpose().get(english)));
                MaterialButton button = new MaterialButton(this, null, androidx.appcompat.R.attr.borderlessButtonStyle);
                button.setText(draft.getCommand());
                button.setAllCaps(false);
                button.setOnClickListener(view -> {
                    ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                    clipboard.setPrimaryClip(ClipData.newPlainText("Read-only command", draft.getCommand()));
                    Toast.makeText(this, R.string.guide_copied, Toast.LENGTH_SHORT).show();
                });
                commands.addView(button);
            }
            if (!drafts.isEmpty()) {
                MaterialButton terminal = new MaterialButton(this);
                terminal.setText(R.string.guide_terminal);
                terminal.setOnClickListener(view -> navigate("terminal"));
                commands.addView(terminal);
            }
        } catch (IllegalArgumentException invalid) { note(R.string.guide_invalid_parameter); }
    }

    private void ai(String content) {
        String safe = formatter.sanitize(content);
        if (safe.trim().isEmpty()) { note(R.string.guide_nothing); return; }
        String limited = safe.substring(0, Math.min(safe.length(), 7000));
        String draft = getString(R.string.guide_ai_instruction, limited);
        review(draft, true, () -> startActivity(new Intent(this, AiCopilotActivity.class)
                .putExtra(AiCopilotActivity.EXTRA_BEGINNER_PROMPT, draft)));
    }

    private void backup() {
        String safe = formatter.sanitize(evidence.getText().toString());
        if (safe.trim().isEmpty()) { note(R.string.guide_nothing); return; }
        new MaterialAlertDialogBuilder(this).setTitle(R.string.guide_backup)
                .setMessage(R.string.guide_backup_note).setNegativeButton(R.string.guide_back, null)
                .setPositiveButton(R.string.guide_continue, (dialog, which) ->
                        startActivity(new Intent(this, ConfigSnapshotCenterActivity.class)
                                .putExtra(ConfigSnapshotCenterActivity.EXTRA_CAPTURE, safe))).show();
    }

    private void review(String value, boolean ai, Runnable confirmed) {
        if (value.isEmpty()) return;
        ScrollView scroll = new ScrollView(this);
        TextView text = renderer.text(getString(R.string.guide_review_note)
                + (ai ? "\n" + getString(R.string.guide_ai_limit) : "") + "\n\n" + value);
        int padding = (int) (20 * getResources().getDisplayMetrics().density);
        text.setPadding(padding, padding, padding, padding);
        text.setTextIsSelectable(true);
        scroll.addView(text);
        new MaterialAlertDialogBuilder(this).setTitle(R.string.guide_review_title).setView(scroll)
                .setNegativeButton(R.string.guide_back, null)
                .setPositiveButton(R.string.guide_continue, (dialog, which) -> confirmed.run()).show();
    }

    private void professional() {
        Class<?> destination = scenario.isNetwork() ? NetworkToolsActivity.class
                : scenario == GuidedScenario.ASK_AI ? AiProviderSettingsActivity.class : OperationsCenterActivity.class;
        Intent intent = new Intent(this, destination);
        if (scenario.isNetwork()) intent.putExtra(NetworkToolsActivity.EXTRA_TARGET, target.getText().toString())
                .putExtra(NetworkToolsActivity.EXTRA_PORT, port.getText().toString());
        startActivity(intent);
    }

    private void navigate(String destination) {
        BeginnerGuideDraftStore.save(draftState());
        if ("terminal".equals(destination) && getIntent().getBooleanExtra(EXTRA_RETURN_REMOTE, false)) {
            finish();
            return;
        }
        new MaterialAlertDialogBuilder(this).setTitle(R.string.guide_connection_choice)
                .setMessage(R.string.guide_connection_help)
                .setNegativeButton(R.string.guide_back, null)
                .setNeutralButton(R.string.guide_usb, (dialog, which) ->
                        startActivity(new Intent(this, MainActivity.class)
                                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP)
                                .putExtra(MainActivity.EXTRA_GUIDE_DESTINATION, destination)))
                .setPositiveButton(R.string.guide_remote, (dialog, which) ->
                        startActivity(new Intent(this, RemoteTerminalActivity.class))).show();
    }

    private void watch(EditText input) {
        input.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (input == evidence && !importing) {
                    source = "";
                    refreshSession();
                }
                if (input == parameter || input == evidence) commands.removeAllViews();
                invalidateResult();
            }
            @Override public void afterTextChanged(Editable value) { }
        });
    }

    private void invalidateResult() {
        if (!report.isEmpty()) { clearResults(); note(R.string.guide_stale); }
    }

    private void clearResults() {
        report = "";
        results.removeAllViews();
        visible(R.id.guide_summary, false);
        visible(R.id.guide_report_actions, false);
        visible(R.id.guide_backup, false);
    }

    private void setBusy(boolean running) {
        busy = running;
        target.setEnabled(!running); port.setEnabled(!running);
        findViewById(R.id.guide_start).setEnabled(!running);
        findViewById(R.id.guide_professional).setEnabled(!running);
        visible(R.id.guide_progress, running);
        visible(R.id.guide_cancel, running);
    }

    private void cancel() {
        if (!busy) return;
        ++generation;
        if (runner != null) runner.cancel();
        if (job != null) job.cancel(true);
        setBusy(false); note(R.string.guide_cancelled);
    }

    private void refreshSession() {
        if (!source.isEmpty()) { session.setText(formatter.sanitize(source)); return; }
        ActiveTerminalSnapshot snapshot = ActiveTerminalSessionStore.shared().latestConnected();
        session.setText(snapshot == null ? getString(R.string.guide_no_session)
                : formatter.sanitize(getString(R.string.guide_live_session, snapshot.getTransport().name(), snapshot.getTargetLabel())));
    }

    private Vendor selectedVendor() { return VENDORS[Math.max(0, vendor.getSelectedItemPosition())]; }
    private void selectVendor(Vendor selected) {
        for (int i = 0; i < VENDORS.length; i++) if (VENDORS[i] == selected) {
            observedVendor = i;
            vendor.setSelection(i);
            return;
        }
    }
    private void visible(int id, boolean show) { findViewById(id).setVisibility(show ? View.VISIBLE : View.GONE); }
    private void text(int id, int string) { ((TextView) findViewById(id)).setText(string); }
    private void note(int string) { progressText.setText(string); progressText.setVisibility(View.VISIBLE); }
    @Override protected void onResume() { super.onResume(); if (session != null) refreshSession(); }
    @Override protected void onStop() {
        cancel();
        Bundle pending = BeginnerGuideDraftStore.peek();
        if (pending != null && scenario.name().equals(pending.getString("scenario"))) {
            BeginnerGuideDraftStore.save(draftState());
        }
        super.onStop();
    }
    @Override protected void onDestroy() { cancel(); worker.shutdownNow(); super.onDestroy(); }
    @Override protected void onSaveInstanceState(@NonNull Bundle state) {
        state.putAll(draftState());
        super.onSaveInstanceState(state);
    }

    private Bundle draftState() {
        Bundle state = new Bundle();
        state.putString("scenario", scenario.name());
        state.putString("target", target.getText().toString());
        state.putString("port", port.getText().toString());
        state.putString("parameter", parameter.getText().toString());
        state.putString("evidence", formatter.sanitize(evidence.getText().toString()));
        state.putString("source", formatter.sanitize(source));
        state.putInt("vendor", vendor.getSelectedItemPosition());
        return state;
    }
}
