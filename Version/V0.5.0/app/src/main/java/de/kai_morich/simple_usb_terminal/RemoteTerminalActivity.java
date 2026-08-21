package de.kai_morich.simple_usb_terminal;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
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
import android.widget.CompoundButton;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.chenwei666.netserial.device.DeviceProfile;
import com.chenwei666.netserial.device.DeviceProfileStore;
import com.chenwei666.netserial.change.ChangeEvidenceRecorder;
import com.chenwei666.netserial.change.ChangeEventType;
import com.chenwei666.netserial.change.ChangeTaskStore;
import com.chenwei666.netserial.remote.JumpHostConfig;
import com.chenwei666.netserial.remote.RemoteConnection;
import com.chenwei666.netserial.remote.RemoteConnectionConfig;
import com.chenwei666.netserial.remote.RemoteConnectionListener;
import com.chenwei666.netserial.remote.RemoteConnectionState;
import com.chenwei666.netserial.remote.RemoteProtocol;
import com.chenwei666.netserial.remote.SshRemoteConnection;
import com.chenwei666.netserial.remote.SshAuthenticationMode;
import com.chenwei666.netserial.remote.SshConnectionOptions;
import com.chenwei666.netserial.remote.SshCredentials;
import com.chenwei666.netserial.remote.SftpTransferListener;
import com.chenwei666.netserial.remote.TelnetRemoteConnection;
import com.chenwei666.netserial.settings.AppLocaleController;
import com.chenwei666.netserial.settings.AppSettings;
import com.chenwei666.netserial.settings.AppSettingsStore;
import com.chenwei666.netserial.safety.CommandEvaluationRequest;
import com.chenwei666.netserial.safety.CommandBatchInspector;
import com.chenwei666.netserial.safety.GuardDecision;
import com.chenwei666.netserial.safety.RiskLevel;
import com.chenwei666.netserial.safety.RuleBasedExecutionGuard;
import com.chenwei666.netserial.safety.TargetSafetyDecision;
import com.chenwei666.netserial.safety.TargetSafetyPolicy;
import com.chenwei666.netserial.terminal.AnsiTextSanitizer;
import com.chenwei666.netserial.terminal.ControlKey;
import com.chenwei666.netserial.terminal.TerminalControlEncoder;
import com.chenwei666.netserial.terminal.TerminalTextBuffer;
import com.chenwei666.netserial.terminal.TargetColorPalette;

