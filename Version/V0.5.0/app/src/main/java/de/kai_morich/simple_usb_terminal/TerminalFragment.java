package de.kai_morich.simple_usb_terminal;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.content.ServiceConnection;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.TextUtils;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.method.ScrollingMovementMethod;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.hoho.android.usbserial.driver.SerialTimeoutException;
import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.driver.UsbSerialProber;
import com.hoho.android.usbserial.util.XonXoffFilter;

import com.chenwei666.netserial.terminal.ControlKey;
import com.chenwei666.netserial.terminal.TerminalControlEncoder;
import com.chenwei666.netserial.terminal.TerminalTextBuffer;
import com.chenwei666.netserial.terminal.TargetColorPalette;
import com.chenwei666.netserial.terminal.AnsiTextSanitizer;
import com.chenwei666.netserial.terminal.SensitiveTextRedactor;
import com.chenwei666.netserial.completion.CompletionResult;
import com.chenwei666.netserial.completion.CompletionRequest;
import com.chenwei666.netserial.completion.CompletionSuggestion;
import com.chenwei666.netserial.completion.OfflineCompletionEngine;
import com.chenwei666.netserial.device.DeviceProfile;
import com.chenwei666.netserial.device.DeviceProfileStore;
import com.chenwei666.netserial.change.ChangeEvidenceRecorder;
import com.chenwei666.netserial.change.ChangeEventType;
import com.chenwei666.netserial.change.ChangeTaskStore;
import com.chenwei666.netserial.safety.CommandEvaluationRequest;
import com.chenwei666.netserial.safety.CommandBatchInspector;
import com.chenwei666.netserial.safety.GuardDecision;
import com.chenwei666.netserial.safety.RiskLevel;
import com.chenwei666.netserial.safety.RuleBasedExecutionGuard;
import com.chenwei666.netserial.safety.TargetSafetyDecision;
import com.chenwei666.netserial.safety.TargetSafetyPolicy;
import com.chenwei666.netserial.settings.AppSettingsStore;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;

public class TerminalFragment extends Fragment implements ServiceConnection, SerialListener {
    private static final int AI_COPILOT_REQUEST = 4101;
    private static final int EXPORT_SESSION_REQUEST = 4102;
    private static final int COMMAND_LIBRARY_REQUEST = 4103;
    private static final int MAX_TERMINAL_VIEW_CHARACTERS = 200_000;

    private enum Connected { False, Pending, True }

    private final Handler mainLooper;
    private final BroadcastReceiver broadcastReceiver;
    private int deviceId, portNum, baudRate;
    private UsbSerialPort usbSerialPort;
    private SerialService service;

    private TextView receiveText;
    private EditText sendText;
    private ImageButton sendBtn;
    private LinearLayout completionResults;
    private TextUtil.HexWatcher hexWatcher;
    private final TerminalControlEncoder controlEncoder = new TerminalControlEncoder();
    private final TerminalTextBuffer terminalBuffer = new TerminalTextBuffer(200_000);
    private final OfflineCompletionEngine completionEngine = OfflineCompletionEngine.createDefault();
    private DeviceProfile deviceProfile = DeviceProfile.defaults();
    private TextView targetBanner;
    private final StringBuilder evidenceOutput = new StringBuilder();

    private Connected connected = Connected.False;
    private boolean initialStart = true;
    private boolean hexEnabled = false;
    private enum SendButtonState {Idle, Busy, Disabled};

    private ControlLines controlLines = new ControlLines();
    private XonXoffFilter flowControlFilter;

    private boolean pendingNewline = false;
    private String newline = TextUtil.newline_crlf;

