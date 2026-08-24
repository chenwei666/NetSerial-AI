package de.kai_morich.simple_usb_terminal;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.OpenableColumns;
import android.text.TextUtils;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

import com.chenwei666.netserial.ai.CredentialVault;
import com.chenwei666.netserial.ai.CredentialVaultFactory;
import com.chenwei666.netserial.ai.DeviceCredentialAliases;
import com.chenwei666.netserial.runbook.RunbookPack;
import com.chenwei666.netserial.runbook.RunbookPackCodec;
import com.chenwei666.netserial.runbook.RunbookSignatureVerifier;
import com.chenwei666.netserial.transfer.TemporaryHttpFileServer;
import com.chenwei666.netserial.transfer.TemporaryTftpReadServer;
import com.chenwei666.netserial.transfer.TemporaryTransferPolicy;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

/** Security-focused tools kept behind one toolbox entry to avoid menu sprawl. */
public final class AdvancedToolkitActivity extends ThemedActivity {
    private static final int PICK_TRANSFER_FILE = 501;
    private static final int PICK_RUNBOOK_FILE = 502;
    private static final long MAX_TRANSFER_COPY = 256L * 1024 * 1024;
    private static final int MAX_RUNBOOK_BYTES = 1_000_000;

    private final ExecutorService worker = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final AtomicLong transferGeneration = new AtomicLong();
    private final Runnable transferExpiration = this::expireTransfer;
    private EditText credentialAlias;
    private EditText credentialSecret;
    private EditText runbookSignature;
    private EditText runbookPublicKey;
    private TextView credentialStatus;
    private TextView transferStatus;
    private TextView runbookStatus;
    private File transferFile;
    private byte[] runbookDocument;
    private TemporaryHttpFileServer httpServer;
    private TemporaryTftpReadServer tftpServer;

    private void expireTransfer() {
        transferGeneration.incrementAndGet();
        stopServers();
        deleteTransferFile();
        transferStatus.setText(R.string.advanced_transfer_expired);
    }

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
        setContentView(R.layout.activity_advanced_toolkit);
        setTitle(R.string.advanced_toolkit_title);
        credentialAlias = findViewById(R.id.advanced_credential_alias);
        credentialSecret = findViewById(R.id.advanced_credential_secret);
        credentialStatus = findViewById(R.id.advanced_credential_status);
        transferStatus = findViewById(R.id.advanced_transfer_status);
        runbookSignature = findViewById(R.id.advanced_runbook_signature);
        runbookPublicKey = findViewById(R.id.advanced_runbook_public_key);
        runbookStatus = findViewById(R.id.advanced_runbook_status);

