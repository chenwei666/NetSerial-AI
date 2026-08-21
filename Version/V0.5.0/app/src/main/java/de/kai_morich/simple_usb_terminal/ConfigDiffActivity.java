package de.kai_morich.simple_usb_terminal;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.chenwei666.netserial.config.ConfigDiff;
import com.chenwei666.netserial.config.ConfigDiffEngine;
import com.chenwei666.netserial.config.ConfigNormalizer;
import com.chenwei666.netserial.config.ConfigSnapshot;
import com.chenwei666.netserial.config.RollbackDraftGenerator;
import com.chenwei666.netserial.device.DeviceProfile;
import com.chenwei666.netserial.device.DeviceProfileStore;
import com.chenwei666.netserial.settings.AppLocaleController;
import com.chenwei666.netserial.terminal.AnsiTextSanitizer;
import com.chenwei666.netserial.terminal.SensitiveTextRedactor;

import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public final class ConfigDiffActivity extends ThemedActivity {
    public static final String EXTRA_CAPTURE = "capture";
    private static final int EXPORT_REQUEST = 5501;
    private EditText before;
    private EditText after;
    private TextView result;
    private String latestReport = "";

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
        setContentView(R.layout.activity_config_diff);
        Toolbar toolbar = findViewById(R.id.config_diff_toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(view -> finish());
        before = findViewById(R.id.config_before);
        after = findViewById(R.id.config_after);
        result = findViewById(R.id.config_result);
        String capture = sanitize(getIntent().getStringExtra(EXTRA_CAPTURE));
        if (!capture.isEmpty()) before.setText(capture);
        findViewById(R.id.config_use_before).setOnClickListener(view -> before.setText(capture));
        findViewById(R.id.config_use_after).setOnClickListener(view -> after.setText(capture));
        findViewById(R.id.config_compare).setOnClickListener(view -> compare());
        findViewById(R.id.config_export).setOnClickListener(view -> export());
    }

    private void compare() {
        try {
            ConfigNormalizer normalizer = new ConfigNormalizer();
            String left = normalizer.normalize(sanitize(before.getText().toString()));
            String right = normalizer.normalize(sanitize(after.getText().toString()));
            before.setText(left);
            after.setText(right);
            ConfigSnapshot leftSnapshot = new ConfigSnapshot("before", System.currentTimeMillis(), left);
            ConfigSnapshot rightSnapshot = new ConfigSnapshot("after", System.currentTimeMillis(), right);
            ConfigDiff diff = new ConfigDiffEngine().compare(left, right);
            DeviceProfile profile = new DeviceProfileStore(this).load();
            String rollback = new RollbackDraftGenerator().generate(diff, profile.getVendor());
            latestReport = getString(R.string.config_report_title)
                    + "\n\n" + getString(R.string.config_report_before_hash, leftSnapshot.getSha256())
                    + "\n" + getString(R.string.config_report_after_hash, rightSnapshot.getSha256())
                    + "\n" + getString(R.string.config_report_added, diff.getAddedLines().size())
                    + "\n" + getString(R.string.config_report_removed, diff.getRemovedLines().size())
                    + "\n\n" + getString(R.string.config_report_diff_heading)
                    + "\n\n```diff\n" + diff.toUnifiedText()
                    + "```\n\n" + getString(R.string.config_report_rollback_heading)
                    + "\n\n> " + getString(R.string.config_report_review_only)
                    + "\n\n```text\n" + rollback + "```\n";
            result.setText(latestReport);
        } catch (RuntimeException exception) {
            result.setText(R.string.config_compare_failed);
        }
    }

    private void export() {
        if (latestReport.isEmpty()) {
            result.setText(R.string.config_compare_first);
            return;
        }
        startActivityForResult(new Intent(Intent.ACTION_CREATE_DOCUMENT).setType("text/markdown")
                .putExtra(Intent.EXTRA_TITLE, "netserial-config-diff.md"), EXPORT_REQUEST);
    }

    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != EXPORT_REQUEST || resultCode != RESULT_OK || data == null
                || data.getData() == null) return;
        Uri uri = data.getData();
        try (OutputStream output = getContentResolver().openOutputStream(uri)) {
            if (output == null) throw new IllegalStateException();
            output.write(latestReport.getBytes(StandardCharsets.UTF_8));
            result.append("\n" + getString(R.string.config_exported));
        } catch (Exception exception) {
            result.append("\n" + getString(R.string.config_export_failed));
        }
    }

    private static String sanitize(String value) {
        String input = value == null ? "" : value;
        if (input.length() > 100_000) input = input.substring(input.length() - 100_000);
        return new SensitiveTextRedactor().redact(new AnsiTextSanitizer().sanitize(input));
    }
}
