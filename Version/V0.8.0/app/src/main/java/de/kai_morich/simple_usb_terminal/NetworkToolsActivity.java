package de.kai_morich.simple_usb_terminal;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import com.chenwei666.netserial.network.Ipv4Calculator;
import com.chenwei666.netserial.network.Ipv4Network;
import com.chenwei666.netserial.network.Ipv6Calculator;
import com.chenwei666.netserial.network.Ipv6Network;
import com.chenwei666.netserial.network.MacAddressInfo;
import com.chenwei666.netserial.network.MacOuiLookup;
import com.chenwei666.netserial.network.CommonPortCatalog;
import com.chenwei666.netserial.network.PortReference;
import com.chenwei666.netserial.network.PortBatchParser;
import com.chenwei666.netserial.network.NetworkIdentifierExtractor;
import com.chenwei666.netserial.network.NetworkProbeService;
import com.chenwei666.netserial.network.AddressProbeEntry;
import com.chenwei666.netserial.network.AddressProbeResult;
import com.chenwei666.netserial.network.TcpBatchProbeResult;
import com.chenwei666.netserial.network.TcpPortProbeResult;
import com.chenwei666.netserial.settings.AppSettingsStore;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public final class NetworkToolsActivity extends ThemedActivity {
    private final ExecutorService worker = Executors.newSingleThreadExecutor();
    private final NetworkProbeService probes = new NetworkProbeService();
    private final AtomicBoolean probeRunning = new AtomicBoolean();
    private EditText cidr;
    private EditText target;
    private EditText port;
    private EditText mac;
    private EditText extractInput;
    private TextView result;
    private int timeoutMillis;

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
        setContentView(R.layout.activity_network_tools);
        Toolbar toolbar = findViewById(R.id.network_tools_toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(view -> finish());
        cidr = findViewById(R.id.network_cidr);
        target = findViewById(R.id.network_target);
        port = findViewById(R.id.network_port);
        mac = findViewById(R.id.network_mac);
        extractInput = findViewById(R.id.network_extract_input);
        result = findViewById(R.id.network_result);
        timeoutMillis = new AppSettingsStore(this).load().getNetworkProbeTimeoutMillis();
        findViewById(R.id.network_calculate).setOnClickListener(view -> calculate());
        findViewById(R.id.network_dns).setOnClickListener(view -> run(() -> probes.dns(target.getText().toString())));
        findViewById(R.id.network_ping).setOnClickListener(view -> run(() -> probes.ping(target.getText().toString(), timeoutMillis)));
        findViewById(R.id.network_trace).setOnClickListener(view -> run(() -> probes.traceroute(target.getText().toString(), timeoutMillis)));
        findViewById(R.id.network_tcp).setOnClickListener(view -> runTcp());
        findViewById(R.id.network_mtu).setOnClickListener(view -> run(() -> probes.pathMtu(target.getText().toString(), timeoutMillis)));
        findViewById(R.id.network_address_summary).setOnClickListener(view ->
                run(() -> formatAddressResult(
                        probes.addressSummary(target.getText().toString()))));
        findViewById(R.id.network_common_ports_fill).setOnClickListener(view ->
                port.setText("22,23,53,80,443,830"));
        findViewById(R.id.network_mac_lookup).setOnClickListener(view -> lookupMac());
        findViewById(R.id.network_ports).setOnClickListener(view -> showPorts());
        findViewById(R.id.network_extract).setOnClickListener(view ->
                result.setText(new NetworkIdentifierExtractor().extract(extractInput.getText().toString())));
        findViewById(R.id.network_copy_result).setOnClickListener(view -> copyResult());
        findViewById(R.id.network_share_result).setOnClickListener(view -> shareResult());
        findViewById(R.id.network_clear_result).setOnClickListener(view -> result.setText(""));
    }

    private void runTcp() {
        run(() -> {
            String ports = port.getText().toString();
            if (new PortBatchParser().parse(ports).size() == 1) {
                return probes.tcp(target.getText().toString(), ports, timeoutMillis);
            }
            return formatTcpBatchResult(
                    probes.tcpBatch(target.getText().toString(), ports, timeoutMillis));
        });
    }

    private String formatTcpBatchResult(TcpBatchProbeResult report) {
        StringBuilder output = new StringBuilder(getString(
                R.string.network_batch_result_header,
                report.getHost(), report.getPorts().size(), report.getTimeoutMillis()));
        for (TcpPortProbeResult item : report.getPorts()) {
            output.append('\n').append(getString(R.string.network_batch_result_line,
                    item.getPort(), getString(item.isOpen()
                            ? R.string.network_port_open
                            : R.string.network_port_closed_filtered),
                    item.getLatencyMillis()));
        }
        return output.toString();
    }

    private String formatAddressResult(AddressProbeResult report) {
        StringBuilder output = new StringBuilder(getString(
                R.string.network_address_result_header, report.getHost()));
        for (AddressProbeEntry item : report.getAddresses()) {
            output.append('\n').append(item.getAddress()).append("  ").append(item.getFamily());
            appendAddressFlag(output, item.isLoopback(), R.string.network_address_loopback);
            appendAddressFlag(output, item.isLinkLocal(), R.string.network_address_link_local);
            appendAddressFlag(output, item.isPrivateAddress(), R.string.network_address_private);
            appendAddressFlag(output, item.isMulticast(), R.string.network_address_multicast);
            appendAddressFlag(output, item.isAnyLocal(), R.string.network_address_any_local);
        }
        return output.toString();
    }

    private void appendAddressFlag(StringBuilder output, boolean present, int label) {
        if (present) output.append("  ").append(getString(label));
    }

    private void copyResult() {
        CharSequence output = result.getText();
        if (output == null || output.length() == 0) return;
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        clipboard.setPrimaryClip(ClipData.newPlainText("NetSerial AI", output));
        Toast.makeText(this, R.string.network_result_copied, Toast.LENGTH_SHORT).show();
    }

    private void shareResult() {
        CharSequence output = result.getText();
        if (output == null || output.length() == 0) return;
        Intent share = new Intent(Intent.ACTION_SEND)
                .setType("text/plain")
                .putExtra(Intent.EXTRA_TEXT, output.toString());
        startActivity(Intent.createChooser(share, getString(R.string.network_share_result)));
    }

    private void calculate() {
        try {
            if (cidr.getText().toString().contains(":")) {
                Ipv6Network network = new Ipv6Calculator().calculate(cidr.getText().toString());
                result.setText(getString(R.string.network_ipv6_result,
                        network.getNetworkPrefix(), network.getTotalAddresses()));
                return;
            }
            Ipv4Network network = new Ipv4Calculator().calculate(cidr.getText().toString());
            result.setText(getString(R.string.network_cidr_result, network.getNetwork(),
                    network.getNetmask(), network.getBroadcast(), network.getFirstUsable(),
                    network.getLastUsable(), network.getTotalAddresses()));
        } catch (RuntimeException exception) {
            result.setText(R.string.network_invalid_input);
        }
    }

    private void lookupMac() {
        try {
            MacAddressInfo info = new MacOuiLookup().lookup(mac.getText().toString());
            result.setText(getString(R.string.network_mac_result, info.getNormalized(), info.getOui(),
                    info.getVendor(), info.isLocallyAdministered(), info.isMulticast()));
        } catch (RuntimeException exception) {
            result.setText(R.string.network_invalid_input);
        }
    }

    private void showPorts() {
        StringBuilder text = new StringBuilder();
        for (PortReference entry : new CommonPortCatalog().list()) {
            text.append(entry.getPort()).append("/").append(entry.getProtocol()).append("  ")
                    .append(entry.getPurpose()).append('\n');
        }
        result.setText(text.toString());
    }

    private void run(Probe probe) {
        if (!probeRunning.compareAndSet(false, true)) {
            result.setText(R.string.network_probe_busy);
            return;
        }
        result.setText(R.string.network_running);
        worker.execute(() -> {
            try {
                String output = probe.execute();
                postResult(output);
            } catch (IllegalArgumentException exception) {
                postResult(getString(R.string.network_invalid_input));
            } catch (Exception exception) {
                postResult(getString(R.string.network_failed_generic));
            } finally {
                probeRunning.set(false);
            }
        });
    }

    private void postResult(String output) {
        runOnUiThread(() -> {
            if (!isFinishing() && !isDestroyed()) result.setText(output);
        });
    }

    @Override protected void onDestroy() {
        worker.shutdownNow();
        probes.cancelActiveProbe();
        super.onDestroy();
    }

    private interface Probe { String execute() throws Exception; }
}
