package de.kai_morich.simple_usb_terminal;

import android.content.Intent;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.chenwei666.netserial.config.ConfigNormalizer;
import com.chenwei666.netserial.config.ConfigSnapshot;
import com.chenwei666.netserial.config.ConfigSnapshotStore;
import com.chenwei666.netserial.terminal.AnsiTextSanitizer;
import com.chenwei666.netserial.terminal.SensitiveTextRedactor;

import java.util.ArrayList;
import java.util.List;

public final class ConfigSnapshotCenterActivity extends ThemedActivity {
    public static final String EXTRA_CAPTURE = "capture";
    private final List<ConfigSnapshot> snapshots = new ArrayList<>();
    private ConfigSnapshotStore store;
    private ArrayAdapter<String> adapter;
    private EditText label;
    private EditText content;

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
        setContentView(R.layout.activity_config_snapshot_center);
        setTitle(R.string.snapshot_title);
        store = new ConfigSnapshotStore(this);
        label = findViewById(R.id.snapshot_label);
        content = findViewById(R.id.snapshot_content);
        content.setText(sanitize(getIntent().getStringExtra(EXTRA_CAPTURE)));
        ListView list = findViewById(R.id.snapshot_list);
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, new ArrayList<>());
        list.setAdapter(adapter);
        list.setOnItemClickListener((parent, view, position, id) -> compare(snapshots.get(position)));
        findViewById(R.id.snapshot_save).setOnClickListener(view -> save());
        refresh();
    }

    private void save() {
        try {
            String normalized = new ConfigNormalizer().normalize(sanitize(content.getText().toString()));
            ConfigSnapshot snapshot = new ConfigSnapshot(label.getText().toString(),
                    System.currentTimeMillis(), normalized);
            store.add(snapshot);
            label.setText("");
            refresh();
            Toast.makeText(this, R.string.snapshot_saved, Toast.LENGTH_SHORT).show();
        } catch (RuntimeException exception) {
            Toast.makeText(this, R.string.snapshot_save_failed, Toast.LENGTH_LONG).show();
        }
    }

    private void refresh() {
        snapshots.clear();
        snapshots.addAll(store.load());
        adapter.clear();
        for (ConfigSnapshot snapshot : snapshots) {
            adapter.add(snapshot.getLabel() + " · " + snapshot.getSha256().substring(0, 12));
        }
        adapter.notifyDataSetChanged();
    }

    private void compare(ConfigSnapshot snapshot) {
        startActivity(new Intent(this, ConfigDiffActivity.class)
                .putExtra(ConfigDiffActivity.EXTRA_BEFORE, snapshot.getNormalizedText())
                .putExtra(ConfigDiffActivity.EXTRA_AFTER, sanitize(content.getText().toString())));
    }

    private static String sanitize(String value) {
        String text = value == null ? "" : value;
        return new SensitiveTextRedactor().redact(new AnsiTextSanitizer().sanitize(text));
    }
}