        findViewById(R.id.advanced_credential_check).setOnClickListener(v -> checkCredential());
        findViewById(R.id.advanced_credential_save).setOnClickListener(v -> saveCredential());
        findViewById(R.id.advanced_credential_delete).setOnClickListener(v -> deleteCredential());
        findViewById(R.id.advanced_transfer_pick).setOnClickListener(v -> pickFile(PICK_TRANSFER_FILE, "*/*"));
        findViewById(R.id.advanced_transfer_start).setOnClickListener(v -> confirmTransfer());
        findViewById(R.id.advanced_transfer_stop).setOnClickListener(v -> stopTransfer());
        findViewById(R.id.advanced_runbook_pick).setOnClickListener(v -> pickFile(PICK_RUNBOOK_FILE, "application/json"));
        findViewById(R.id.advanced_runbook_verify).setOnClickListener(v -> verifyRunbook());
    }

    private void checkCredential() {
        if (!credentialStorageAvailable()) return;
        try {
            boolean exists = vault().contains(credentialKey());
            credentialStatus.setText(exists ? R.string.advanced_credential_present : R.string.advanced_credential_missing);
        } catch (RuntimeException exception) {
            credentialStatus.setText(R.string.advanced_invalid_input);
        }
    }

    private void saveCredential() {
        if (!credentialStorageAvailable()) return;
        char[] secret = credentialSecret.getText().toString().toCharArray();
        try {
            vault().store(credentialKey(), secret);
            credentialSecret.getText().clear();
            credentialStatus.setText(R.string.advanced_credential_saved);
        } catch (RuntimeException exception) {
            credentialStatus.setText(R.string.advanced_invalid_input);
        } finally {
            Arrays.fill(secret, '\0');
        }
    }

    private void deleteCredential() {
        if (!credentialStorageAvailable()) return;
        try {
            vault().delete(credentialKey());
            credentialSecret.getText().clear();
            credentialStatus.setText(R.string.advanced_credential_deleted);
        } catch (RuntimeException exception) {
            credentialStatus.setText(R.string.advanced_invalid_input);
        }
    }

    private CredentialVault vault() {
        return CredentialVaultFactory.create(this);
    }

    private boolean credentialStorageAvailable() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) return true;
        credentialStatus.setText(R.string.advanced_keystore_unavailable);
        return false;
    }

    private String credentialKey() {
        return DeviceCredentialAliases.vaultKey(credentialAlias.getText().toString());
    }

    private void pickFile(int requestCode, String mimeType) {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT)
                .addCategory(Intent.CATEGORY_OPENABLE)
                .setType(mimeType);
        startActivityForResult(intent, requestCode);
    }

    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != Activity.RESULT_OK || data == null || data.getData() == null) return;
        Uri uri = data.getData();
        if (requestCode == PICK_TRANSFER_FILE) copyTransferFile(uri);
        if (requestCode == PICK_RUNBOOK_FILE) loadRunbook(uri);
    }

    private void copyTransferFile(Uri uri) {
        long generation = transferGeneration.incrementAndGet();
        mainHandler.removeCallbacks(transferExpiration);
        stopServers();
        deleteTransferFile();
        transferStatus.setText(R.string.advanced_loading);
        worker.execute(() -> {
            try {
                File directory = new File(getCacheDir(), "temporary-transfer");
                if (!directory.exists() && !directory.mkdirs()) throw new IllegalStateException("Unable to create cache");
                File destination = new File(directory, System.currentTimeMillis() + "-" + safeName(displayName(uri)));
                try (InputStream input = getContentResolver().openInputStream(uri);
                     FileOutputStream output = new FileOutputStream(destination)) {
                    if (input == null) throw new IllegalArgumentException("File unavailable");
                    byte[] buffer = new byte[32 * 1024];
                    long total = 0;
                    for (int count; (count = input.read(buffer)) >= 0;) {
                        total += count;
                        if (total > MAX_TRANSFER_COPY) throw new IllegalArgumentException("File too large");
                        output.write(buffer, 0, count);
                    }
                } catch (Exception exception) {
                    //noinspection ResultOfMethodCallIgnored
                    destination.delete();
                    throw exception;
                }
                if (generation != transferGeneration.get()) {
                    //noinspection ResultOfMethodCallIgnored
                    destination.delete();
                    return;
                }
                transferFile = destination;
                runOnUiThread(() -> transferStatus.setText(getString(R.string.advanced_transfer_selected,
                        destination.getName(), destination.length())));
            } catch (Exception exception) {
                runOnUiThread(() -> transferStatus.setText(R.string.advanced_transfer_error));
            }
        });
    }

    private void confirmTransfer() {
        if (transferFile == null || !transferFile.isFile()) {
            transferStatus.setText(R.string.advanced_transfer_select_first);
            return;
        }
        new AlertDialog.Builder(this)
                .setTitle(R.string.advanced_transfer_confirm_title)
                .setMessage(R.string.advanced_transfer_confirm_message)
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(R.string.advanced_transfer_start, (dialog, which) -> startTransfer())
                .show();
    }

    private void startTransfer() {
        stopServers();
        long generation = transferGeneration.incrementAndGet();
        File selectedFile = transferFile;
        worker.execute(() -> {
            try {
                InetAddress address = privateAddress();
                TemporaryTransferPolicy policy = new TemporaryTransferPolicy(address, 10 * 60_000L, 3);
                TemporaryHttpFileServer http = new TemporaryHttpFileServer(policy, selectedFile);
                String url = http.start();
                TemporaryTftpReadServer tftp = null;
                String tftpText = getString(R.string.advanced_tftp_unavailable);
                if (selectedFile.length() <= 31L * 1024 * 1024) {
                    tftp = new TemporaryTftpReadServer(policy, selectedFile);
                    int port = tftp.start();
                    tftpText = getString(R.string.advanced_tftp_address, address.getHostAddress(), port,
                            tftp.getPublishedName());
                }
                if (generation != transferGeneration.get()) {
                    http.close();
                    if (tftp != null) tftp.close();
                    return;
                }
                httpServer = http;
                tftpServer = tftp;
                String status = getString(R.string.advanced_transfer_running, url, tftpText);
                runOnUiThread(() -> {
                    mainHandler.removeCallbacks(transferExpiration);
                    mainHandler.postDelayed(transferExpiration, 10 * 60_000L);
                    transferStatus.setText(status);
                });
            } catch (Exception exception) {
                stopServers();
                runOnUiThread(() -> transferStatus.setText(R.string.advanced_transfer_error));
            }
        });
    }

    private void stopTransfer() {
        transferGeneration.incrementAndGet();
        mainHandler.removeCallbacks(transferExpiration);
        stopServers();
        deleteTransferFile();
        transferStatus.setText(R.string.advanced_transfer_stopped);
    }

    private synchronized void stopServers() {
        if (httpServer != null) httpServer.close();
        if (tftpServer != null) tftpServer.close();
        httpServer = null;
        tftpServer = null;
    }

    private synchronized void deleteTransferFile() {
        File value = transferFile;
        transferFile = null;
        if (value == null) return;
        try {
            File root = new File(getCacheDir(), "temporary-transfer").getCanonicalFile();
            File candidate = value.getCanonicalFile();
            if (candidate.getPath().startsWith(root.getPath() + File.separator)) {
                //noinspection ResultOfMethodCallIgnored
                candidate.delete();
            }
        } catch (Exception ignored) { }
    }

    private void loadRunbook(Uri uri) {
        runbookStatus.setText(R.string.advanced_loading);
        worker.execute(() -> {
            try {
                byte[] value = readBounded(uri, MAX_RUNBOOK_BYTES);
                new RunbookPackCodec().decode(value);
                runbookDocument = value;
                runOnUiThread(() -> runbookStatus.setText(R.string.advanced_runbook_loaded));
            } catch (Exception exception) {
                runbookDocument = null;
                runOnUiThread(() -> runbookStatus.setText(R.string.advanced_runbook_invalid));
            }
        });
    }

    private void verifyRunbook() {
        byte[] document = runbookDocument;
        if (document == null) {
            runbookStatus.setText(R.string.advanced_runbook_select_first);
            return;
        }
        String signature = runbookSignature.getText().toString();
        String publicKey = runbookPublicKey.getText().toString();
        worker.execute(() -> {
            boolean verified = new RunbookSignatureVerifier().verify(document, signature, publicKey);
            if (!verified) {
                runOnUiThread(() -> runbookStatus.setText(R.string.advanced_runbook_signature_failed));
                return;
            }
            try {
                RunbookPack pack = new RunbookPackCodec().decode(document);
                StringBuilder preview = new StringBuilder(getString(R.string.advanced_runbook_verified,
                        pack.getId(), pack.getVersion(), pack.getAuthor(), pack.getCommands().size()));
                for (String command : pack.getCommands()) preview.append("\n").append(command);
                runOnUiThread(() -> runbookStatus.setText(preview.toString()));
            } catch (RuntimeException exception) {
                runOnUiThread(() -> runbookStatus.setText(R.string.advanced_runbook_invalid));
            }
        });
    }

    private byte[] readBounded(Uri uri, int limit) throws Exception {
        try (InputStream input = getContentResolver().openInputStream(uri);
             ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            if (input == null) throw new IllegalArgumentException("File unavailable");
            byte[] buffer = new byte[16 * 1024];
            int total = 0;
            for (int count; (count = input.read(buffer)) >= 0;) {
                total += count;
                if (total > limit) throw new IllegalArgumentException("File too large");
                output.write(buffer, 0, count);
            }
            return output.toByteArray();
        }
    }

    private String displayName(Uri uri) {
        try (Cursor cursor = getContentResolver().query(uri, new String[]{OpenableColumns.DISPLAY_NAME},
                null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) return cursor.getString(0);
        }
        String segment = uri.getLastPathSegment();
        return TextUtils.isEmpty(segment) ? "transfer.bin" : segment;
    }

    private static String safeName(String value) {
        String result = value == null ? "" : value.replaceAll("[^A-Za-z0-9._-]", "_");
        if (result.isEmpty()) result = "transfer.bin";
        return result.length() > 96 ? result.substring(result.length() - 96) : result;
    }

    private static InetAddress privateAddress() throws Exception {
        Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
        for (NetworkInterface network : Collections.list(interfaces)) {
            if (!network.isUp() || network.isLoopback()) continue;
            for (InetAddress address : Collections.list(network.getInetAddresses())) {
                if (address instanceof Inet4Address && address.isSiteLocalAddress()) return address;
            }
        }
        return InetAddress.getLoopbackAddress();
    }

    @Override protected void onDestroy() {
        transferGeneration.incrementAndGet();
        mainHandler.removeCallbacks(transferExpiration);
        stopServers();
        deleteTransferFile();
        worker.shutdownNow();
        if (runbookDocument != null) Arrays.fill(runbookDocument, (byte) 0);
        super.onDestroy();
    }
}