    public TerminalFragment() {
        mainLooper = new Handler(Looper.getMainLooper());
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if(Constants.INTENT_ACTION_GRANT_USB.equals(intent.getAction())) {
                    Boolean granted = intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false);
                    connect(granted);
                }
            }
        };
    }

    /*
     * Lifecycle
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        setRetainInstance(true);
        deviceId = getArguments().getInt("device");
        portNum = getArguments().getInt("port");
        baudRate = getArguments().getInt("baud");
    }

    @Override
    public void onDestroy() {
        if (connected != Connected.False)
            disconnect();
        getActivity().stopService(new Intent(getActivity(), SerialService.class));
        super.onDestroy();
    }

    @Override
    public void onStart() {
        super.onStart();
        if(service != null)
            service.attach(this);
        else
            getActivity().startService(new Intent(getActivity(), SerialService.class)); // prevents service destroy on unbind from recreated activity caused by orientation change
        ContextCompat.registerReceiver(getActivity(), broadcastReceiver, new IntentFilter(Constants.INTENT_ACTION_GRANT_USB), ContextCompat.RECEIVER_NOT_EXPORTED);
    }

    @Override
    public void onStop() {
        getActivity().unregisterReceiver(broadcastReceiver);
        if(service != null && !getActivity().isChangingConfigurations())
            service.detach();
        super.onStop();
    }

    @SuppressWarnings("deprecation") // onAttach(context) was added with API 23. onAttach(activity) works for all API versions
    @Override
    public void onAttach(@NonNull Activity activity) {
        super.onAttach(activity);
        getActivity().bindService(new Intent(getActivity(), SerialService.class), this, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onDetach() {
        try { getActivity().unbindService(this); } catch(Exception ignored) {}
        super.onDetach();
    }

    @Override
    public void onResume() {
        super.onResume();
        deviceProfile = new DeviceProfileStore(requireContext()).load();
        renderTargetBanner();
        if (receiveText != null) {
            receiveText.setTextSize(new AppSettingsStore(requireContext()).load().getTerminalTextSizeSp());
        }
        if(initialStart && service != null) {
            initialStart = false;
            getActivity().runOnUiThread(this::connect);
        }
        if(connected == Connected.True)
            controlLines.start();
    }

    @Override
    public void onPause() {
        controlLines.stop();
        super.onPause();
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder binder) {
        service = ((SerialService.SerialBinder) binder).getService();
        service.attach(this);
        if(initialStart && isResumed()) {
            initialStart = false;
            getActivity().runOnUiThread(this::connect);
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        service = null;
    }

    /*
     * UI
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_terminal, container, false);
        receiveText = view.findViewById(R.id.receive_text);                          // TextView performance decreases with number of spans
        targetBanner = view.findViewById(R.id.terminal_target_banner);
        receiveText.setTextColor(getResources().getColor(R.color.colorRecieveText)); // set as default color to reduce number of spans
        receiveText.setMovementMethod(ScrollingMovementMethod.getInstance());

        sendText = view.findViewById(R.id.send_text);
        sendBtn = view.findViewById(R.id.send_btn);
        hexWatcher = new TextUtil.HexWatcher(sendText);
        hexWatcher.enable(hexEnabled);
        sendText.addTextChangedListener(hexWatcher);
        sendText.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateCompletions(s == null ? "" : s.toString());
            }
            @Override public void afterTextChanged(Editable s) { }
        });
        sendText.setHint(hexEnabled ? "HEX mode" : "");

        View sendBtn = view.findViewById(R.id.send_btn);
        sendBtn.setOnClickListener(v -> send(sendText.getText().toString()));
        view.findViewById(R.id.tab_btn).setOnClickListener(v -> sendControl(ControlKey.TAB));
        view.findViewById(R.id.ai_btn).setOnClickListener(v -> openAiCopilot());
        bindControl(view, R.id.esc_btn, ControlKey.ESCAPE);
        bindControl(view, R.id.ctrl_c_btn, ControlKey.CTRL_C);
        bindControl(view, R.id.ctrl_z_btn, ControlKey.CTRL_Z);
        bindControl(view, R.id.up_btn, ControlKey.ARROW_UP);
        bindControl(view, R.id.down_btn, ControlKey.ARROW_DOWN);
        bindControl(view, R.id.left_btn, ControlKey.ARROW_LEFT);
        bindControl(view, R.id.right_btn, ControlKey.ARROW_RIGHT);
        bindControl(view, R.id.backspace_btn, ControlKey.BACKSPACE);
        bindControl(view, R.id.delete_btn, ControlKey.DELETE);
        bindControl(view, R.id.question_btn, ControlKey.QUESTION_MARK);
        bindControl(view, R.id.pipe_btn, ControlKey.PIPE);
        completionResults = view.findViewById(R.id.completion_results);
        deviceProfile = new DeviceProfileStore(requireContext()).load();
        renderTargetBanner();
        controlLines.onCreateView(view);
        return view;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_terminal, menu);
    }

    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        menu.findItem(R.id.hex).setChecked(hexEnabled);
        controlLines.onPrepareOptionsMenu(menu);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            menu.findItem(R.id.backgroundNotification).setChecked(service != null && service.areNotificationsEnabled());
        } else {
            menu.findItem(R.id.backgroundNotification).setChecked(true);
            menu.findItem(R.id.backgroundNotification).setEnabled(false);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.clear) {
            receiveText.setText("");
            terminalBuffer.clear();
            return true;
        } else if (id == R.id.newline) {
            String[] newlineNames = getResources().getStringArray(R.array.newline_names);
            String[] newlineValues = getResources().getStringArray(R.array.newline_values);
            int pos = Arrays.asList(newlineValues).indexOf(newline);
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("Newline");
            builder.setSingleChoiceItems(newlineNames, pos, (dialog, which) -> {
                newline = newlineValues[which];
                dialog.dismiss();
            });
            builder.create().show();
            return true;
        } else if (id == R.id.hex) {
            hexEnabled = !hexEnabled;
            sendText.setText("");
            hexWatcher.enable(hexEnabled);
            sendText.setHint(hexEnabled ? "HEX mode" : "");
            item.setChecked(hexEnabled);
            return true;
        } else if (id == R.id.controlLines) {
            item.setChecked(controlLines.showControlLines(!item.isChecked()));
            return true;
        } else if (id == R.id.flowControl) {
            controlLines.selectFlowControl();
            return true;
        } else if (id == R.id.backgroundNotification) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (!service.areNotificationsEnabled() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 0);
                } else {
                    showNotificationSettings();
                }
            }
            return true;
        } else if (id == R.id.sendBreak) {
            try {
                usbSerialPort.setBreak(true);
                Thread.sleep(100);
                status("send BREAK");
                usbSerialPort.setBreak(false);
            } catch (Exception e) {
                status("send BREAK failed: " + e.getMessage());
            }
            return true;
        } else if (id == R.id.ai_settings) {
            startActivity(new Intent(getActivity(), AiProviderSettingsActivity.class));
            return true;
        } else if (id == R.id.device_memory) {
            startActivity(new Intent(getActivity(), DeviceMemoryActivity.class));
            return true;
        } else if (id == R.id.export_session) {
            Intent export = new Intent(Intent.ACTION_CREATE_DOCUMENT)
                    .setType("text/plain")
                    .putExtra(Intent.EXTRA_TITLE, "netserial-redacted-session.txt");
            startActivityForResult(export, EXPORT_SESSION_REQUEST);
            return true;
        } else if (id == R.id.change_task) {
            startActivity(new Intent(requireContext(), ChangeTaskActivity.class));
            return true;
        } else if (id == R.id.config_diff) {
            startActivity(new Intent(requireContext(), ConfigDiffActivity.class)
                    .putExtra(ConfigDiffActivity.EXTRA_CAPTURE, terminalBuffer.tail(100_000)));
            return true;
        } else if (id == R.id.command_library) {
            Intent library = new Intent(requireContext(), CommandLibraryActivity.class)
                    .putExtra(CommandLibraryActivity.EXTRA_VENDOR, deviceProfile.getVendor().name())
                    .putExtra(CommandLibraryActivity.EXTRA_PICK_MODE, true);
            startActivityForResult(library, COMMAND_LIBRARY_REQUEST);
            return true;
        } else if (id == R.id.remote_terminal) {
            startActivity(new Intent(requireContext(), RemoteTerminalActivity.class));
            return true;
        } else if (id == R.id.app_settings) {
            startActivity(new Intent(requireContext(), AppSettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /*
     * Serial + UI
     */
    private void connect() {
        connect(null);
    }

    private void connect(Boolean permissionGranted) {
        UsbDevice device = null;
        UsbManager usbManager = (UsbManager) getActivity().getSystemService(Context.USB_SERVICE);
        for(UsbDevice v : usbManager.getDeviceList().values())
            if(v.getDeviceId() == deviceId)
                device = v;
        if(device == null) {
            status("connection failed: device not found");
            return;
        }
        UsbSerialDriver driver = UsbSerialProber.getDefaultProber().probeDevice(device);
        if(driver == null) {
            driver = CustomProber.getCustomProber().probeDevice(device);
        }
        if(driver == null) {
            status("connection failed: no driver for device");
            return;
        }
        if(driver.getPorts().size() < portNum) {
            status("connection failed: not enough ports at device");
            return;
        }
        usbSerialPort = driver.getPorts().get(portNum);
        UsbDeviceConnection usbConnection = usbManager.openDevice(driver.getDevice());
        if(usbConnection == null && permissionGranted == null && !usbManager.hasPermission(driver.getDevice())) {
            int flags = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? PendingIntent.FLAG_MUTABLE : 0;
            Intent intent = new Intent(Constants.INTENT_ACTION_GRANT_USB);
            intent.setPackage(getActivity().getPackageName());
            PendingIntent usbPermissionIntent = PendingIntent.getBroadcast(getActivity(), 0, intent, flags);
            usbManager.requestPermission(driver.getDevice(), usbPermissionIntent);
            return;
        }
        if(usbConnection == null) {
            if (!usbManager.hasPermission(driver.getDevice()))
                status("connection failed: permission denied");
            else
                status("connection failed: open failed");
            return;
        }

        connected = Connected.Pending;
        try {
            usbSerialPort.open(usbConnection);
            try {
                usbSerialPort.setParameters(baudRate, UsbSerialPort.DATABITS_8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE);
            } catch (UnsupportedOperationException e) {
                status("Setting serial parameters failed: " + e.getMessage());
            }
            SerialSocket socket = new SerialSocket(getActivity().getApplicationContext(), usbConnection, usbSerialPort);
            service.connect(socket);
            // usb connect is not asynchronous. connect-success and connect-error are returned immediately from socket.connect
            // for consistency to bluetooth/bluetooth-LE app use same SerialListener and SerialService classes
            onSerialConnect();
        } catch (Exception e) {
            onSerialConnectError(e);
        }
    }

    private void disconnect() {
        connected = Connected.False;
        controlLines.stop();
        service.disconnect();
        updateSendBtn(SendButtonState.Idle);
        usbSerialPort = null;
    }

    private void send(String str) {
        if(connected != Connected.True) {
            Toast.makeText(getActivity(), R.string.remote_disconnected, Toast.LENGTH_SHORT).show();
            return;
        }
        if (!hexEnabled && !str.trim().isEmpty()) {
            GuardDecision decision = RuleBasedExecutionGuard.createDefault().evaluate(
                    new CommandEvaluationRequest(deviceProfile.getVendor(), deviceProfile.getCliMode(),
                            str, RiskLevel.R0_INFORMATIONAL));
            TargetSafetyDecision targetDecision = new TargetSafetyPolicy().evaluate(
                    deviceProfile, deviceProfile.getName(), decision.getEffectiveRisk(),
                    new ChangeTaskStore(requireContext()).loadActive(), System.currentTimeMillis());
            if (!targetDecision.isAllowed()) {
                new AlertDialog.Builder(requireContext()).setTitle(R.string.target_blocked_title)
                        .setMessage(getString(R.string.target_blocked_message,
                                targetLabel(), TargetSafetyMessageResolver.resolve(
                                        requireContext(), targetDecision.getReason())))
                        .setPositiveButton(android.R.string.ok, null).show();
                return;
            }
            if (decision.getEffectiveRisk().ordinal() >= RiskLevel.R3_HIGH.ordinal()) {
                CommandSendConfirmation.confirm(requireContext(), getString(R.string.target_command_summary,
                                targetLabel(), new CommandBatchInspector().count(str)) + "\n\n" + str,
                        decision.getEffectiveRisk(), () -> {
                            sendHighRiskAfterRevalidation(str, decision.getEffectiveRisk());
                        });
                return;
            }
        }
        sendUnchecked(str);
    }

    private void sendHighRiskAfterRevalidation(String command, RiskLevel risk) {
        DeviceProfile currentProfile = new DeviceProfileStore(requireContext()).load();
        TargetSafetyDecision currentDecision = new TargetSafetyPolicy().evaluate(currentProfile,
                currentProfile.getName(), risk, new ChangeTaskStore(requireContext()).loadActive(),
                System.currentTimeMillis());
        if (!currentDecision.isAllowed()) {
            new AlertDialog.Builder(requireContext()).setTitle(R.string.target_blocked_title)
                    .setMessage(getString(R.string.target_blocked_message, targetLabel(),
                            TargetSafetyMessageResolver.resolve(requireContext(),
                                    currentDecision.getReason())))
                    .setPositiveButton(android.R.string.ok, null).show();
            return;
        }
        new ChangeEvidenceRecorder(requireContext()).record(ChangeEventType.COMMAND_CONFIRMED,
                targetLabel(), risk.name() + ": " + command);
        sendUnchecked(command);
    }

    private void sendUnchecked(String str) {
        flushEvidenceOutput();
        String msg;
        byte[] data;
        if(hexEnabled) {
            StringBuilder sb = new StringBuilder();
            TextUtil.toHexString(sb, TextUtil.fromHexString(str));
            TextUtil.toHexString(sb, newline.getBytes());
            msg = sb.toString();
            data = TextUtil.fromHexString(msg);
        } else {
            msg = str;
            data = (str + newline).getBytes();
        }
        try {
            SpannableStringBuilder spn = new SpannableStringBuilder(msg + '\n');
            spn.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.colorSendText)), 0, spn.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            receiveText.append(spn);
            terminalBuffer.append(msg + "\n");
            new ChangeEvidenceRecorder(requireContext()).record(ChangeEventType.COMMAND_SENT,
                    targetLabel(), msg);
            trimTerminalView();
            service.write(data);
        } catch (SerialTimeoutException e) { // e.g. writing large data at low baud rate or suspended by flow control
            mainLooper.post(() -> sendAgain(data, e.bytesTransferred));
        } catch (Exception e) {
            onSerialIoError(e);
        }
    }

    private void sendControl(ControlKey key) {
        if (connected != Connected.True) {
            Toast.makeText(getActivity(), R.string.remote_disconnected, Toast.LENGTH_SHORT).show();
            return;
        }
        byte[] data = controlEncoder.encode(key);
        try {
            service.write(data);
        } catch (SerialTimeoutException e) {
            mainLooper.post(() -> sendAgain(data, e.bytesTransferred));
        } catch (Exception e) {
            onSerialIoError(e);
        }
    }

    private void sendAgain(byte[] data0, int offset) {
        updateSendBtn(controlLines.sendAllowed ? SendButtonState.Busy : SendButtonState.Disabled);
        if (connected != Connected.True) {
            return;
        }
        byte[] data;
        if (offset == 0) {
            data = data0;
        } else {
            data = new byte[data0.length - offset];
            System.arraycopy(data0, offset, data, 0, data.length);
        }
        try {
            service.write(data);
        } catch (SerialTimeoutException e) {
            mainLooper.post(() -> sendAgain(data, e.bytesTransferred));
            return;
        } catch (Exception e) {
            onSerialIoError(e);
        }
        updateSendBtn(controlLines.sendAllowed ? SendButtonState.Idle : SendButtonState.Disabled);
    }

    private void receive(ArrayDeque<byte[]> datas) {
        SpannableStringBuilder spn = new SpannableStringBuilder();
        for (byte[] data : datas) {
            if (flowControlFilter != null)
                data = flowControlFilter.filter(data);
            if (hexEnabled) {
                spn.append(TextUtil.toHexString(data)).append('\n');
            } else {
                String msg = new String(data);
                if (newline.equals(TextUtil.newline_crlf) && msg.length() > 0) {
                    // don't show CR as ^M if directly before LF
                    msg = msg.replace(TextUtil.newline_crlf, TextUtil.newline_lf);
                    // special handling if CR and LF come in separate fragments
                    if (pendingNewline && msg.charAt(0) == '\n') {
                        if(spn.length() >= 2) {
                            spn.delete(spn.length() - 2, spn.length());
                        } else {
                            Editable edt = receiveText.getEditableText();
                            if (edt != null && edt.length() >= 2)
                                edt.delete(edt.length() - 2, edt.length());
                        }
                    }
                    pendingNewline = msg.charAt(msg.length() - 1) == '\r';
                }
                spn.append(TextUtil.toCaretString(msg, newline.length() != 0));
            }
        }
        receiveText.append(spn);
        terminalBuffer.append(spn.toString());
        appendEvidenceOutput(spn.toString());
        trimTerminalView();
    }

    void status(String str) {
        SpannableStringBuilder spn = new SpannableStringBuilder(str + '\n');
        spn.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.colorStatusText)), 0, spn.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        receiveText.append(spn);
        terminalBuffer.append(str + "\n");
        trimTerminalView();
    }

    private String targetLabel() {
        return "USB · " + deviceProfile.getName() + " · " + deviceProfile.getEnvironment().name();
    }

    private void renderTargetBanner() {
        if (targetBanner != null) {
            targetBanner.setText(getString(R.string.target_banner_format, targetLabel(),
                    deviceProfile.getVendor().name(), deviceProfile.isProtectedDevice()
                            ? getString(R.string.target_protected) : getString(R.string.target_unprotected)));
            targetBanner.setBackgroundColor(new TargetColorPalette().colorFor(targetLabel()));
        }
    }

    private void appendEvidenceOutput(String value) {
        evidenceOutput.append(value);
        if (evidenceOutput.length() > 8_000) evidenceOutput.delete(0, evidenceOutput.length() - 8_000);
    }

    private void flushEvidenceOutput() {
        if (evidenceOutput.length() == 0 || getContext() == null) return;
        new ChangeEvidenceRecorder(requireContext()).record(ChangeEventType.OUTPUT_CAPTURED,
                targetLabel(), evidenceOutput.toString());
        evidenceOutput.setLength(0);
    }

    @Override public void onDestroyView() {
        flushEvidenceOutput();
        targetBanner = null;
        super.onDestroyView();
    }

    private void bindControl(View root, int viewId, ControlKey key) {
        root.findViewById(viewId).setOnClickListener(view -> sendControl(key));
    }

    private void updateCompletions(String input) {
        if (completionResults == null) return;
        completionResults.removeAllViews();
        if (hexEnabled || input.trim().isEmpty()) return;
        CompletionResult result = completionEngine.complete(new CompletionRequest(
                deviceProfile.getVendor(), deviceProfile.getCliMode(), input, 5));
        for (CompletionSuggestion suggestion : result.getSuggestions()) {
            Button button = new Button(requireContext());
            button.setAllCaps(false);
            button.setText(suggestion.getInsertion());
            button.setOnClickListener(view -> {
                sendText.setText(suggestion.getInsertion());
                sendText.setSelection(sendText.length());
            });
            completionResults.addView(button);
        }
    }

    private void openAiCopilot() {
        Intent intent = new Intent(requireContext(), AiCopilotActivity.class);
        intent.putExtra(AiCopilotActivity.EXTRA_TERMINAL_CONTEXT, terminalBuffer.tail(12_000));
        startActivityForResult(intent, AI_COPILOT_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == AI_COPILOT_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            String command = data.getStringExtra(AiCopilotActivity.EXTRA_SELECTED_COMMAND);
            if (command != null) {
                sendText.setText(command);
                sendText.setSelection(command.length());
            }
        } else if (requestCode == COMMAND_LIBRARY_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            String command = data.getStringExtra(CommandLibraryActivity.RESULT_COMMAND);
            if (command != null) {
                sendText.setText(command);
                sendText.setSelection(command.length());
            }
        } else if (requestCode == EXPORT_SESSION_REQUEST && resultCode == Activity.RESULT_OK
                && data != null && data.getData() != null) {
            exportSession(data.getData());
        }
    }

    private void exportSession(Uri uri) {
        String sanitized = new SensitiveTextRedactor().redact(
                new AnsiTextSanitizer().sanitize(terminalBuffer.snapshot()));
        try (OutputStream output = requireContext().getContentResolver().openOutputStream(uri)) {
            if (output == null) throw new IOException("unable to open export destination");
            output.write(sanitized.getBytes(StandardCharsets.UTF_8));
            Toast.makeText(requireContext(), R.string.terminal_export_success, Toast.LENGTH_SHORT).show();
        } catch (IOException exception) {
            Toast.makeText(requireContext(), R.string.terminal_export_failed, Toast.LENGTH_SHORT).show();
        }
    }

    private void trimTerminalView() {
        Editable content = receiveText.getEditableText();
        if (content != null && content.length() > MAX_TERMINAL_VIEW_CHARACTERS) {
            int overflow = content.length() - MAX_TERMINAL_VIEW_CHARACTERS;
            int newlineIndex = TextUtils.indexOf(content, '\n', overflow);
            content.delete(0, newlineIndex >= 0 ? newlineIndex + 1 : overflow);
        }
    }

    void updateSendBtn(SendButtonState state) {
        sendBtn.setEnabled(state == SendButtonState.Idle);
        sendBtn.setImageAlpha(state == SendButtonState.Idle ? 255 : 64);
        sendBtn.setImageResource(state == SendButtonState.Disabled ? R.drawable.ic_block_white_24dp : R.drawable.ic_send_white_24dp);
    }

    /*
     * starting with Android 14, notifications are not shown in notification bar by default when App is in background
     */

    private void showNotificationSettings() {
        Intent intent = new Intent();
        intent.setAction("android.settings.APP_NOTIFICATION_SETTINGS");
        intent.putExtra("android.provider.extra.APP_PACKAGE", getActivity().getPackageName());
        startActivity(intent);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(Arrays.equals(permissions, new String[]{Manifest.permission.POST_NOTIFICATIONS}) &&
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && !service.areNotificationsEnabled())
            showNotificationSettings();
    }

    /*
     * SerialListener
     */
    @Override
    public void onSerialConnect() {
        status("connected");
        connected = Connected.True;
        controlLines.start();
    }

    @Override
    public void onSerialConnectError(Exception e) {
        status("connection failed: " + e.getMessage());
        disconnect();
    }

    @Override
    public void onSerialRead(byte[] data) {
        ArrayDeque<byte[]> datas = new ArrayDeque<>();
        datas.add(data);
        receive(datas);
    }

    public void onSerialRead(ArrayDeque<byte[]> datas) {
        receive(datas);
    }

    @Override
    public void onSerialIoError(Exception e) {
        status("connection lost: " + e.getMessage());
        disconnect();
    }

    class ControlLines {
        private static final int refreshInterval = 200; // msec

        private final Runnable runnable;

        private View frame;
        private ToggleButton rtsBtn, ctsBtn, dtrBtn, dsrBtn, cdBtn, riBtn;

        private boolean showControlLines;                                               // show & update control line buttons
        private UsbSerialPort.FlowControl flowControl = UsbSerialPort.FlowControl.NONE; // !NONE: update send button state

        boolean sendAllowed = true;

        ControlLines() {
            runnable = this::run; // w/o explicit Runnable, a new lambda would be created on each postDelayed, which would not be found again by removeCallbacks
        }

        void onCreateView(View view) {
            frame = view.findViewById(R.id.controlLines);
            rtsBtn = view.findViewById(R.id.controlLineRts);
            ctsBtn = view.findViewById(R.id.controlLineCts);
            dtrBtn = view.findViewById(R.id.controlLineDtr);
            dsrBtn = view.findViewById(R.id.controlLineDsr);
            cdBtn = view.findViewById(R.id.controlLineCd);
            riBtn = view.findViewById(R.id.controlLineRi);
            rtsBtn.setOnClickListener(this::toggle);
            dtrBtn.setOnClickListener(this::toggle);
        }

        void onPrepareOptionsMenu(Menu menu) {
            try {
                EnumSet<UsbSerialPort.ControlLine> scl = usbSerialPort.getSupportedControlLines();
                EnumSet<UsbSerialPort.FlowControl> sfc = usbSerialPort.getSupportedFlowControl();
                menu.findItem(R.id.controlLines).setEnabled(!scl.isEmpty());
                menu.findItem(R.id.controlLines).setChecked(showControlLines);
                menu.findItem(R.id.flowControl).setEnabled(sfc.size() > 1);
            } catch (Exception ignored) {
            }
        }

        void selectFlowControl() {
            EnumSet<UsbSerialPort.FlowControl> sfc = usbSerialPort.getSupportedFlowControl();
            UsbSerialPort.FlowControl fc = usbSerialPort.getFlowControl();
            ArrayList<String> names = new ArrayList<>();
            ArrayList<UsbSerialPort.FlowControl> values = new ArrayList<>();
            int pos = 0;
            names.add("<none>");
            values.add(UsbSerialPort.FlowControl.NONE);
            if (sfc.contains(UsbSerialPort.FlowControl.RTS_CTS)) {
                names.add("RTS/CTS control lines");
                values.add(UsbSerialPort.FlowControl.RTS_CTS);
                if (fc == UsbSerialPort.FlowControl.RTS_CTS) pos = names.size() -1;
            }
            if (sfc.contains(UsbSerialPort.FlowControl.DTR_DSR)) {
                names.add("DTR/DSR control lines");
                values.add(UsbSerialPort.FlowControl.DTR_DSR);
                if (fc == UsbSerialPort.FlowControl.DTR_DSR) pos = names.size() - 1;
            }
            if (sfc.contains(UsbSerialPort.FlowControl.XON_XOFF)) {
                names.add("XON/XOFF characters");
                values.add(UsbSerialPort.FlowControl.XON_XOFF);
                if (fc == UsbSerialPort.FlowControl.XON_XOFF) pos = names.size() - 1;
            }
            if (sfc.contains(UsbSerialPort.FlowControl.XON_XOFF_INLINE)) {
                names.add("XON/XOFF characters");
                values.add(UsbSerialPort.FlowControl.XON_XOFF_INLINE);
                if (fc == UsbSerialPort.FlowControl.XON_XOFF_INLINE) pos = names.size() - 1;
            }
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("Flow Control");
            builder.setSingleChoiceItems(names.toArray(new CharSequence[0]), pos, (dialog, which) -> {
                dialog.dismiss();
                try {
                    flowControl = values.get(which);
                    usbSerialPort.setFlowControl(flowControl);
                    flowControlFilter = usbSerialPort.getFlowControl() == UsbSerialPort.FlowControl.XON_XOFF_INLINE ? new XonXoffFilter() : null;
                    start();
                } catch (Exception e) {
                    status("Set flow control failed: "+e.getClass().getName()+" "+e.getMessage());
                    flowControl = UsbSerialPort.FlowControl.NONE;
                    flowControlFilter = null;
                    start();
                }
            });
            builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
            builder.setNeutralButton("Info", (dialog, which) -> {
                dialog.dismiss();
                AlertDialog.Builder builder2 = new AlertDialog.Builder(getActivity());
                builder2.setTitle("Flow Control").setMessage("If send is stopped by the external device, the 'Send' button changes to 'Blocked' icon.");
                builder2.create().show();
            });
            builder.create().show();
        }

        public boolean showControlLines(boolean show) {
            showControlLines = show;
            start();
            return showControlLines;
        }

        void start() {
            if (showControlLines) {
                try {
                    EnumSet<UsbSerialPort.ControlLine> lines = usbSerialPort.getSupportedControlLines();
                    rtsBtn.setVisibility(lines.contains(UsbSerialPort.ControlLine.RTS) ? View.VISIBLE : View.INVISIBLE);
                    ctsBtn.setVisibility(lines.contains(UsbSerialPort.ControlLine.CTS) ? View.VISIBLE : View.INVISIBLE);
                    dtrBtn.setVisibility(lines.contains(UsbSerialPort.ControlLine.DTR) ? View.VISIBLE : View.INVISIBLE);
                    dsrBtn.setVisibility(lines.contains(UsbSerialPort.ControlLine.DSR) ? View.VISIBLE : View.INVISIBLE);
                    cdBtn.setVisibility(lines.contains(UsbSerialPort.ControlLine.CD)   ? View.VISIBLE : View.INVISIBLE);
                    riBtn.setVisibility(lines.contains(UsbSerialPort.ControlLine.RI)   ? View.VISIBLE : View.INVISIBLE);
                } catch (IOException e) {
                    showControlLines = false;
                    status("getSupportedControlLines() failed: " + e.getMessage());
                }
            }
            frame.setVisibility(showControlLines ? View.VISIBLE : View.GONE);
            if(flowControl == UsbSerialPort.FlowControl.NONE) {
                sendAllowed = true;
                updateSendBtn(SendButtonState.Idle);
            }

            mainLooper.removeCallbacks(runnable);
            if (showControlLines || flowControl != UsbSerialPort.FlowControl.NONE) {
                run();
            }
        }

        void stop() {
            mainLooper.removeCallbacks(runnable);
            sendAllowed = true;
            updateSendBtn(SendButtonState.Idle);
            rtsBtn.setChecked(false);
            ctsBtn.setChecked(false);
            dtrBtn.setChecked(false);
            dsrBtn.setChecked(false);
            cdBtn.setChecked(false);
            riBtn.setChecked(false);
        }

        private void run() {
            if (connected != Connected.True)
                return;
            try {
                if (showControlLines) {
                    EnumSet<UsbSerialPort.ControlLine> lines = usbSerialPort.getControlLines();
                    if(rtsBtn.isChecked() != lines.contains(UsbSerialPort.ControlLine.RTS)) rtsBtn.setChecked(!rtsBtn.isChecked());
                    if(ctsBtn.isChecked() != lines.contains(UsbSerialPort.ControlLine.CTS)) ctsBtn.setChecked(!ctsBtn.isChecked());
                    if(dtrBtn.isChecked() != lines.contains(UsbSerialPort.ControlLine.DTR)) dtrBtn.setChecked(!dtrBtn.isChecked());
                    if(dsrBtn.isChecked() != lines.contains(UsbSerialPort.ControlLine.DSR)) dsrBtn.setChecked(!dsrBtn.isChecked());
                    if(cdBtn.isChecked()  != lines.contains(UsbSerialPort.ControlLine.CD))  cdBtn.setChecked(!cdBtn.isChecked());
                    if(riBtn.isChecked()  != lines.contains(UsbSerialPort.ControlLine.RI))  riBtn.setChecked(!riBtn.isChecked());
                }
                if (flowControl != UsbSerialPort.FlowControl.NONE) {
                    switch (usbSerialPort.getFlowControl()) {
                        case DTR_DSR:         sendAllowed = usbSerialPort.getDSR(); break;
                        case RTS_CTS:         sendAllowed = usbSerialPort.getCTS(); break;
                        case XON_XOFF:        sendAllowed = usbSerialPort.getXON(); break;
                        case XON_XOFF_INLINE: sendAllowed = flowControlFilter != null && flowControlFilter.getXON(); break;
                        default:              sendAllowed = true;
                    }
                    updateSendBtn(sendAllowed ? SendButtonState.Idle : SendButtonState.Disabled);
                }
                mainLooper.postDelayed(runnable, refreshInterval);
            } catch (IOException e) {
                status("getControlLines() failed: " + e.getMessage() + " -> stopped control line refresh");
            }
        }

        private void toggle(View v) {
            ToggleButton btn = (ToggleButton) v;
            if (connected != Connected.True) {
                btn.setChecked(!btn.isChecked());
                Toast.makeText(getActivity(), R.string.remote_disconnected, Toast.LENGTH_SHORT).show();
                return;
            }
            String ctrl = "";
            try {
                if (btn.equals(rtsBtn)) { ctrl = "RTS"; usbSerialPort.setRTS(btn.isChecked()); }
                if (btn.equals(dtrBtn)) { ctrl = "DTR"; usbSerialPort.setDTR(btn.isChecked()); }
            } catch (IOException e) {
                status("set" + ctrl + " failed: " + e.getMessage());
            }
        }

    }

}
