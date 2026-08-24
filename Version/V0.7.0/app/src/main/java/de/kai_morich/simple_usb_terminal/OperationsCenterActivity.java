package de.kai_morich.simple_usb_terminal;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.chenwei666.netserial.automation.BatchExecutionPlan;
import com.chenwei666.netserial.automation.BatchTaskPlanner;
import com.chenwei666.netserial.automation.PlaybookPlan;
import com.chenwei666.netserial.automation.PlaybookType;
import com.chenwei666.netserial.automation.SafePlaybookEngine;
import com.chenwei666.netserial.compliance.ComplianceFinding;
import com.chenwei666.netserial.compliance.ComplianceReport;
import com.chenwei666.netserial.compliance.ConfigComplianceEngine;
import com.chenwei666.netserial.config.ConfigSnapshot;
import com.chenwei666.netserial.config.ConfigSnapshotStore;
import com.chenwei666.netserial.config.ConfigurationBackupEngine;
import com.chenwei666.netserial.diagnostics.HealthCheckPlan;
import com.chenwei666.netserial.diagnostics.HealthFinding;
import com.chenwei666.netserial.diagnostics.HealthReport;
import com.chenwei666.netserial.diagnostics.PortLookupType;
import com.chenwei666.netserial.diagnostics.PortTroubleshootingPlan;
import com.chenwei666.netserial.diagnostics.PortTroubleshootingEngine;
import com.chenwei666.netserial.diagnostics.SwitchHealthEngine;
import com.chenwei666.netserial.diagnostics.TroubleshootingStep;
import com.chenwei666.netserial.device.DeviceFingerprint;
import com.chenwei666.netserial.device.DeviceFingerprintEngine;
import com.chenwei666.netserial.device.DeviceProfile;
import com.chenwei666.netserial.device.DeviceProfileStore;
import com.chenwei666.netserial.device.Vendor;
import com.chenwei666.netserial.terminal.AnsiTextSanitizer;
import com.chenwei666.netserial.terminal.SensitiveTextRedactor;
import com.chenwei666.netserial.topology.SnmpV3DiscoveryPlanner;
import com.chenwei666.netserial.topology.SnmpV3QueryPlan;
import com.chenwei666.netserial.topology.TopologyGraph;
import com.chenwei666.netserial.topology.TopologyLink;
import com.chenwei666.netserial.topology.TopologyParser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** Offline operations planning. This activity never connects to or executes against a device. */
public final class OperationsCenterActivity extends ThemedActivity {
    public static final String EXTRA_PICK_MODE = "pick_mode";
    public static final String EXTRA_CAPTURE = "capture";
    public static final String RESULT_COMMAND_BATCH = "command_batch";