import java.io.File;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class RemoteTerminalActivity extends ThemedActivity implements RemoteConnectionListener {
    private static final int COMMAND_REQUEST = 5201;
    private static final int AI_REQUEST = 5202;
    private static final int PRIVATE_KEY_REQUEST = 5203;
    private static final int SFTP_DOWNLOAD_REQUEST = 5204;
    private static final int SFTP_UPLOAD_REQUEST = 5205;
    private static final int MAX_PRIVATE_KEY_BYTES = 256_000;
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
    private Spinner authModeSpinner;
    private CompoundButton jumpEnabled;
    private View jumpFields;
    private EditText jumpHost;
    private EditText jumpPort;
    private EditText jumpUser;
    private EditText jumpPassword;
    private EditText sftpPath;
    private TextView targetBanner;
    private RemoteConnection connection;
    private AppSettings settings;
    private final TerminalTextBuffer terminalBuffer = new TerminalTextBuffer(MAX_OUTPUT);
    private final AnsiTextSanitizer sanitizer = new AnsiTextSanitizer();
    private final TerminalControlEncoder controlEncoder = new TerminalControlEncoder();
    private final StringBuilder evidenceOutput = new StringBuilder();
    private byte[] privateKeyBytes = new byte[0];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
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
        authModeSpinner = findViewById(R.id.remote_auth_mode);
        jumpEnabled = findViewById(R.id.remote_jump_enabled);
        jumpFields = findViewById(R.id.remote_jump_fields);
        jumpHost = findViewById(R.id.remote_jump_host);
        jumpPort = findViewById(R.id.remote_jump_port);
        jumpUser = findViewById(R.id.remote_jump_user);
        jumpPassword = findViewById(R.id.remote_jump_password);
        sftpPath = findViewById(R.id.remote_sftp_path);
        targetBanner = findViewById(R.id.remote_target_banner);
        DeviceProfile savedProfile = new DeviceProfileStore(this).load();
        if (!savedProfile.getManagementAddress().isEmpty()) {
            hostInput.setText(savedProfile.getManagementAddress());
        }
        outputText.setTextSize(settings.getTerminalTextSizeSp());
        passwordInput.setSaveEnabled(false);
        jumpPassword.setSaveEnabled(false);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            passwordInput.setImportantForAutofill(View.IMPORTANT_FOR_AUTOFILL_NO_EXCLUDE_DESCENDANTS);
            jumpPassword.setImportantForAutofill(View.IMPORTANT_FOR_AUTOFILL_NO_EXCLUDE_DESCENDANTS);
        }
        ArrayAdapter<String> protocols = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item,
                new String[]{"SSH", "Telnet"});
        protocols.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        protocolSpinner.setAdapter(protocols);
        protocolSpinner.setOnItemSelectedListener(new SimpleItemSelectedListener(this::protocolChanged));
        ArrayAdapter<String> authentication = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,
                new String[]{getString(R.string.ssh_auth_password), getString(R.string.ssh_auth_private_key)});
        authentication.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        authModeSpinner.setAdapter(authentication);
        authModeSpinner.setOnItemSelectedListener(new SimpleItemSelectedListener(position -> authenticationChanged()));
        jumpEnabled.setOnCheckedChangeListener((button, checked) ->
                jumpFields.setVisibility(checked ? View.VISIBLE : View.GONE));
        portInput.setText(R.string.remote_default_ssh_port);
        renderTargetBanner();
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
        findViewById(R.id.remote_private_key).setOnClickListener(v -> choosePrivateKey());
        findViewById(R.id.remote_sftp_download).setOnClickListener(v -> chooseSftpDownload());
        findViewById(R.id.remote_sftp_upload).setOnClickListener(v -> chooseSftpUpload());
        findViewById(R.id.remote_config_diff).setOnClickListener(v -> startActivity(
                new Intent(this, ConfigDiffActivity.class).putExtra(ConfigDiffActivity.EXTRA_CAPTURE,
                        terminalBuffer.tail(100_000))));
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
        findViewById(R.id.remote_ssh_options).setVisibility(ssh ? View.VISIBLE : View.GONE);
        findViewById(R.id.remote_sftp_path).setEnabled(ssh);
        findViewById(R.id.remote_sftp_download).setEnabled(ssh);
        findViewById(R.id.remote_sftp_upload).setEnabled(ssh);
        renderTargetBanner();
    }

    private void authenticationChanged() {
        boolean key = authModeSpinner.getSelectedItemPosition() == 1;
        findViewById(R.id.remote_private_key).setVisibility(key ? View.VISIBLE : View.GONE);
        passwordInput.setHint(key ? R.string.ssh_passphrase_hint : R.string.remote_password_hint);
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
            renderTargetBanner();
            RemoteConnectionConfig config = new RemoteConnectionConfig(protocol,
                    hostInput.getText().toString(), Integer.parseInt(portInput.getText().toString().trim()),
                    usernameInput.getText().toString(), settings.getRemoteTimeoutMillis(), settings.getRemoteCharset());
            if (protocol == RemoteProtocol.SSH) {
                SshAuthenticationMode authenticationMode = authModeSpinner.getSelectedItemPosition() == 1
                        ? SshAuthenticationMode.PRIVATE_KEY : SshAuthenticationMode.PASSWORD;
                if (authenticationMode == SshAuthenticationMode.PRIVATE_KEY && privateKeyBytes.length == 0) {
                    Toast.makeText(this, R.string.ssh_private_key_required, Toast.LENGTH_LONG).show();
                    return;
                }
                JumpHostConfig jump = null;
                if (jumpEnabled.isChecked()) {
                    jump = new JumpHostConfig(jumpHost.getText().toString(),
                            Integer.parseInt(jumpPort.getText().toString().trim()),
                            jumpUser.getText().toString());
                }
                SshConnectionOptions options = new SshConnectionOptions(authenticationMode,
                        authenticationMode == SshAuthenticationMode.PRIVATE_KEY ? privateKeyBytes : null,
                        jump, settings.getSshKeepAliveMillis());
                connection = new SshRemoteConnection(config, this,
                        new File(getFilesDir(), "ssh_known_hosts"), options);
            } else {
                connection = new TelnetRemoteConnection(config, this);
            }
            char[] password = passwordInput.getText().toString().toCharArray();
            char[] bastionPassword = jumpPassword.getText().toString().toCharArray();
            passwordInput.setText("");
            jumpPassword.setText("");
            if (connection instanceof SshRemoteConnection) {
                ((SshRemoteConnection) connection).connect(new SshCredentials(password, bastionPassword));
            } else {
                connection.connect(password);
            }
            Arrays.fill(password, '\0');
            Arrays.fill(bastionPassword, '\0');
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
        String target = targetLabel(profile);
        TargetSafetyDecision targetDecision = new TargetSafetyPolicy().evaluate(profile,
                hostInput.getText().toString().trim(),
                decision.getEffectiveRisk(), new ChangeTaskStore(this).loadActive(), System.currentTimeMillis());
        if (!targetDecision.isAllowed()) {
            new AlertDialog.Builder(this).setTitle(R.string.target_blocked_title)
                    .setMessage(getString(R.string.target_blocked_message, target,
                            TargetSafetyMessageResolver.resolve(this, targetDecision.getReason())))
                    .setPositiveButton(android.R.string.ok, null).show();
            return;
        }
        CommandSendConfirmation.confirm(this, getString(R.string.target_command_summary, target,
                        new CommandBatchInspector().count(command)) + "\n\n" + command,
                decision.getEffectiveRisk(), () -> sendCommandNow(command, decision.getEffectiveRisk()));
    }

    private void sendCommandNow(String command, RiskLevel risk) {
        flushEvidenceOutput();
        if (risk.ordinal() >= RiskLevel.R3_HIGH.ordinal()) {
            DeviceProfile confirmedProfile = new DeviceProfileStore(this).load();
            TargetSafetyDecision confirmedDecision = new TargetSafetyPolicy().evaluate(confirmedProfile,
                    hostInput.getText().toString().trim(), risk,
                    new ChangeTaskStore(this).loadActive(), System.currentTimeMillis());
            if (!confirmedDecision.isAllowed()) {
                new AlertDialog.Builder(this).setTitle(R.string.target_blocked_title)
                        .setMessage(getString(R.string.target_blocked_message,
                                targetLabel(confirmedProfile), TargetSafetyMessageResolver.resolve(
                                        this, confirmedDecision.getReason())))
                        .setPositiveButton(android.R.string.ok, null).show();
                return;
            }
            new ChangeEvidenceRecorder(this).record(ChangeEventType.COMMAND_CONFIRMED,
                    targetLabel(confirmedProfile), risk.name() + ": " + command);
        }
        sendBytes((command + "\r\n").getBytes(settings.getRemoteCharset().equals("GBK")
                ? java.nio.charset.Charset.forName("GBK") : java.nio.charset.Charset.forName(settings.getRemoteCharset())));
        commandInput.setText("");
        DeviceProfile profile = new DeviceProfileStore(this).load();
        new ChangeEvidenceRecorder(this).record(ChangeEventType.COMMAND_SENT,
                targetLabel(profile), command);
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
        if (requestCode == PRIVATE_KEY_REQUEST && data.getData() != null) {
            loadPrivateKey(data.getData());
            return;
        }
        if (requestCode == SFTP_DOWNLOAD_REQUEST && data.getData() != null) {
            startSftpDownload(data.getData());
            return;
        }
        if (requestCode == SFTP_UPLOAD_REQUEST && data.getData() != null) {
            startSftpUpload(data.getData());
            return;
        }
        if (requestCode != COMMAND_REQUEST && requestCode != AI_REQUEST) return;
        String command = requestCode == COMMAND_REQUEST
                ? data.getStringExtra(CommandLibraryActivity.RESULT_COMMAND)
                : data.getStringExtra(AiCopilotActivity.EXTRA_SELECTED_COMMAND);
        if (command != null) commandInput.setText(command);
    }

    private void choosePrivateKey() {
        startActivityForResult(new Intent(Intent.ACTION_OPEN_DOCUMENT)
                .setType("*/*").addCategory(Intent.CATEGORY_OPENABLE), PRIVATE_KEY_REQUEST);
    }

    private void loadPrivateKey(Uri uri) {
        try (InputStream input = getContentResolver().openInputStream(uri);
             ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            if (input == null) throw new IllegalStateException();
            byte[] buffer = new byte[8192];
            int count;
            while ((count = input.read(buffer)) != -1) {
                if (output.size() + count > MAX_PRIVATE_KEY_BYTES) throw new IllegalArgumentException();
                output.write(buffer, 0, count);
            }
            byte[] loaded = output.toByteArray();
            if (loaded.length == 0) throw new IllegalArgumentException();
            Arrays.fill(privateKeyBytes, (byte) 0);
            privateKeyBytes = loaded;
            ((Button) findViewById(R.id.remote_private_key)).setText(R.string.ssh_private_key_loaded);
        } catch (Exception exception) {
            Toast.makeText(this, R.string.ssh_private_key_failed, Toast.LENGTH_LONG).show();
        }
    }

    private void chooseSftpDownload() {
        if (!validateSftpReady()) return;
        String name = new File(sftpPath.getText().toString().trim()).getName();
        if (name.isEmpty()) name = "switch-config.txt";
        startActivityForResult(new Intent(Intent.ACTION_CREATE_DOCUMENT).setType("application/octet-stream")
                .putExtra(Intent.EXTRA_TITLE, name), SFTP_DOWNLOAD_REQUEST);
    }

    private void chooseSftpUpload() {
        if (!validateSftpReady()) return;
        if (!ensureSftpUploadAuthorized()) return;
        DeviceProfile profile = new DeviceProfileStore(this).load();
        new AlertDialog.Builder(this).setTitle(R.string.sftp_upload_warning_title)
                .setMessage(getString(R.string.sftp_upload_warning_message,
                        targetLabel(profile), sftpPath.getText().toString().trim()))
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(R.string.sftp_choose_upload, (dialog, which) ->
                        startActivityForResult(new Intent(Intent.ACTION_OPEN_DOCUMENT).setType("*/*")
                                .addCategory(Intent.CATEGORY_OPENABLE), SFTP_UPLOAD_REQUEST))
                .show();
    }

    private boolean validateSftpReady() {
        if (!(connection instanceof SshRemoteConnection)
                || connection.getState() != RemoteConnectionState.CONNECTED
                || sftpPath.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, R.string.sftp_not_ready, Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    private void startSftpDownload(Uri uri) {
        try {
            OutputStream output = getContentResolver().openOutputStream(uri);
            if (output == null) throw new IllegalStateException();
            ((SshRemoteConnection) connection).download(sftpPath.getText().toString(), output,
                    transferListener("SFTP download"));
        } catch (Exception exception) {
            Toast.makeText(this, R.string.sftp_failed, Toast.LENGTH_LONG).show();
        }
    }

    private void startSftpUpload(Uri uri) {
        if (!validateSftpReady() || !ensureSftpUploadAuthorized()) return;
        try {
            InputStream input = getContentResolver().openInputStream(uri);
            if (input == null) throw new IllegalStateException();
            ((SshRemoteConnection) connection).upload(input, sftpPath.getText().toString(),
                    transferListener("SFTP upload"));
        } catch (Exception exception) {
            Toast.makeText(this, R.string.sftp_failed, Toast.LENGTH_LONG).show();
        }
    }

    private boolean ensureSftpUploadAuthorized() {
        DeviceProfile profile = new DeviceProfileStore(this).load();
        TargetSafetyDecision decision = new TargetSafetyPolicy().evaluate(profile,
                hostInput.getText().toString().trim(), RiskLevel.R3_HIGH,
                new ChangeTaskStore(this).loadActive(), System.currentTimeMillis());
        if (decision.isAllowed()) return true;
        new AlertDialog.Builder(this).setTitle(R.string.target_blocked_title)
                .setMessage(getString(R.string.target_blocked_message, targetLabel(profile),
                        TargetSafetyMessageResolver.resolve(this, decision.getReason())))
                .setPositiveButton(android.R.string.ok, null).show();
        return false;
    }

    private SftpTransferListener transferListener(String operation) {
        return new SftpTransferListener() {
            @Override public void onComplete() {
                runOnUiThread(() -> {
                    DeviceProfile profile = new DeviceProfileStore(RemoteTerminalActivity.this).load();
                    new ChangeEvidenceRecorder(RemoteTerminalActivity.this).record(
                            ChangeEventType.VERIFICATION, targetLabel(profile), operation + ": "
                                    + sftpPath.getText().toString().trim());
                    Toast.makeText(RemoteTerminalActivity.this,
                            R.string.sftp_complete, Toast.LENGTH_SHORT).show();
                });
            }
            @Override public void onError(String safeMessage) {
                runOnUiThread(() -> Toast.makeText(RemoteTerminalActivity.this,
                        getString(R.string.sftp_failed_detail, safeMessage), Toast.LENGTH_LONG).show());
            }
        };
    }

    @Override
    public void onStateChanged(RemoteConnectionState state, String detail) {
        runOnUiThread(() -> {
            renderState(state, stateLabel(state));
            DeviceProfile profile = new DeviceProfileStore(this).load();
            new ChangeEvidenceRecorder(this).record(ChangeEventType.CONNECTION,
                    targetLabel(profile), state.name());
            if (state == RemoteConnectionState.DISCONNECTED) flushEvidenceOutput();
        });
    }

    @Override
    public void onTextReceived(String text) {
        String safe = sanitizer.sanitize(text);
        runOnUiThread(() -> {
            outputText.setText(terminalBuffer.append(safe));
            appendEvidenceOutput(safe);
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
        authModeSpinner.setEnabled(disconnected);
        jumpEnabled.setEnabled(disconnected);
        jumpHost.setEnabled(disconnected);
        jumpPort.setEnabled(disconnected);
        jumpUser.setEnabled(disconnected);
        jumpPassword.setEnabled(disconnected);
        usernameInput.setEnabled(disconnected && protocolSpinner.getSelectedItemPosition() == 0);
        passwordInput.setEnabled(disconnected && protocolSpinner.getSelectedItemPosition() == 0);
        boolean sshConnected = connected && protocolSpinner.getSelectedItemPosition() == 0;
        sftpPath.setEnabled(sshConnected);
        findViewById(R.id.remote_sftp_download).setEnabled(sshConnected);
        findViewById(R.id.remote_sftp_upload).setEnabled(sshConnected);
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

    private String targetLabel(DeviceProfile profile) {
        String host = hostInput == null ? "" : hostInput.getText().toString().trim();
        String protocol = protocolSpinner == null || protocolSpinner.getSelectedItemPosition() == 0
                ? "SSH" : "Telnet";
        return protocol + " · " + profile.getName() + " · " + host + " · "
                + profile.getEnvironment().name();
    }

    private void renderTargetBanner() {
        if (targetBanner == null) return;
        DeviceProfile profile = new DeviceProfileStore(this).load();
        targetBanner.setText(getString(R.string.target_banner_format, targetLabel(profile),
                profile.getVendor().name(), profile.isProtectedDevice()
                        ? getString(R.string.target_protected) : getString(R.string.target_unprotected)));
        targetBanner.setBackgroundColor(new TargetColorPalette().colorFor(targetLabel(profile)));
    }

    private void appendEvidenceOutput(String value) {
        evidenceOutput.append(value);
        if (evidenceOutput.length() > 8_000) evidenceOutput.delete(0, evidenceOutput.length() - 8_000);
    }

    private void flushEvidenceOutput() {
        if (evidenceOutput.length() == 0) return;
        DeviceProfile profile = new DeviceProfileStore(this).load();
        new ChangeEvidenceRecorder(this).record(ChangeEventType.OUTPUT_CAPTURED,
                targetLabel(profile), evidenceOutput.toString());
        evidenceOutput.setLength(0);
    }

    @Override
    protected void onResume() {
        super.onResume();
        settings = new AppSettingsStore(this).load();
        if (outputText != null) outputText.setTextSize(settings.getTerminalTextSizeSp());
        renderTargetBanner();
    }

    @Override
    protected void onDestroy() {
        flushEvidenceOutput();
        if (connection != null) connection.disconnect();
        passwordInput.setText("");
        jumpPassword.setText("");
        Arrays.fill(privateKeyBytes, (byte) 0);
        super.onDestroy();
    }
}
