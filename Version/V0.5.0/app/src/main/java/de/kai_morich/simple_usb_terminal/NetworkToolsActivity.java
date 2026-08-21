package de.kai_morich.simple_usb_terminal;

import android.os.Bundle;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.chenwei666.netserial.network.Ipv4Calculator;
import com.chenwei666.netserial.network.Ipv4Network;
import com.chenwei666.netserial.network.Ipv6Calculator;
import com.chenwei666.netserial.network.Ipv6Network;
import com.chenwei666.netserial.network.MacAddressInfo;
import com.chenwei666.netserial.network.MacOuiLookup;
import com.chenwei666.netserial.network.CommonPortCatalog;
import com.chenwei666.netserial.network.PortReference;
import com.chenwei666.netserial.network.NetworkIdentifierExtractor;
import com.chenwei666.netserial.network.NetworkProbeService;
import com.chenwei666.netserial.settings.AppLocaleController;
import com.chenwei666.netserial.settings.AppSettingsStore;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class NetworkToolsActivity extends ThemedActivity {
    private final ExecutorService worker = Executors.newSingleThreadExecutor();
    private final NetworkProbeService probes = new NetworkProbeService();
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
        findViewById(R.id.network_tcp).setOnClickListener(view -> run(() -> probes.tcp(target.getText().toString(), port.getText().toString(), timeoutMillis)));
        findViewById(R.id.network_mtu).setOnClickListener(view -> run(() -> probes.pathMtu(target.getText().toString(), timeoutMillis)));
        findViewById(R.id.network_mac_lookup).setOnClickListener(view -> lookupMac());
        findViewById(R.id.network_ports).setOnClickListener(view -> showPorts());
        findViewById(R.id.network_extract).setOnClickListener(view ->
                result.setText(new NetworkIdentifierExtractor().extract(extractInput.getText().toString())));
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
        result.setText(R.string.network_running);
        worker.execute(() -> {
            try {
                String output = probe.execute();
                runOnUiThread(() -> result.setText(output));
            } catch (Exception exception) {
                runOnUiThread(() -> result.setText(R.string.network_failed_generic));
            }
        });
    }

    @Override protected void onDestroy() {
        worker.shutdownNow();
        super.onDestroy();
    }

    private interface Probe { String execute() throws Exception; }
}
