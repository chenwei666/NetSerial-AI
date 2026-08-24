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
import androidx.fragment.app.Fragment;

import com.chenwei666.netserial.navigation.FeatureCategory;
import com.chenwei666.netserial.navigation.FeatureDescriptor;
import com.chenwei666.netserial.navigation.FeatureRegistry;
import com.chenwei666.netserial.navigation.FeatureUsageManager;
import com.chenwei666.netserial.navigation.SharedPreferencesFeatureUsagePersistence;
import com.google.android.material.button.MaterialButton;

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
        title.setText(settings ? R.string.settings_hub_title : R.string.toolbox_title);
        summary.setText(settings ? R.string.settings_hub_subtitle : R.string.toolbox_subtitle);
        view.findViewById(R.id.feature_hub_search).setOnClickListener(v ->
                FeatureSearchDialog.show(getParentFragmentManager()));
        render();
    }

    private void render() {
        container.removeAllViews();
        for (FeatureDescriptor feature : registry.forCategory(category)) {
            MaterialButton button = FeatureButtonFactory.create(requireContext(), feature);
            button.setOnClickListener(v -> {
                usage.recordOpen(feature.getId());
                FeatureNavigator.open(requireActivity(), feature.getId());
            });
            button.setOnLongClickListener(v -> {
                boolean added = usage.toggleFavorite(feature.getId());
                Toast.makeText(requireContext(), added ? R.string.feature_favorite_added
                        : R.string.feature_favorite_removed, Toast.LENGTH_SHORT).show();
                return true;
            });
            container.addView(button);
        }
    }
}
