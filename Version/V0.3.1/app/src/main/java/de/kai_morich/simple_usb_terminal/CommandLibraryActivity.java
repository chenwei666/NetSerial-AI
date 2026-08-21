package de.kai_morich.simple_usb_terminal;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.chenwei666.netserial.commands.CommandCategory;
import com.chenwei666.netserial.commands.CommonCommand;
import com.chenwei666.netserial.commands.CommonCommandCatalog;
import com.chenwei666.netserial.device.Vendor;
import com.chenwei666.netserial.settings.AppLocaleController;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CommandLibraryActivity extends AppCompatActivity {
    public static final String EXTRA_VENDOR = "vendor";
    public static final String EXTRA_PICK_MODE = "pick_mode";
    public static final String RESULT_COMMAND = "command";

    private static final List<Vendor> VENDORS = Arrays.asList(
            Vendor.H3C_COMWARE, Vendor.HUAWEI_VRP, Vendor.CISCO_IOS, Vendor.RUIJIE_RGOS);

    private final CommonCommandCatalog catalog = CommonCommandCatalog.createDefault();
    private final List<CommonCommand> visibleCommands = new ArrayList<>();
    private Spinner vendorSpinner;
    private Spinner categorySpinner;
    private EditText search;
    private ArrayAdapter<CommonCommand> commandAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppLocaleController.applyStoredLanguage(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_command_library);
        setTitle(R.string.command_library_title);
        vendorSpinner = findViewById(R.id.command_vendor);
        categorySpinner = findViewById(R.id.command_category);
        search = findViewById(R.id.command_search);
        bindFilters();
        bindList();
        selectInitialVendor();
        refresh();
    }

    private void bindFilters() {
        ArrayAdapter<String> vendors = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item,
                new String[]{getString(R.string.vendor_h3c), getString(R.string.vendor_huawei),
                        getString(R.string.vendor_cisco), getString(R.string.vendor_ruijie)});
        vendors.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        vendorSpinner.setAdapter(vendors);
        ArrayAdapter<String> categories = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item,
                new String[]{getString(R.string.category_all), getString(R.string.category_device),
                        getString(R.string.category_interface), getString(R.string.category_vlan),
                        getString(R.string.category_layer3), getString(R.string.category_routing),
                        getString(R.string.category_loop), getString(R.string.category_aggregation),
                        getString(R.string.category_security), getString(R.string.category_troubleshooting),
                        getString(R.string.category_save)});
        categories.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(categories);
        AdapterView.OnItemSelectedListener listener = new SimpleItemSelectedListener(position -> refresh());
        vendorSpinner.setOnItemSelectedListener(listener);
        categorySpinner.setOnItemSelectedListener(listener);
        search.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { refresh(); }
            @Override public void afterTextChanged(Editable s) { }
        });
    }

    private void bindList() {
        ListView list = findViewById(R.id.command_list);
        commandAdapter = new ArrayAdapter<CommonCommand>(this, android.R.layout.simple_list_item_2,
                android.R.id.text1, visibleCommands) {
            @NonNull
            @Override public View getView(int position, View convertView, @NonNull ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                CommonCommand command = getItem(position);
                ((TextView) view.findViewById(android.R.id.text1)).setText(command.getCommand());
                ((TextView) view.findViewById(android.R.id.text2)).setText(
                        getString(R.string.command_detail_format, riskLabel(command),
                                categoryLabel(command.getCategory()), command.getDescription()));
                return view;
            }
        };
        list.setAdapter(commandAdapter);
        list.setOnItemClickListener((parent, view, position, id) -> choose(visibleCommands.get(position)));
    }

    private void selectInitialVendor() {
        String name = getIntent().getStringExtra(EXTRA_VENDOR);
        if (name == null) return;
        try {
            int index = VENDORS.indexOf(Vendor.valueOf(name));
            if (index >= 0) vendorSpinner.setSelection(index);
        } catch (IllegalArgumentException ignored) { }
    }

    private void refresh() {
        if (vendorSpinner == null || vendorSpinner.getSelectedItemPosition() < 0) return;
        int categoryPosition = categorySpinner.getSelectedItemPosition();
        CommandCategory category = categoryPosition == 0 ? null : CommandCategory.values()[categoryPosition - 1];
        visibleCommands.clear();
        visibleCommands.addAll(catalog.search(VENDORS.get(vendorSpinner.getSelectedItemPosition()), category,
                search.getText().toString(), 200));
        if (commandAdapter != null) commandAdapter.notifyDataSetChanged();
        ((TextView) findViewById(R.id.command_count)).setText(getString(R.string.command_count, visibleCommands.size()));
    }

    private void choose(CommonCommand command) {
        if (getIntent().getBooleanExtra(EXTRA_PICK_MODE, false)) {
            setResult(RESULT_OK, new Intent().putExtra(RESULT_COMMAND, command.getCommand()));
            finish();
            return;
        }
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        clipboard.setPrimaryClip(ClipData.newPlainText("switch command", command.getCommand()));
        Toast.makeText(this, R.string.command_copied, Toast.LENGTH_SHORT).show();
    }

    private String riskLabel(CommonCommand command) {
        switch (command.getRiskLevel()) {
            case R3_HIGH: return getString(R.string.risk_high);
            case R2_CONFIGURATION: return getString(R.string.risk_configuration);
            default: return getString(R.string.risk_read_only);
        }
    }

    private String categoryLabel(CommandCategory category) {
        int[] labels = {R.string.category_device, R.string.category_interface, R.string.category_vlan,
                R.string.category_layer3, R.string.category_routing, R.string.category_loop,
                R.string.category_aggregation, R.string.category_security,
                R.string.category_troubleshooting, R.string.category_save};
        return getString(labels[category.ordinal()]);
    }
}
