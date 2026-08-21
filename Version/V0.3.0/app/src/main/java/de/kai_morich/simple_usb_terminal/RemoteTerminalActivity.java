package de.kai_morich.simple_usb_terminal;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.chenwei666.netserial.device.DeviceProfile;
import com.chenwei666.netserial.device.DeviceProfileStore;
import com.chenwei666.netserial.remote.RemoteConnection;
import com.chenwei666.netserial.remote.RemoteConnectionConfig;
import com.chenwei666.netserial.remote.RemoteConnectionListener;
import com.chenwei666.netserial.remote.RemoteConnectionState;
import com.chenwei666.netserial.remote.RemoteProtocol;
import com.chenwei666.netserial.remote.SshRemoteConnection;
import com.chenwei666.netserial.remote.TelnetRemoteConnection;
import com.chenwei666.netserial.settings.AppLocaleController;
import com.chenwei666.netserial.settings.AppSettings;
import com.chenwei666.netserial.settings.AppSettingsStore;
import com.chenwei666.netserial.safety.CommandEvaluationRequest;
import com.chenwei666.netserial.safety.GuardDecision;
import com.chenwei666.netserial.safety.RiskLevel;
import com.chenwei666.netserial.safety.RuleBasedExecutionGuard;
import com.chenwei666.netserial.terminal.AnsiTextSanitizer;
import com.chenwei666.netserial.terminal.ControlKey;
import com.chenwei666.netserial.terminal.TerminalControlEncoder;
import com.chenwei666.netserial.terminal.TerminalTextBuffer;

