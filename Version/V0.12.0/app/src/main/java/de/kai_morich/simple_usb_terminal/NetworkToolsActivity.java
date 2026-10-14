package de.kai_morich.simple_usb_terminal;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.activity.OnBackPressedCallback;

import com.chenwei666.netserial.network.toolbox.ProbeSession;
import com.chenwei666.netserial.network.toolbox.ToolRequest;
import com.chenwei666.netserial.network.toolbox.ToolResult;
import com.chenwei666.netserial.network.toolbox.ToolboxService;
import com.chenwei666.netserial.network.toolbox.ToolFailures;
import com.chenwei666.netserial.terminal.SensitiveTextRedactor;
import com.chenwei666.netserial.settings.AppSettingsStore;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.color.MaterialColors;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/** User-driven, bounded diagnostics. Views are read only on the UI thread. */
public final class NetworkToolsActivity extends ThemedActivity {
    public static final String EXTRA_TARGET = "guided_target";
    public static final String EXTRA_PORT = "guided_port";
    private final ExecutorService worker = Executors.newSingleThreadExecutor();
    private EditText target, option, timeout, count, search;
    private TextView result, stateLabel, resultTitle, raw;
    private LinearLayout catalogItems, resultRows;
    private Toolbar toolbar;
    private ToolboxService service;
    private ProbeSession session;
    private Future<?> pending;
    private ToolResult latest;
    private ToolRequest latestRequest;
    private String selected;
    private boolean busy;
    private long generation;
    private final Map<String, String[]> drafts = new HashMap<>();

