package de.kai_morich.simple_usb_terminal;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;
import androidx.core.widget.TextViewCompat;
import androidx.fragment.app.Fragment;

import com.chenwei666.netserial.navigation.FeatureCategory;
import com.chenwei666.netserial.navigation.FeatureDescriptor;
import com.chenwei666.netserial.navigation.FeatureId;
import com.chenwei666.netserial.navigation.FeatureRegistry;
import com.chenwei666.netserial.navigation.FeatureUsageManager;
import com.chenwei666.netserial.navigation.SharedPreferencesFeatureUsagePersistence;

public final class FeatureHubFragment extends Fragment {
    private static final String ARG_CATEGORY = "category";
    private final FeatureRegistry registry = FeatureRegistry.createDefault();
    private LinearLayout container;
    private FeatureUsageManager usage;
    private FeatureCategory category;

    public static FeatureHubFragment newInstance(FeatureCategory category) {
        FeatureHubFragment fragment = new FeatureHubFragment();
        Bundle args = new Bundle();
        args.putString(ARG_CATEGORY, category.name());
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable @Override public View onCreateView(@NonNull LayoutInflater inflater,
                                                  @Nullable ViewGroup parent,
                                                  @Nullable Bundle state) {
        return inflater.inflate(R.layout.fragment_feature_hub, parent, false);
    }

    @Override public void onViewCreated(@NonNull View view, @Nullable Bundle state) {
        String value = requireArguments().getString(ARG_CATEGORY, FeatureCategory.TOOLBOX.name());
        category = FeatureCategory.valueOf(value);
        usage = new FeatureUsageManager(new SharedPreferencesFeatureUsagePersistence(requireContext()));
        container = view.findViewById(R.id.feature_hub_container);
        TextView title = view.findViewById(R.id.feature_hub_title);
        TextView summary = view.findViewById(R.id.feature_hub_summary);
        boolean settings = category == FeatureCategory.SETTINGS;
        title.setText(settings ? R.string.settings_hub_title : R.string.beginner_tools_title);
        summary.setText(settings ? R.string.settings_hub_subtitle : R.string.beginner_tools_subtitle);
        view.findViewById(R.id.feature_hub_search).setOnClickListener(v ->
                FeatureSearchDialog.show(getParentFragmentManager()));
        render();
    }

    private void render() {
        container.removeAllViews();
        if (category == FeatureCategory.TOOLBOX) {
            addGroup(R.string.beginner_tools_ai, R.string.beginner_tools_ai_desc,
                    FeatureId.AI_COPILOT, FeatureId.COMMAND_LIBRARY);
            addGroup(R.string.beginner_tools_connect, R.string.beginner_tools_connect_desc,
                    FeatureId.NETWORK_TOOLS, FeatureId.USB_CONNECTIONS, FeatureId.REMOTE_CONNECTIONS,
                    FeatureId.SESSION_WORKSPACE, FeatureId.TERMINAL_WORKSPACE);
            addGroup(R.string.beginner_tools_config, R.string.beginner_tools_config_desc,
                    FeatureId.CONFIG_SNAPSHOTS, FeatureId.CONFIG_DIFF);
            addGroup(R.string.beginner_tools_advanced, R.string.beginner_tools_advanced_desc,
                    FeatureId.OPERATIONS_CENTER, FeatureId.ADVANCED_TOOLKIT, FeatureId.CHANGE_TASK);
        } else {
            for (FeatureDescriptor feature : registry.forCategory(category)) addFeature(feature);
        }
    }

    private void addGroup(int title, int description, FeatureId... features) {
        TextView heading = new TextView(requireContext());
        heading.setText(title);
        TextViewCompat.setTextAppearance(heading, com.google.android.material.R.style.TextAppearance_Material3_TitleMedium);
        heading.setPadding(0, dp(20), 0, dp(4));
        ViewCompat.setAccessibilityHeading(heading, true);
        container.addView(heading);
        TextView summary = new TextView(requireContext());
        summary.setText(description);
        TextViewCompat.setTextAppearance(summary, com.google.android.material.R.style.TextAppearance_Material3_BodyMedium);
        summary.setPadding(0, 0, 0, dp(12));
        container.addView(summary);
        for (FeatureId feature : features) addFeature(registry.require(feature));
    }

    private void addFeature(FeatureDescriptor feature) {
        View card = FeatureButtonFactory.create(requireContext(), feature);
        card.setOnClickListener(v -> {
            usage.recordOpen(feature.getId());
            FeatureNavigator.open(requireActivity(), feature.getId());
        });
        card.setOnLongClickListener(v -> {
            boolean added = usage.toggleFavorite(feature.getId());
            Toast.makeText(requireContext(), added ? R.string.feature_favorite_added
                    : R.string.feature_favorite_removed, Toast.LENGTH_SHORT).show();
            return true;
        });
        container.addView(card);
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    @Override public void onDestroyView() {
        container = null;
        super.onDestroyView();
    }
}
