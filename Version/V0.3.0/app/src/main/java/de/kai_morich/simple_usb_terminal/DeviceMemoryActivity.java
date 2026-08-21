package de.kai_morich.simple_usb_terminal;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.chenwei666.netserial.device.CliMode;
import com.chenwei666.netserial.device.DeviceProfile;
import com.chenwei666.netserial.device.DeviceProfileStore;
import com.chenwei666.netserial.device.Vendor;
import com.chenwei666.netserial.memory.MemoryRecord;
import com.chenwei666.netserial.memory.MemoryScope;
import com.chenwei666.netserial.memory.MemoryVault;
import com.chenwei666.netserial.memory.SharedPreferencesMemoryPersistence;
import com.google.gson.Gson;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class DeviceMemoryActivity extends AppCompatActivity {
    private static final int EXPORT_REQUEST = 5101;
    private static final int IMPORT_REQUEST = 5102;
    private static final int MAX_IMPORT_BYTES = 1_000_000;
    private final Gson gson = new Gson();
    private DeviceProfileStore profileStore;
    private MemoryVault memoryVault;
    private EditText name;
    private EditText baud;
    private EditText memoryContent;
    private Spinner vendor;
    private Spinner mode;
    private LinearLayout memoryList;
    private TextView status;

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
        setContentView(R.layout.activity_device_memory);
        Toolbar toolbar = findViewById(R.id.device_memory_toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(view -> finish());
        profileStore = new DeviceProfileStore(this);
        memoryVault = new MemoryVault(new SharedPreferencesMemoryPersistence(this));
        name = findViewById(R.id.device_name);
        baud = findViewById(R.id.device_baud);
        memoryContent = findViewById(R.id.memory_content);
        vendor = findViewById(R.id.device_vendor);
        mode = findViewById(R.id.device_mode);
        memoryList = findViewById(R.id.memory_list);
        status = findViewById(R.id.device_memory_status);
        vendor.setAdapter(enumAdapter(Vendor.values()));
        mode.setAdapter(enumAdapter(CliMode.values()));
        findViewById(R.id.device_save).setOnClickListener(view -> saveDevice());
        findViewById(R.id.memory_add).setOnClickListener(view -> addMemory());
        findViewById(R.id.data_export).setOnClickListener(view -> startExport());
        findViewById(R.id.data_import).setOnClickListener(view -> startImport());
        render();
    }

    private <T> ArrayAdapter<T> enumAdapter(T[] values) {
        ArrayAdapter<T> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item,
                Arrays.asList(values));
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        return adapter;
    }

    private void render() {
        DeviceProfile profile = profileStore.load();
        name.setText(profile.getName());
        baud.setText(String.valueOf(profile.getBaudRate()));
        vendor.setSelection(profile.getVendor().ordinal());
        mode.setSelection(profile.getCliMode().ordinal());
        renderMemories();
    }

    private void saveDevice() {
        try {
            DeviceProfile profile = currentProfile();
            profileStore.save(profile);
            status.setText(R.string.device_saved);
        } catch (RuntimeException exception) {
            status.setText(R.string.device_invalid);
        }
    }

    private DeviceProfile currentProfile() {
        return new DeviceProfile(name.getText().toString(), (Vendor) vendor.getSelectedItem(),
                (CliMode) mode.getSelectedItem(), Integer.parseInt(baud.getText().toString()));
    }

    private void addMemory() {
        String content = memoryContent.getText().toString().trim();
        if (content.isEmpty()) return;
        try {
            DeviceProfile profile = currentProfile();
            long now = System.currentTimeMillis();
            memoryVault.add(MemoryRecord.userVerified(MemoryScope.DEVICE, profile.getName(),
                    content, now, now + 180L * 24L * 60L * 60L * 1000L));
            memoryContent.setText("");
            status.setText(R.string.memory_saved);
            renderMemories();
        } catch (RuntimeException exception) {
            status.setText(R.string.memory_rejected);
        }
    }

    private void renderMemories() {
        memoryList.removeAllViews();
        try {
            for (MemoryRecord record : memoryVault.list(System.currentTimeMillis())) {
                Button item = new Button(this);
                item.setAllCaps(false);
                item.setText(record.getScope().name() + " · " + record.getSubjectId() + "\n"
                        + record.getContent() + "\n" + getString(R.string.memory_delete_hint));
                item.setOnClickListener(view -> {
                    memoryVault.delete(record.getId());
                    renderMemories();
                });
                memoryList.addView(item);
            }
        } catch (RuntimeException exception) {
            status.setText(R.string.memory_load_failed);
        }
    }

    private void startExport() {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT)
                .setType("application/json")
                .putExtra(Intent.EXTRA_TITLE, "netserial-safe-backup.json");
        startActivityForResult(intent, EXPORT_REQUEST);
    }

    private void startImport() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT)
                .setType("application/json")
                .addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, IMPORT_REQUEST);
    }

    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK || data == null || data.getData() == null) return;
        if (requestCode == EXPORT_REQUEST) exportTo(data.getData());
        if (requestCode == IMPORT_REQUEST) importFrom(data.getData());
    }

    private void exportTo(Uri uri) {
        try (OutputStream output = getContentResolver().openOutputStream(uri)) {
            if (output == null) throw new IllegalStateException();
            SafeBackup backup = new SafeBackup(1, currentProfile(),
                    memoryVault.list(System.currentTimeMillis()));
            output.write(gson.toJson(backup).getBytes(StandardCharsets.UTF_8));
            status.setText(R.string.data_exported);
        } catch (Exception exception) {
            status.setText(R.string.data_export_failed);
        }
    }

    private void importFrom(Uri uri) {
        try (InputStream input = getContentResolver().openInputStream(uri)) {
            if (input == null) throw new IllegalStateException();
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            byte[] buffer = new byte[8192];
            int read;
            while ((read = input.read(buffer)) != -1) {
                if (output.size() + read > MAX_IMPORT_BYTES) throw new IllegalArgumentException();
                output.write(buffer, 0, read);
            }
            SafeBackup backup = gson.fromJson(new String(output.toByteArray(), StandardCharsets.UTF_8),
                    SafeBackup.class);
            if (backup == null || backup.schemaVersion != 1 || backup.device == null
                    || backup.memories == null) throw new IllegalArgumentException();
            DeviceProfile validated = new DeviceProfile(backup.device.getName(),
                    backup.device.getVendor(), backup.device.getCliMode(), backup.device.getBaudRate());
            long now = System.currentTimeMillis();
            List<MemoryRecord> validatedMemories = memoryVault.validateForImport(backup.memories, now);
            DeviceProfile previousProfile = profileStore.load();
            profileStore.save(validated);
            try {
                memoryVault.replaceAll(validatedMemories, now);
            } catch (RuntimeException exception) {
                profileStore.save(previousProfile);
                throw exception;
            }
            render();
            status.setText(R.string.data_imported);
        } catch (Exception exception) {
            status.setText(R.string.data_import_failed);
        }
    }

    private static final class SafeBackup {
        private final int schemaVersion;
        private final DeviceProfile device;
        private final List<MemoryRecord> memories;
        private SafeBackup(int schemaVersion, DeviceProfile device, List<MemoryRecord> memories) {
            this.schemaVersion = schemaVersion;
            this.device = device;
            this.memories = new ArrayList<>(memories);
        }
    }
}