    private static final class Tool {
        final String id;
        final int title, description, hint, category;
        Tool(String id, int title, int description, int hint, int category) {
            this.id = id; this.title = title; this.description = description;
            this.hint = hint; this.category = category;
        }
    }
    private static final Tool[] TOOLS = {
        new Tool("HEALTH", R.string.tb_health, R.string.tb_health_desc, R.string.tb_hint_health, R.string.tb_common),
        new Tool("PING", R.string.tb_ping, R.string.tb_ping_desc, R.string.tb_hint_ping, R.string.tb_common),
        new Tool("TCP", R.string.tb_tcp, R.string.tb_tcp_desc, R.string.tb_hint_tcp, R.string.tb_common),
        new Tool("TRACE", R.string.tb_trace, R.string.tb_trace_desc, R.string.tb_hint_trace, R.string.tb_connect),
        new Tool("MTU", R.string.tb_mtu, R.string.tb_mtu_desc, R.string.tb_hint_mtu, R.string.tb_connect),
        new Tool("ADDRESS", R.string.tb_address, R.string.tb_address_desc, R.string.tb_hint_address, R.string.tb_connect),
        new Tool("DNS", R.string.tb_dns, R.string.tb_dns_desc, R.string.tb_hint_dns, R.string.tb_web),
        new Tool("HTTP", R.string.tb_http, R.string.tb_http_desc, R.string.tb_hint_http, R.string.tb_web),
        new Tool("TLS", R.string.tb_tls, R.string.tb_tls_desc, R.string.tb_hint_tls, R.string.tb_web),
        new Tool("INFO", R.string.tb_info, R.string.tb_info_desc, R.string.tb_hint_info, R.string.tb_lan),
        new Tool("DISCOVER", R.string.tb_discover, R.string.tb_discover_desc, R.string.tb_hint_discover, R.string.tb_lan),
        new Tool("WOL", R.string.tb_wol, R.string.tb_wol_desc, R.string.tb_hint_wol, R.string.tb_lan),
        new Tool("CIDR", R.string.tb_cidr, R.string.tb_cidr_desc, R.string.tb_hint_cidr, R.string.tb_offline),
        new Tool("MAC", R.string.tb_mac, R.string.tb_mac_desc, R.string.tb_hint_mac, R.string.tb_offline),
        new Tool("PORTS", R.string.tb_ports, R.string.tb_ports_desc, R.string.tb_hint_ports, R.string.tb_offline),
        new Tool("EXTRACT", R.string.tb_extract, R.string.tb_extract_desc, R.string.tb_hint_extract, R.string.tb_offline)
    };

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
        setContentView(R.layout.activity_network_tools);
        configureDetailColumns();
        toolbar = findViewById(R.id.network_tools_toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> backToCatalog());
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override public void handleOnBackPressed() { backToCatalog(); }
        });
        service = new ToolboxService(getApplicationContext());
        target = findViewById(R.id.network_target);
        option = findViewById(R.id.network_port);
        timeout = findViewById(R.id.toolbox_timeout);
        count = findViewById(R.id.toolbox_count);
        search = findViewById(R.id.toolbox_search);
        result = findViewById(R.id.network_result);
        stateLabel = findViewById(R.id.toolbox_state);
        resultTitle = findViewById(R.id.toolbox_result_title);
        raw = findViewById(R.id.toolbox_raw);
        catalogItems = findViewById(R.id.toolbox_catalog_items);
        resultRows = findViewById(R.id.toolbox_result_rows);
        int configured = new AppSettingsStore(this).load().getNetworkProbeTimeoutMillis() / 1000;
        int seconds = getPreferences(MODE_PRIVATE).getInt("timeout_seconds", configured);
        timeout.setText(String.valueOf(Math.max(1, Math.min(15, seconds))));
        findViewById(R.id.toolbox_start).setOnClickListener(v -> prepareRun());
        findViewById(R.id.toolbox_stop).setOnClickListener(v -> stop(true));
        findViewById(R.id.toolbox_raw_toggle).setOnClickListener(v -> {
            boolean show = raw.getVisibility() != View.VISIBLE;
            raw.setVisibility(show ? View.VISIBLE : View.GONE);
            ((TextView) v).setText(show ? R.string.tb_raw_hide : R.string.tb_raw);
        });
        findViewById(R.id.toolbox_report).setOnClickListener(v -> showReport(false));
        findViewById(R.id.toolbox_ai).setOnClickListener(v -> showReport(true));
        findViewById(R.id.toolbox_trace_device).setOnClickListener(v -> showDeviceTrace());
        search.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int length, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int length) { renderCatalog(s.toString()); }
            @Override public void afterTextChanged(Editable e) { }
        });
        renderCatalog("");
        if (savedInstanceState != null && savedInstanceState.getString("tool") != null) {
            selectTool(savedInstanceState.getString("tool"));
        } else {
            String guidedTarget = getIntent().getStringExtra(EXTRA_TARGET);
            String guidedPort = getIntent().getStringExtra(EXTRA_PORT);
            if (guidedTarget != null || guidedPort != null) {
                selectTool(guidedPort == null ? "PING" : "TCP");
                if (guidedTarget != null) target.setText(guidedTarget);
                if (guidedPort != null) option.setText(guidedPort);
            }
        }
    }

    private void renderCatalog(String query) {
        catalogItems.removeAllViews();
        String needle = query.trim().toLowerCase(Locale.ROOT);
        int[] categories = { R.string.tb_common, R.string.tb_connect, R.string.tb_web, R.string.tb_lan, R.string.tb_offline };
        int width = getResources().getConfiguration().screenWidthDp;
        int columns = getResources().getConfiguration().fontScale > 1.25f ? 1 : width >= 720 ? 3 : 2;
        int total = 0;
        for (int category : categories) {
            List<Tool> matches = new ArrayList<>();
            for (Tool tool : TOOLS) {
                String index = (tool.id + " " + getString(tool.title) + " " + getString(tool.description)).toLowerCase(Locale.ROOT);
                if (tool.category == category && (needle.isEmpty() || index.contains(needle))) matches.add(tool);
            }
            if (matches.isEmpty()) continue;
            TextView heading = text(getString(category), 18, true);
            heading.setPadding(0, dp(20), 0, dp(8));
            catalogItems.addView(heading);
            for (int start = 0; start < matches.size(); start += columns) {
                LinearLayout row = new LinearLayout(this);
                row.setOrientation(LinearLayout.HORIZONTAL);
                row.setBaselineAligned(false);
                catalogItems.addView(row);
                for (int col = 0; col < columns; col++) {
                    LinearLayout.LayoutParams cell = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1);
                    cell.setMargins(col == 0 ? 0 : dp(8), 0, 0, dp(8));
                    if (start + col >= matches.size()) {
                        row.addView(new View(this), cell);
                        continue;
                    }
                    Tool tool = matches.get(start + col);
                    MaterialCardView card = new MaterialCardView(this);
                    card.setRadius(dp(16));
                    card.setCardElevation(0);
                    card.setStrokeWidth(dp(1));
                    card.setStrokeColor(MaterialColors.getColor(card, com.google.android.material.R.attr.colorOutline));
                    card.setCardBackgroundColor(MaterialColors.getColor(card, com.google.android.material.R.attr.colorSurface));
                    card.setClickable(true);
                    card.setFocusable(true);
                    card.setTag("tool:" + tool.id);
                    card.setContentDescription(getString(tool.title) + ". " + getString(tool.description));
                    card.setOnClickListener(v -> selectTool(tool.id));
                    LinearLayout content = new LinearLayout(this);
                    content.setOrientation(LinearLayout.VERTICAL);
                    content.setPadding(dp(14), dp(16), dp(14), dp(16));
                    content.setMinimumHeight(dp(116));
                    TextView title = text(getString(tool.title), 16, true);
                    title.setTextColor(MaterialColors.getColor(card, androidx.appcompat.R.attr.colorPrimary));
                    content.addView(title);
                    TextView desc = text(getString(tool.description), 13, false);
                    desc.setPadding(0, dp(8), 0, 0);
                    content.addView(desc);
                    card.addView(content);
                    row.addView(card, cell);
                    total++;
                }
            }
        }
        if (total == 0) catalogItems.addView(text(getString(R.string.tb_no_match), 16, false));
    }

    private void configureDetailColumns() {
        boolean wide = getResources().getConfiguration().screenWidthDp >= 720
                && getResources().getConfiguration().fontScale < 1.25f;
        LinearLayout detail = findViewById(R.id.toolbox_detail);
        detail.setOrientation(wide ? LinearLayout.HORIZONTAL : LinearLayout.VERTICAL);
        LinearLayout.LayoutParams parameters = new LinearLayout.LayoutParams(
                wide ? 0 : LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT, wide ? 0.38f : 0);
        LinearLayout.LayoutParams results = new LinearLayout.LayoutParams(
                wide ? 0 : LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT, wide ? 0.62f : 0);
        results.setMarginStart(wide ? dp(16) : 0);
        findViewById(R.id.toolbox_parameters).setLayoutParams(parameters);
        findViewById(R.id.toolbox_results_container).setLayoutParams(results);
    }

    private Tool findTool(String id) {
        for (Tool tool : TOOLS) if (tool.id.equals(id)) return tool;
        throw new IllegalArgumentException("Unknown tool");
    }

    private void selectTool(String id) {
        if (busy) stop(false);
        saveDraft();
        Tool tool = findTool(id);
        selected = id;
        latest = null;
        latestRequest = null;
        findViewById(R.id.toolbox_catalog).setVisibility(View.GONE);
        findViewById(R.id.toolbox_detail).setVisibility(View.VISIBLE);
        if (getSupportActionBar() != null) getSupportActionBar().setTitle(tool.title);
        ((TextView) findViewById(R.id.toolbox_description)).setText(tool.hint);
        ((ScrollView) findViewById(R.id.toolbox_scroll)).smoothScrollTo(0, 0);
        boolean offline = isOffline(id);
        boolean hasTarget = !id.equals("INFO") && !id.equals("PORTS");
        boolean hasOption = id.equals("DNS") || id.equals("TCP") || id.equals("TLS")
                || id.equals("DISCOVER") || id.equals("WOL") || id.equals("HEALTH") || id.equals("CIDR");
        findViewById(R.id.toolbox_target_layout).setVisibility(hasTarget ? View.VISIBLE : View.GONE);
        findViewById(R.id.toolbox_option_layout).setVisibility(hasOption ? View.VISIBLE : View.GONE);
        findViewById(R.id.toolbox_limits).setVisibility(offline || id.equals("INFO") || id.equals("WOL") ? View.GONE : View.VISIBLE);
        findViewById(R.id.toolbox_count_layout).setVisibility(id.equals("PING") || id.equals("TRACE") ? View.VISIBLE : View.GONE);
        ((TextInputLayout) findViewById(R.id.toolbox_count_layout)).setHint(getString(
                id.equals("TRACE") ? R.string.tb_trace_count : R.string.tb_ping_count));
        target.setText("");
        target.setInputType(id.equals("EXTRACT") ? InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE
                : InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_URI);
        target.setSingleLine(!id.equals("EXTRACT"));
        target.setMinLines(id.equals("EXTRACT") ? 4 : 1);
        target.setMaxLines(id.equals("EXTRACT") ? 8 : 1);
        ((TextInputLayout) findViewById(R.id.toolbox_target_layout)).setHint(getString(targetHint(id)));
        int optionHint = id.equals("CIDR") ? R.string.tb_mask_option : id.equals("DNS") ? R.string.tb_dns_option : id.equals("WOL") ? R.string.tb_mac_input
                : id.equals("TLS") ? R.string.tb_tls_port : id.equals("HEALTH") ? R.string.tb_health_port : R.string.tb_port_option;
        ((TextInputLayout) findViewById(R.id.toolbox_option_layout)).setHint(getString(optionHint));
        option.setText(id.equals("DNS") ? "A" : id.equals("TCP") ? "22,80,443"
                : id.equals("DISCOVER") ? "80,443" : id.equals("TLS") || id.equals("HEALTH") ? "443" : "");
        count.setText(id.equals("TRACE") ? "20" : "4");
        String[] draft = drafts.get(id);
        if (draft != null) {
            target.setText(draft[0]);
            option.setText(draft[1]);
            count.setText(draft[2]);
        }
        stateLabel.setText(R.string.tb_ready);
        findViewById(R.id.toolbox_result_card).setVisibility(View.GONE);
        target.clearFocus();
    }

    private boolean isOffline(String id) { return id.equals("CIDR") || id.equals("MAC") || id.equals("PORTS") || id.equals("EXTRACT"); }
    private int targetHint(String id) {
        switch (id) {
            case "HTTP": return R.string.tb_url;
            case "CIDR": return R.string.tb_cidr_input;
            case "DISCOVER": return R.string.tb_private_cidr;
            case "MAC": return R.string.tb_mac_input;
            case "EXTRACT": return R.string.tb_text_input;
            case "WOL": return R.string.tb_wol_target;
            default: return R.string.tb_target;
        }
    }

    private void prepareRun() {
        if (busy || selected == null) return;
        final ToolRequest request;
        try {
            boolean usesTimeout = findViewById(R.id.toolbox_limits).getVisibility() == View.VISIBLE;
            int seconds = usesTimeout ? Integer.parseInt(timeout.getText().toString()) : 3;
            int amount = selected.equals("PING") || selected.equals("TRACE") ? Integer.parseInt(count.getText().toString()) : 4;
            request = new ToolRequest(target.getText().toString(), option.getText().toString(), seconds * 1000, amount,
                    getResources().getConfiguration().locale.getLanguage().equals("zh"));
            if (usesTimeout) getPreferences(MODE_PRIVATE).edit().putInt("timeout_seconds", seconds).apply();
        } catch (RuntimeException invalid) {
            stateLabel.setText(R.string.tb_invalid);
            return;
        }
        if (selected.equals("DISCOVER") || selected.equals("WOL")) {
            String tool = selected;
            AlertDialog dialog = new AlertDialog.Builder(this).setTitle(R.string.tb_confirm_title)
                    .setMessage(getString(tool.equals("DISCOVER") ? R.string.tb_discover_confirm : R.string.tb_wol_confirm,
                            request.target, request.option))
                    .setNegativeButton(R.string.tb_cancel, null)
                    .setPositiveButton(R.string.tb_confirm, (d, which) -> { if (tool.equals(selected) && !busy) start(request); })
                    .create();
            secureShow(dialog);
        } else start(request);
    }

    private void start(ToolRequest request) {
        final String tool = selected;
        final long token = ++generation;
        latest = null;
        latestRequest = request;
        findViewById(R.id.toolbox_result_card).setVisibility(View.GONE);
        stateLabel.setText(R.string.tb_running);
        setBusy(true);
        InputMethodManager keyboard = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (keyboard != null) keyboard.hideSoftInputFromWindow(target.getWindowToken(), 0);
        target.clearFocus(); option.clearFocus(); timeout.clearFocus(); count.clearFocus();
        final ProbeSession running = new ProbeSession(120000, report -> runOnUiThread(() -> {
            if (current(token)) display(report);
        }));
        session = running;
        pending = worker.submit(() -> {
            ToolResult completed;
            try {
                completed = service.run(tool, request, running);
            } catch (Exception failed) {
                if (failed instanceof InterruptedException) Thread.currentThread().interrupt();
                completed = ToolFailures.result(getString(findTool(tool).title), request, failed);
            } finally {
                running.cancel();
            }
            ToolResult delivered = completed;
            runOnUiThread(() -> {
                if (!current(token)) return;
                session = null;
                pending = null;
                setBusy(false);
                display(delivered);
            });
        });
    }

    private boolean current(long token) { return token == generation && !isFinishing() && !isDestroyed(); }

    private void setBusy(boolean value) {
        busy = value;
        target.setEnabled(!value); option.setEnabled(!value); timeout.setEnabled(!value); count.setEnabled(!value);
        findViewById(R.id.toolbox_start).setVisibility(value ? View.GONE : View.VISIBLE);
        findViewById(R.id.toolbox_stop).setVisibility(value ? View.VISIBLE : View.GONE);
        findViewById(R.id.toolbox_progress).setVisibility(value ? View.VISIBLE : View.GONE);
        findViewById(R.id.toolbox_report).setEnabled(!value);
        findViewById(R.id.toolbox_ai).setEnabled(!value);
        findViewById(R.id.toolbox_trace_device).setEnabled(!value);
    }

    private void display(ToolResult report) {
        latest = report;
        findViewById(R.id.toolbox_result_card).setVisibility(View.VISIBLE);
        resultTitle.setText(report.title);
        result.setText(report.summary);
        int status;
        switch (report.status) {
            case OK: status = R.string.tb_status_ok; break;
            case PARTIAL: status = R.string.tb_status_partial; break;
            case UNSUPPORTED: status = R.string.tb_status_unsupported; break;
            case CANCELLED: status = R.string.tb_status_cancelled; break;
            default: status = R.string.tb_status_failed;
        }
        stateLabel.setText(busy ? R.string.tb_running : status);
        resultRows.removeAllViews();
        for (ToolResult.Row item : report.rows) {
            LinearLayout line = new LinearLayout(this);
            line.setOrientation(getResources().getConfiguration().screenWidthDp >= 600 ? LinearLayout.HORIZONTAL : LinearLayout.VERTICAL);
            line.setPadding(0, dp(8), 0, dp(8));
            TextView label = text(item.label, 13, false);
            TextView value = text(item.value, 16, true);
            value.setTextIsSelectable(true);
            if (line.getOrientation() == LinearLayout.HORIZONTAL) {
                line.addView(label, new LinearLayout.LayoutParams(0, -2, 1));
                line.addView(value, new LinearLayout.LayoutParams(0, -2, 2));
            } else { line.addView(label); line.addView(value); }
            resultRows.addView(line);
        }
        ToolboxTrendView trend = findViewById(R.id.toolbox_trend);
        trend.setSamples(report.samples);
        trend.setVisibility(report.samples.isEmpty() ? View.GONE : View.VISIBLE);
        findViewById(R.id.toolbox_trend_label).setVisibility(report.samples.isEmpty() ? View.GONE : View.VISIBLE);
        raw.setText(report.raw);
        raw.setVisibility(View.GONE);
        ((TextView) findViewById(R.id.toolbox_raw_toggle)).setText(R.string.tb_raw);
        findViewById(R.id.toolbox_raw_toggle).setVisibility(report.raw.isEmpty() ? View.GONE : View.VISIBLE);
        findViewById(R.id.toolbox_trace_device).setVisibility("TRACE".equals(selected)
                && (report.status == ToolResult.Status.UNSUPPORTED || report.status == ToolResult.Status.PARTIAL)
                ? View.VISIBLE : View.GONE);
    }

    private void stop(boolean showResult) {
        ++generation;
        if (session != null) session.cancel();
        if (pending != null) pending.cancel(true);
        session = null; pending = null;
        setBusy(false);
        if (showResult) {
            ToolResult stopped = new ToolResult(ToolResult.Status.CANCELLED,
                    getString(findTool(selected).title), getString(R.string.tb_cancelled),
                    latest == null ? Collections.emptyList() : latest.rows,
                    latest == null ? "" : latest.raw,
                    latest == null ? Collections.emptyList() : latest.samples);
            display(stopped);
        }
    }

    private void showReport(boolean ai) {
        if (latest == null || busy) return;
        final String report = new SensitiveTextRedactor().redact(latest.export());
        LinearLayout body = new LinearLayout(this);
        body.setOrientation(LinearLayout.VERTICAL);
        body.setPadding(dp(20), dp(8), dp(20), dp(12));
        body.addView(text(getString(ai ? R.string.tb_report_ai_notice : R.string.tb_report_notice), 14, false));
        TextView preview = text(report, 13, false);
        preview.setTextIsSelectable(true);
        preview.setPadding(0, dp(16), 0, 0);
        body.addView(preview);
        ScrollView scroll = new ScrollView(this);
        scroll.addView(body);
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle(ai ? R.string.tb_report_ai_title : R.string.tb_report)
                .setView(scroll).setNegativeButton(R.string.tb_close, null);
        if (ai) {
            builder.setPositiveButton(R.string.tb_ai, (d, which) -> startActivity(new Intent(this, AiCopilotActivity.class)
                    .putExtra(AiCopilotActivity.EXTRA_BEGINNER_PROMPT, getString(R.string.ai_diagnostic_prompt, report))));
        } else {
            builder.setPositiveButton(R.string.tb_copy, (d, which) -> copy(report));
            builder.setNeutralButton(R.string.tb_share, (d, which) -> startActivity(Intent.createChooser(
                    new Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT, report), getString(R.string.tb_share))));
        }
        secureShow(builder.create());
    }

    private void showDeviceTrace() {
        String host = latestRequest == null ? "" : latestRequest.target;
        if (host.length() > 253 || !host.matches("[A-Za-z0-9][A-Za-z0-9.:%_-]*") || host.startsWith("-")) {
            Toast.makeText(this, R.string.tb_trace_target_invalid, Toast.LENGTH_LONG).show();
            return;
        }
        String command = "H3C / Huawei:\ntracert " + host + "\n\nCisco / Ruijie:\ntraceroute " + host;
        secureShow(new AlertDialog.Builder(this).setTitle(R.string.tb_trace_device)
                .setMessage(getString(R.string.tb_trace_device_hint) + "\n\n" + command)
                .setNegativeButton(R.string.tb_close, null)
                .setPositiveButton(R.string.tb_copy, (d, which) -> copy(command)).create());
    }

    private void secureShow(AlertDialog dialog) {
        if (dialog.getWindow() != null) dialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
        dialog.show();
    }
    private void copy(String text) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard != null) clipboard.setPrimaryClip(ClipData.newPlainText("NetSerial AI", text));
        Toast.makeText(this, R.string.network_result_copied, Toast.LENGTH_SHORT).show();
    }
    private TextView text(String value, int size, boolean bold) {
        TextView view = new TextView(this);
        view.setText(value); view.setTextSize(size);
        view.setTextColor(MaterialColors.getColor(view, com.google.android.material.R.attr.colorOnSurface));
        if (bold) view.setTypeface(view.getTypeface(), android.graphics.Typeface.BOLD);
        return view;
    }
    private int dp(int value) { return Math.round(value * getResources().getDisplayMetrics().density); }
    private void backToCatalog() {
        if (selected == null) { finish(); return; }
        if (busy) stop(false);
        saveDraft();
        selected = null;
        findViewById(R.id.toolbox_detail).setVisibility(View.GONE);
        findViewById(R.id.toolbox_catalog).setVisibility(View.VISIBLE);
        if (getSupportActionBar() != null) getSupportActionBar().setTitle(R.string.tb_title);
        ((ScrollView) findViewById(R.id.toolbox_scroll)).smoothScrollTo(0, 0);
    }
    private void saveDraft() {
        if (selected != null) drafts.put(selected, new String[] {
                target.getText().toString(), option.getText().toString(), count.getText().toString() });
    }
    @Override protected void onSaveInstanceState(Bundle state) {
        if (selected != null) state.putString("tool", selected);
        super.onSaveInstanceState(state);
    }
    @Override protected void onStop() {
        if (busy) stop(true);
        super.onStop();
    }
    @Override protected void onDestroy() {
        ++generation;
        if (session != null) session.cancel();
        if (pending != null) pending.cancel(true);
        worker.shutdownNow();
        super.onDestroy();
    }
}
