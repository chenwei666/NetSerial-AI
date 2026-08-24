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

import com.chenwei666.netserial.change.ChangeTask;
import com.chenwei666.netserial.change.ChangeTaskStore;
import com.chenwei666.netserial.device.DeviceEnvironment;
import com.chenwei666.netserial.device.DeviceProfile;
import com.chenwei666.netserial.device.DeviceProfileStore;
import com.chenwei666.netserial.device.Vendor;
import com.chenwei666.netserial.navigation.FeatureDescriptor;
import com.chenwei666.netserial.navigation.FeatureId;
import com.chenwei666.netserial.navigation.FeatureRegistry;
import com.chenwei666.netserial.navigation.FeatureUsageManager;
import com.chenwei666.netserial.navigation.SharedPreferencesFeatureUsagePersistence;
import com.google.android.material.button.MaterialButton;

import java.util.Arrays;
import java.util.List;

public final class HomeFragment extends Fragment {
    private final FeatureRegistry registry = FeatureRegistry.createDefault();
    private LinearLayout favoritesContainer;
    private LinearLayout recentContainer;
    private TextView deviceStatus;
    private TextView changeStatus;
    private FeatureUsageManager usage;

    @Nullable @Override public View onCreateView(@NonNull LayoutInflater inflater,
                                                  @Nullable ViewGroup container,
                                                  @Nullable Bundle state) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override public void onViewCreated(@NonNull View view, @Nullable Bundle state) {
        usage = new FeatureUsageManager(new SharedPreferencesFeatureUsagePersistence(requireContext()));
        favoritesContainer = view.findViewById(R.id.home_favorite_container);
        recentContainer = view.findViewById(R.id.home_recent_container);
        deviceStatus = view.findViewById(R.id.home_device_status);
        changeStatus = view.findViewById(R.id.home_change_status);
        view.findViewById(R.id.home_search).setOnClickListener(v -> FeatureSearchDialog.show(getParentFragmentManager()));
    }

    @Override public void onResume() {
        super.onResume();
        renderStatus();
        renderLists();
    }

    private void renderLists() {
        List<FeatureId> favorites = usage.favorites();
        if (favorites.isEmpty()) favorites = Arrays.asList(FeatureId.REMOTE_CONNECTIONS,
                FeatureId.AI_COPILOT, FeatureId.COMMAND_LIBRARY, FeatureId.NETWORK_TOOLS);
        render(favoritesContainer, favorites, false);
        List<FeatureId> recent = usage.recent();
        if (recent.isEmpty()) {
            recentContainer.removeAllViews();
            TextView empty = new TextView(requireContext());
            empty.setText(R.string.home_no_recent);
            recentContainer.addView(empty);
        } else {
            render(recentContainer, recent, true);
        }
    }

    private void render(LinearLayout container, List<FeatureId> ids, boolean compact) {
        container.removeAllViews();
        for (FeatureId id : ids) {
            FeatureDescriptor feature;
            try { feature = registry.require(id); } catch (IllegalArgumentException ignored) { continue; }
            MaterialButton button = FeatureButtonFactory.create(requireContext(), feature);
            if (compact) button.setText(getString(feature.getTitleResource()));
            button.setOnClickListener(v -> open(id));
            button.setOnLongClickListener(v -> {
                boolean added = usage.toggleFavorite(id);
                Toast.makeText(requireContext(), added ? R.string.feature_favorite_added
                        : R.string.feature_favorite_removed, Toast.LENGTH_SHORT).show();
                renderLists();
                return true;
            });
            container.addView(button);
        }
    }

    private void open(FeatureId id) {
        usage.recordOpen(id);
        FeatureNavigator.open(requireActivity(), id);
    }

    private void renderStatus() {
        DeviceProfile profile = new DeviceProfileStore(requireContext()).load();
        deviceStatus.setText(getString(R.string.dashboard_profile_format, profile.getName(),
                vendorLabel(profile.getVendor()), environmentLabel(profile.getEnvironment()),
                getString(profile.isProtectedDevice() ? R.string.target_protected : R.string.target_unprotected)));
        ChangeTask task = new ChangeTaskStore(requireContext()).loadActive();
        changeStatus.setText(task == null ? getString(R.string.dashboard_no_change)
                : getString(R.string.dashboard_active_change, task.getTicketNumber(), task.getDeviceName()));
    }

    private String vendorLabel(Vendor vendor) {
        switch (vendor) {
            case GENERIC: return getString(R.string.vendor_generic);
            case HUAWEI_VRP: return getString(R.string.vendor_huawei);
            case CISCO_IOS: return getString(R.string.vendor_cisco);
            case RUIJIE_RGOS: return getString(R.string.vendor_ruijie);
            case H3C_COMWARE:
            default: return getString(R.string.vendor_h3c);
        }
    }

    private String environmentLabel(DeviceEnvironment environment) {
        switch (environment) {
            case PRODUCTION: return getString(R.string.environment_production);
            case TEST: return getString(R.string.environment_test);
            case LAB:
            default: return getString(R.string.environment_lab);
        }
    }
}
