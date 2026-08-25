package de.kai_morich.simple_usb_terminal;

import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Typeface;
import android.os.Build;
import android.text.InputType;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.chenwei666.netserial.device.DeviceProfile;
import com.chenwei666.netserial.device.Vendor;
import com.chenwei666.netserial.web.WebAccessPlan;
import com.chenwei666.netserial.web.WebAccessPlanFactory;
import com.chenwei666.netserial.web.WebAccessRequest;

import java.util.Arrays;
import java.util.List;

public final class WebAccessWizardDialog {
    public interface Listener { void onPlanConfirmed(WebAccessPlan plan); }

    private static final List<Vendor> VENDORS = Arrays.asList(Vendor.H3C_COMWARE,
            Vendor.HUAWEI_VRP, Vendor.CISCO_IOS, Vendor.RUIJIE_RGOS);

    private WebAccessWizardDialog() { }

    public static void show(Activity activity, DeviceProfile profile, String detectedPlatform,
                            Listener listener) {
        activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
        int padding = Math.round(20 * activity.getResources().getDisplayMetrics().density);
        LinearLayout form = new LinearLayout(activity);
        form.setOrientation(LinearLayout.VERTICAL);
        form.setPadding(padding, 0, padding, 0);

        TextView note = new TextView(activity);
        note.setText(R.string.web_access_security_note);
        note.setPadding(0, 0, 0, padding / 2);
        form.addView(note);

        Spinner vendor = new Spinner(activity);
        ArrayAdapter<String> vendors = new ArrayAdapter<>(activity, android.R.layout.simple_spinner_item,
                new String[]{activity.getString(R.string.vendor_h3c), activity.getString(R.string.vendor_huawei),
                        activity.getString(R.string.vendor_cisco), activity.getString(R.string.vendor_ruijie)});
        vendors.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        vendor.setAdapter(vendors);
        int selected = VENDORS.indexOf(profile.getVendor());
        vendor.setSelection(selected < 0 ? 0 : selected);
        form.addView(vendor);

        EditText username = field(activity, R.string.web_access_username_hint,
                InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
        EditText password = field(activity, R.string.web_access_password_hint,
                InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        password.setSaveEnabled(false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            password.setImportantForAutofill(View.IMPORTANT_FOR_AUTOFILL_NO_EXCLUDE_DESCENDANTS);
        }
        form.addView(username);
        form.addView(password);

        CheckBox https = new CheckBox(activity);
        https.setText(R.string.web_access_enable_https);
        https.setChecked(true);
        form.addView(https);
        CheckBox http = new CheckBox(activity);
        http.setText(R.string.web_access_enable_http);
        http.setChecked(false);
        form.addView(http);

        AlertDialog dialog = new AlertDialog.Builder(activity)
                .setTitle(R.string.web_access_title)
                .setView(form)
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(R.string.web_access_generate, null)
                .create();
        dialog.setOnShowListener(ignored -> dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener(view -> {
                    char[] secret = password.getText().toString().toCharArray();
                    try (WebAccessRequest request = new WebAccessRequest(
                            VENDORS.get(vendor.getSelectedItemPosition()), detectedPlatform,
                            username.getText().toString(), secret, https.isChecked(), http.isChecked())) {
                        WebAccessPlan plan = new WebAccessPlanFactory().create(request);
                        password.setText("");
                        Arrays.fill(secret, '\0');
                        dialog.dismiss();
                        confirmPlan(activity, plan, listener);
                    } catch (RuntimeException exception) {
                        Arrays.fill(secret, '\0');
                        Toast.makeText(activity, R.string.web_access_invalid, Toast.LENGTH_LONG).show();
                    }
                }));
        dialog.show();
    }

    private static void confirmPlan(Activity activity, WebAccessPlan plan, Listener listener) {
        TextView preview = new TextView(activity);
        int padding = Math.round(20 * activity.getResources().getDisplayMetrics().density);
        preview.setPadding(padding, 0, padding, 0);
        preview.setTypeface(Typeface.MONOSPACE);
        preview.setText(activity.getString(R.string.web_access_preview,
                activity.getString(plan.containsPlainHttp()
                        ? R.string.web_access_http_warning : R.string.web_access_https_summary),
                plan.redactedBatch()));
        new AlertDialog.Builder(activity).setTitle(R.string.web_access_confirm_title)
                .setView(preview)
                .setNegativeButton(android.R.string.cancel, (dialog, which) -> plan.destroy())
                .setOnCancelListener(dialog -> plan.destroy())
                .setPositiveButton(R.string.web_access_send, (dialog, which) -> listener.onPlanConfirmed(plan))
                .show();
    }

    private static EditText field(Activity activity, int hint, int inputType) {
        EditText value = new EditText(activity);
        value.setHint(hint);
        value.setSingleLine(true);
        value.setMaxLines(1);
        value.setInputType(inputType);
        return value;
    }
}