import java.io.File;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class RemoteTerminalActivity extends AppCompatActivity implements RemoteConnectionListener {
    private static final int COMMAND_REQUEST = 5201;
    private static final int AI_REQUEST = 5202;
    private static final int MAX_OUTPUT = 200_000;

    private Spinner protocolSpinner;
    private EditText hostInput;
    private EditText portInput;
    private EditText usernameInput;
    private EditText passwordInput;
    private EditText commandInput;
    private TextView statusText;
    private TextView outputText;
    private ScrollView outputScroll;
    private Button connectButton;
    private Button sendButton;
    private RemoteConnection connection;
    private AppSettings settings;
    private final TerminalTextBuffer terminalBuffer = new TerminalTextBuffer(MAX_OUTPUT);
    private final AnsiTextSanitizer sanitizer = new AnsiTextSanitizer();
    private final TerminalControlEncoder controlEncoder = new TerminalControlEncoder();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppLocaleController.applyStoredLanguage(this);
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
        setContentView(R.layout.activity_remote_terminal);
        setTitle(R.string.remote_terminal_title);
        settings = new AppSettingsStore(this).load();
        bindViews();
        bindActions();
        renderState(RemoteConnectionState.DISCONNECTED, getString(R.string.remote_disconnected));
    }

    private void bindViews() {
        protocolSpinner = findViewById(R.id.remote_protocol);
        hostInput = findViewById(R.id.remote_host);
        portInput = findViewById(R.id.remote_port);
        usernameInput = findViewById(R.id.remote_username);
        passwordInput = findViewById(R.id.remote_password);
        commandInput = findViewById(R.id.remote_command);
        statusText = findViewById(R.id.remote_status);
        outputText = findViewById(R.id.remote_output);
        outputScroll = findViewById(R.id.remote_output_scroll);
        connectButton = findViewById(R.id.remote_connect);
        sendButton = findViewById(R.id.remote_send);
        outputText.setTextSize(settings.getTerminalTextSizeSp());
        passwordInput.setSaveEnabled(false);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            passwordInput.setImportantForAutofill(View.IMPORTANT_FOR_AUTOFILL_NO_EXCLUDE_DESCENDANTS);
        }
        ArrayAdapter<String> protocols = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item,
                new String[]{"SSH", "Telnet"});
        protocols.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        protocolSpinner.setAdapter(protocols);
        protocolSpinner.setOnItemSelectedListener(new SimpleItemSelectedListener(this::protocolChanged));
        portInput.setText(R.string.remote_default_ssh_port);
    }

    private void bindActions() {
        connectButton.setOnClickListener(v -> toggleConnection());
        sendButton.setOnClickListener(v -> sendCommand());
        findViewById(R.id.remote_clear).setOnClickListener(v -> {
            terminalBuffer.clear();
            outputText.setText("");
        });
        findViewById(R.id.remote_commands).setOnClickListener(v -> openCommandLibrary());
        findViewById(R.id.remote_ai).setOnClickListener(v -> openAi());
        findViewById(R.id.remote_tab).setOnClickListener(v -> sendControl(ControlKey.TAB));
        findViewById(R.id.remote_esc).setOnClickListener(v -> sendControl(ControlKey.ESCAPE));
        findViewById(R.id.remote_ctrl_c).setOnClickListener(v -> sendControl(ControlKey.CTRL_C));
        findViewById(R.id.remote_up).setOnClickListener(v -> sendControl(ControlKey.ARROW_UP));
        findViewById(R.id.remote_down).setOnClickListener(v -> sendControl(ControlKey.ARROW_DOWN));
        findViewById(R.id.remote_question).setOnClickListener(v -> sendControl(ControlKey.QUESTION_MARK));
    }

    private void protocolChanged(int position) {
        RemoteProtocol protocol = position == 0 ? RemoteProtocol.SSH : RemoteProtocol.TELNET;
        String current = portInput.getText().toString().trim();
        if (current.isEmpty() || current.equals("22") || current.equals("23")) {
            portInput.setText(String.valueOf(protocol.getDefaultPort()));
        }
        boolean ssh = protocol == RemoteProtocol.SSH;
        usernameInput.setEnabled(ssh);
        passwordInput.setEnabled(ssh);
        passwordInput.setHint(ssh ? R.string.remote_password_hint : R.string.remote_telnet_login_hint);
    }

    private void toggleConnection() {
        if (connection != null && connection.getState() != RemoteConnectionState.DISCONNECTED) {
            connection.disconnect();
            return;
        }
        RemoteProtocol protocol = protocolSpinner.getSelectedItemPosition() == 0 ? RemoteProtocol.SSH : RemoteProtocol.TELNET;
        if (protocol == RemoteProtocol.TELNET) {
            if (!settings.isTelnetEnabled()) {
                new AlertDialog.Builder(this).setTitle(R.string.telnet_disabled_title)
                        .setMessage(R.string.telnet_disabled_message)
                        .setPositiveButton(R.string.open_settings, (dialog, which) -> startActivity(new Intent(this, AppSettingsActivity.class)))
                        .setNegativeButton(android.R.string.cancel, null).show();
                return;
            }
            new AlertDialog.Builder(this).setTitle(R.string.telnet_warning_title)
                    .setMessage(R.string.telnet_warning_message)
                    .setNegativeButton(android.R.string.cancel, null)
                    .setPositiveButton(R.string.continue_connection, (dialog, which) -> connect(protocol))
                    .show();
            return;
        }
        connect(protocol);
    }

    private void connect(RemoteProtocol protocol) {
        try {
            RemoteConnectionConfig config = new RemoteConnectionConfig(protocol,
                    hostInput.getText().toString(), Integer.parseInt(portInput.getText().toString().trim()),
                    usernameInput.getText().toString(), settings.getRemoteTimeoutMillis(), settings.getRemoteCharset());
            if (protocol == RemoteProtocol.SSH) {
                connection = new SshRemoteConnection(config, this, new File(getFilesDir(), "ssh_known_hosts"));
            } else {
                connection = new TelnetRemoteConnection(config, this);
            }
            char[] password = passwordInput.getText().toString().toCharArray();
            passwordInput.setText("");
            connection.connect(password);
            Arrays.fill(password, '\0');
        } catch (RuntimeException exception) {
            Toast.makeText(this, getString(R.string.remote_invalid_config), Toast.LENGTH_LONG).show();
        }
    }

    private void sendCommand() {
        String command = commandInput.getText().toString();
        if (command.trim().isEmpty()) return;
        DeviceProfile profile = new DeviceProfileStore(this).load();
        GuardDecision decision = RuleBasedExecutionGuard.createDefault().evaluate(
                new CommandEvaluationRequest(profile.getVendor(), profile.getCliMode(), command, RiskLevel.R0_INFORMATIONAL));
        CommandSendConfirmation.confirm(this, command, decision.getEffectiveRisk(), () -> sendCommandNow(command));
    }

    private void sendCommandNow(String command) {
        sendBytes((command + "\r\n").getBytes(settings.getRemoteCharset().equals("GBK")
                ? java.nio.charset.Charset.forName("GBK") : java.nio.charset.Charset.forName(settings.getRemoteCharset())));
        commandInput.setText("");
    }

    private void sendControl(ControlKey key) {
        sendBytes(controlEncoder.encode(key));
    }

    private void sendBytes(byte[] bytes) {
        if (connection == null || connection.getState() != RemoteConnectionState.CONNECTED) {
            Toast.makeText(this, R.string.remote_not_connected, Toast.LENGTH_SHORT).show();
            return;
        }
        connection.send(bytes);
    }

    private void openCommandLibrary() {
        DeviceProfile profile = new DeviceProfileStore(this).load();
        Intent intent = new Intent(this, CommandLibraryActivity.class)
                .putExtra(CommandLibraryActivity.EXTRA_VENDOR, profile.getVendor().name())
                .putExtra(CommandLibraryActivity.EXTRA_PICK_MODE, true);
        startActivityForResult(intent, COMMAND_REQUEST);
    }

    private void openAi() {
        Intent intent = new Intent(this, AiCopilotActivity.class)
                .putExtra(AiCopilotActivity.EXTRA_TERMINAL_CONTEXT, terminalBuffer.tail(12_000));
        startActivityForResult(intent, AI_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != Activity.RESULT_OK || data == null) return;
        String command = requestCode == COMMAND_REQUEST
                ? data.getStringExtra(CommandLibraryActivity.RESULT_COMMAND)
                : data.getStringExtra(AiCopilotActivity.EXTRA_SELECTED_COMMAND);
        if (command != null) commandInput.setText(command);
    }

    @Override
    public void onStateChanged(RemoteConnectionState state, String detail) {
        runOnUiThread(() -> renderState(state, stateLabel(state)));
    }

    @Override
    public void onTextReceived(String text) {
        String safe = sanitizer.sanitize(text);
        runOnUiThread(() -> {
            outputText.setText(terminalBuffer.append(safe));
            outputScroll.post(() -> outputScroll.fullScroll(View.FOCUS_DOWN));
        });
    }

    @Override
    public void onError(String safeMessage) {
        runOnUiThread(() -> appendStatus(getString(R.string.remote_error_format, safeMessage)));
    }

    @Override
    public boolean confirmUnknownSshHost(String verificationMessage) {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicBoolean accepted = new AtomicBoolean(false);
        runOnUiThread(() -> new AlertDialog.Builder(this)
                .setTitle(R.string.ssh_unknown_host_title)
                .setMessage(getString(R.string.ssh_unknown_host_message, verificationMessage))
                .setCancelable(false)
                .setNegativeButton(android.R.string.cancel, (dialog, which) -> latch.countDown())
                .setPositiveButton(R.string.ssh_trust_host, (dialog, which) -> {
                    accepted.set(true);
                    latch.countDown();
                }).show());
        try {
            return latch.await(120, TimeUnit.SECONDS) && accepted.get();
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    private void renderState(RemoteConnectionState state, String label) {
        statusText.setText(label);
        boolean disconnected = state == RemoteConnectionState.DISCONNECTED;
        boolean connected = state == RemoteConnectionState.CONNECTED;
        connectButton.setText(disconnected ? R.string.remote_connect : R.string.remote_disconnect);
        hostInput.setEnabled(disconnected);
        portInput.setEnabled(disconnected);
        protocolSpinner.setEnabled(disconnected);
        usernameInput.setEnabled(disconnected && protocolSpinner.getSelectedItemPosition() == 0);
        passwordInput.setEnabled(disconnected && protocolSpinner.getSelectedItemPosition() == 0);
        sendButton.setEnabled(connected);
    }

    private String stateLabel(RemoteConnectionState state) {
        switch (state) {
            case CONNECTING: return getString(R.string.remote_connecting);
            case CONNECTED: return getString(R.string.remote_connected);
            case DISCONNECTING: return getString(R.string.remote_disconnecting);
            default: return getString(R.string.remote_disconnected);
        }
    }

    private void appendStatus(String message) {
        outputText.setText(terminalBuffer.append("\r\n[" + message + "]\r\n"));
    }

    @Override
    protected void onResume() {
        super.onResume();
        settings = new AppSettingsStore(this).load();
        if (outputText != null) outputText.setTextSize(settings.getTerminalTextSizeSp());
    }

    @Override
    protected void onDestroy() {
        if (connection != null) connection.disconnect();
        passwordInput.setText("");
        super.onDestroy();
    }
}
