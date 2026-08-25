package de.kai_morich.simple_usb_terminal;

import android.app.Dialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import com.chenwei666.netserial.navigation.FeatureDescriptor;
import com.chenwei666.netserial.navigation.FeatureRegistry;
import com.chenwei666.netserial.navigation.FeatureUsageManager;
import com.chenwei666.netserial.navigation.SharedPreferencesFeatureUsagePersistence;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.List;

public final class FeatureSearchDialog extends DialogFragment {
    private final FeatureRegistry registry = FeatureRegistry.createDefault();
    private LinearLayout results;
    private FeatureUsageManager usage;

    public static void show(FragmentManager manager) {
        if (manager.findFragmentByTag("feature-search") == null) {
            new FeatureSearchDialog().show(manager, "feature-search");
        }
    }

    @NonNull @Override public Dialog onCreateDialog(Bundle state) {
        usage = new FeatureUsageManager(new SharedPreferencesFeatureUsagePersistence(requireContext()));
        LinearLayout root = new LinearLayout(requireContext());
        root.setOrientation(LinearLayout.VERTICAL);
        int padding = Math.round(20 * getResources().getDisplayMetrics().density);
        root.setPadding(padding, 0, padding, padding);
        EditText query = new EditText(requireContext());
        query.setSingleLine(true);
        query.setHint(R.string.feature_search_hint);
        root.addView(query, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        ScrollView scroll = new ScrollView(requireContext());
        results = new LinearLayout(requireContext());
        results.setOrientation(LinearLayout.VERTICAL);
        scroll.addView(results, new ScrollView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        root.addView(scroll, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                Math.round(420 * getResources().getDisplayMetrics().density)));
        query.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { render(s.toString()); }
            @Override public void afterTextChanged(Editable s) { }
        });
        render("");
        return new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.feature_search)
                .setView(root)
                .setNegativeButton(android.R.string.cancel, null)
                .create();
    }

    private void render(String query) {
        results.removeAllViews();
        List<FeatureDescriptor> matches = registry.search(query, requireContext()::getString);
        if (matches.isEmpty()) {
            TextView empty = new TextView(requireContext());
            empty.setText(R.string.feature_search_empty);
            results.addView(empty);
            return;
        }
        for (FeatureDescriptor feature : matches) {
            View card =
                    FeatureButtonFactory.create(requireContext(), feature);
            card.setOnClickListener(v -> {
                usage.recordOpen(feature.getId());
                FeatureNavigator.open(requireActivity(), feature.getId());
                dismiss();
            });
            card.setOnLongClickListener(v -> {
                boolean added = usage.toggleFavorite(feature.getId());
                Toast.makeText(requireContext(), added ? R.string.feature_favorite_added
                        : R.string.feature_favorite_removed, Toast.LENGTH_SHORT).show();
                return true;
            });
            results.addView(card);
        }
    }
}