    private Spinner vendorSpinner;
    private Spinner playbookSpinner;
    private EditText input;
    private EditText parameter;
    private EditText targets;
    private TextView output;
    private PlaybookPlan latestPlan;

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
        setContentView(R.layout.activity_operations_center);
        setTitle(R.string.operations_title);
        vendorSpinner = findViewById(R.id.operations_vendor);
        playbookSpinner = findViewById(R.id.operations_playbook);
        input = findViewById(R.id.operations_input);
        parameter = findViewById(R.id.operations_parameter);
        targets = findViewById(R.id.operations_targets);
        output = findViewById(R.id.operations_output);
        bindSpinners();
        input.setText(sanitize(getIntent().getStringExtra(EXTRA_CAPTURE)));
        findViewById(R.id.operations_identify).setOnClickListener(view -> identify());
        findViewById(R.id.operations_health_plan).setOnClickListener(view -> healthPlan());
        findViewById(R.id.operations_health_analyze).setOnClickListener(view -> analyzeHealth());
        findViewById(R.id.operations_port_plan).setOnClickListener(view -> portPlan());
        findViewById(R.id.operations_topology).setOnClickListener(view -> parseTopology());
        findViewById(R.id.operations_backup).setOnClickListener(view -> saveBackup());
        findViewById(R.id.operations_snmp).setOnClickListener(view -> planSnmp());
        findViewById(R.id.operations_generate).setOnClickListener(view -> generate());
        findViewById(R.id.operations_compliance).setOnClickListener(view -> analyzeCompliance());
        findViewById(R.id.operations_batch).setOnClickListener(view -> planBatch());
        findViewById(R.id.operations_use).setOnClickListener(view -> usePlan());
        findViewById(R.id.operations_sessions).setOnClickListener(view ->
                startActivity(new Intent(this, SessionWorkspaceActivity.class)));
        findViewById(R.id.operations_snapshots).setOnClickListener(view ->
                startActivity(new Intent(this, ConfigSnapshotCenterActivity.class)
                        .putExtra(ConfigSnapshotCenterActivity.EXTRA_CAPTURE, sanitize(input.getText().toString()))));
    }

    private void bindSpinners() {
        ArrayAdapter<String> vendors = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item,
                new String[]{"H3C / Comware", "Huawei / VRP", "Cisco IOS", "Ruijie / RGOS"});
        vendors.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        vendorSpinner.setAdapter(vendors);
        selectVendor(new DeviceProfileStore(this).load().getVendor());
        ArrayAdapter<String> playbooks = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item,
                new String[]{getString(R.string.playbook_health), getString(R.string.playbook_interface),
                        getString(R.string.playbook_vlan), getString(R.string.playbook_neighbors),
                        getString(R.string.playbook_security)});
        playbooks.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        playbookSpinner.setAdapter(playbooks);
    }

    private void identify() {
        DeviceFingerprint result = new DeviceFingerprintEngine().identify(sanitize(input.getText().toString()));
        output.setText(getString(R.string.operations_identify_result, result.getVendor().name(),
                result.getConfidence(), result.getPlatform(), result.getEvidence()));
        if (result.isHighConfidence()) {
            selectVendor(result.getVendor());
            DeviceProfileStore store = new DeviceProfileStore(this);
            store.save(store.load().withVendor(result.getVendor()));
        }
    }

    private void generate() {
        try {
            latestPlan = new SafePlaybookEngine().plan(selectedVendor(), selectedPlaybook(),
                    parameter.getText().toString());
            output.setText(getString(R.string.operations_plan_result, latestPlan.getRisk().name(),
                    latestPlan.commandBatch(), join(latestPlan.getStopConditions())));
        } catch (RuntimeException exception) {
            latestPlan = null;
            output.setText(R.string.operations_invalid_parameter);
        }
    }

    private void healthPlan() {
        HealthCheckPlan plan = new SwitchHealthEngine().plan(selectedVendor());
        output.setText(getString(R.string.operations_health_result, plan.getRisk().name(), plan.commandBatch()));
    }

    private void analyzeHealth() {
        HealthReport report = new SwitchHealthEngine().analyze(sanitize(input.getText().toString()));
        StringBuilder result = new StringBuilder();
        for (HealthFinding finding : report.getFindings()) {
            if (result.length() > 0) result.append("\n\n");
            result.append('[').append(finding.getSeverity()).append("] ").append(finding.getCode())
                    .append("\n").append(finding.getEvidence())
                    .append("\n").append(finding.getRecommendation());
        }
        output.setText(result.toString());
    }

    private void portPlan() {
        try {
            String value = parameter.getText().toString().trim();
            PortLookupType type = value.matches("(?:\\d{1,3}\\.){3}\\d{1,3}") ? PortLookupType.IP
                    : value.matches("(?i)[0-9a-f]{2}(?:[:-]?[0-9a-f]{2}){5}") ? PortLookupType.MAC
                    : PortLookupType.INTERFACE;
            PortTroubleshootingPlan plan = new PortTroubleshootingEngine().plan(selectedVendor(), type, value);
            StringBuilder result = new StringBuilder();
            for (TroubleshootingStep step : plan.getSteps()) {
                if (result.length() > 0) result.append("\n\n");
                result.append('[').append(step.getPhase()).append("] ").append(step.getCommand())
                        .append("\n").append(step.getPurpose());
            }
            output.setText(result.toString());
        } catch (RuntimeException exception) {
            output.setText(R.string.operations_advanced_invalid);
        }
    }

    private void parseTopology() {
        DeviceProfile profile = new DeviceProfileStore(this).load();
        TopologyGraph graph = new TopologyParser().parse(profile.getName(), sanitize(input.getText().toString()));
        StringBuilder result = new StringBuilder("Nodes: ").append(graph.getNodes().size())
                .append(" · Links: ").append(graph.getLinks().size());
        for (TopologyLink link : graph.getLinks()) {
            result.append("\n").append(link.getLocalNode()).append(' ').append(link.getLocalPort())
                    .append(" -> ").append(link.getRemoteNode()).append(' ').append(link.getRemotePort());
        }
        output.setText(result.toString());
    }

    private void saveBackup() {
        try {
            DeviceProfile profile = new DeviceProfileStore(this).load();
            ConfigSnapshot snapshot = new ConfigurationBackupEngine().capture(
                    profile.getName() + "-" + System.currentTimeMillis(), input.getText().toString(),
                    System.currentTimeMillis());
            new ConfigSnapshotStore(this).add(snapshot);
            output.setText(getString(R.string.operations_backup_saved, snapshot.getLabel(), snapshot.getSha256()));
        } catch (RuntimeException exception) {
            output.setText(R.string.operations_advanced_invalid);
        }
    }

    private void planSnmp() {
        try {
            String[] values = parameter.getText().toString().split("\\|", -1);
            if (values.length != 3) throw new IllegalArgumentException("SNMP parameters required");
            SnmpV3QueryPlan plan = new SnmpV3DiscoveryPlanner().plan(targets.getText().toString().trim(),
                    values[0], values[1], values[2]);
            output.setText(getString(R.string.operations_snmp_hint) + "\n\nCIDR: " + plan.getCidr()
                    + "\nUser: " + plan.getSecurityName() + "\nAuth: " + plan.getAuthenticationProtocol()
                    + "\nPrivacy: " + plan.getPrivacyProtocol() + "\nOIDs:\n" + join(plan.getObjectIdentifiers()));
        } catch (RuntimeException exception) {
            output.setText(R.string.operations_advanced_invalid);
        }
    }

    private void analyzeCompliance() {
        try {
            ComplianceReport report = new ConfigComplianceEngine().analyze(selectedVendor(),
                    sanitize(input.getText().toString()));
            StringBuilder text = new StringBuilder(getString(R.string.operations_compliance_disclaimer));
            for (ComplianceFinding finding : report.getFindings()) {
                text.append("\n\n[").append(finding.getSeverity()).append("] ")
                        .append(finding.getRuleId()).append("\n")
                        .append(finding.getMessage()).append("\n")
                        .append(finding.getRecommendation());
            }
            output.setText(text.toString());
        } catch (RuntimeException exception) {
            output.setText(R.string.operations_input_invalid);
        }
    }

    private void planBatch() {
        if (latestPlan == null) generate();
        if (latestPlan == null) return;
        try {
            String[] raw = targets.getText().toString().split("[\\s,，;；]+");
            List<String> values = new ArrayList<>();
            for (String target : raw) if (!target.trim().isEmpty()) values.add(target.trim());
            BatchExecutionPlan plan = new BatchTaskPlanner().plan(values, latestPlan);
            output.setText(getString(R.string.operations_batch_result, plan.getCanaryTarget(),
                    join(plan.getRemainingTargets()), latestPlan.commandBatch()));
        } catch (RuntimeException exception) {
            output.setText(R.string.operations_targets_invalid);
        }
    }

    private void usePlan() {
        if (latestPlan == null) {
            Toast.makeText(this, R.string.operations_generate_first, Toast.LENGTH_SHORT).show();
            return;
        }
        if (!getIntent().getBooleanExtra(EXTRA_PICK_MODE, false)) {
            Toast.makeText(this, R.string.operations_preview_only, Toast.LENGTH_LONG).show();
            return;
        }
        setResult(Activity.RESULT_OK, new Intent().putExtra(RESULT_COMMAND_BATCH, latestPlan.commandBatch()));
        finish();
    }

    private Vendor selectedVendor() {
        return new Vendor[]{Vendor.H3C_COMWARE, Vendor.HUAWEI_VRP, Vendor.CISCO_IOS,
                Vendor.RUIJIE_RGOS}[vendorSpinner.getSelectedItemPosition()];
    }

    private PlaybookType selectedPlaybook() {
        return PlaybookType.values()[playbookSpinner.getSelectedItemPosition()];
    }

    private void selectVendor(Vendor vendor) {
        List<Vendor> values = Arrays.asList(Vendor.H3C_COMWARE, Vendor.HUAWEI_VRP,
                Vendor.CISCO_IOS, Vendor.RUIJIE_RGOS);
        int index = values.indexOf(vendor);
        vendorSpinner.setSelection(index < 0 ? 0 : index);
    }

    private static String join(List<String> values) {
        StringBuilder result = new StringBuilder();
        for (String value : values) {
            if (result.length() > 0) result.append("\n");
            result.append("- ").append(value);
        }
        return result.toString();
    }

    private static String sanitize(String value) {
        String text = value == null ? "" : value;
        if (text.length() > 500_000) text = text.substring(text.length() - 500_000);
        return new SensitiveTextRedactor().redact(new AnsiTextSanitizer().sanitize(text));
    }
}
