package de.kai_morich.simple_usb_terminal;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;
import androidx.core.widget.TextViewCompat;
import androidx.fragment.app.Fragment;

import com.chenwei666.netserial.guided.GuidedScenario;
import com.chenwei666.netserial.navigation.FeatureDescriptor;
import com.chenwei666.netserial.navigation.FeatureId;
import com.chenwei666.netserial.navigation.FeatureRegistry;
import com.chenwei666.netserial.navigation.FeatureUsageManager;
import com.chenwei666.netserial.navigation.SharedPreferencesFeatureUsagePersistence;
import com.chenwei666.netserial.terminal.ActiveTerminalSessionStore;
import com.chenwei666.netserial.terminal.ActiveTerminalSnapshot;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.color.MaterialColors;
import com.google.android.material.navigation.NavigationBarView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/** Problem-first entry point; diagnostic execution belongs to the guided flow. */
public final class HomeFragment extends Fragment {
    private static final String STATE_SHORTCUTS = "shortcutsExpanded";
    private final FeatureRegistry registry = FeatureRegistry.createDefault();
    private LinearLayout tasksContainer;
    private LinearLayout favoritesContainer;
    private LinearLayout recentContainer;
    private TextView deviceStatus;
    private View shortcuts;
    private MaterialButton shortcutsToggle;
    private FeatureUsageManager usage;
    private boolean shortcutsExpanded;

    @Nullable @Override public View onCreateView(@NonNull LayoutInflater inflater,
                                                 @Nullable ViewGroup container,
                                                 @Nullable Bundle state) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override public void onViewCreated(@NonNull View view, @Nullable Bundle state) {
        usage = new FeatureUsageManager(new SharedPreferencesFeatureUsagePersistence(requireContext()));
        tasksContainer = view.findViewById(R.id.home_tasks_container);
        favoritesContainer = view.findViewById(R.id.home_favorite_container);
        recentContainer = view.findViewById(R.id.home_recent_container);
        deviceStatus = view.findViewById(R.id.home_device_status);
        shortcuts = view.findViewById(R.id.home_shortcuts);
        shortcutsToggle = view.findViewById(R.id.home_shortcuts_toggle);
        shortcutsExpanded = state != null && state.getBoolean(STATE_SHORTCUTS);
        shortcutsToggle.setOnClickListener(v -> {
            shortcutsExpanded = !shortcutsExpanded;
            renderShortcutState();
        });
        renderShortcutState();
        view.findViewById(R.id.home_connect).setOnClickListener(v -> open(FeatureId.USB_CONNECTIONS));
        view.findViewById(R.id.home_professional).setOnClickListener(v -> {
            NavigationBarView navigation = requireActivity().findViewById(R.id.bottom_navigation);
            navigation.setSelectedItemId(R.id.nav_toolbox);
        });
        EditText search = view.findViewById(R.id.home_problem_search);
        search.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                renderTasks(s.toString());
            }
            @Override public void afterTextChanged(Editable editable) { }
        });
        renderTasks(search.getText().toString());
    }

    @Override public void onResume() {
        super.onResume();
        renderStatus();
        renderLists();
    }

    @Override public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        // Bottom navigation retains fragments, so returning home need not call onResume.
        if (!hidden && getView() != null) {
            renderStatus();
            renderLists();
        }
    }

    @Override public void onSaveInstanceState(@NonNull Bundle state) {
        state.putBoolean(STATE_SHORTCUTS, shortcutsExpanded);
        super.onSaveInstanceState(state);
    }

    private void renderTasks(String query) {
        tasksContainer.removeAllViews();
        String needle = query.trim().toLowerCase(Locale.ROOT);
        int count = addTaskGroup(R.string.beginner_group_network, needle,
                GuidedScenario.NO_INTERNET, GuidedScenario.SLOW_NETWORK, GuidedScenario.DNS_FAILURE);
        count += addTaskGroup(R.string.beginner_group_switch, needle,
                GuidedScenario.SWITCH_LOGIN, GuidedScenario.PORT_LINK, GuidedScenario.VLAN_GATEWAY,
                GuidedScenario.IDENTIFY_DEVICE, GuidedScenario.SWITCH_HEALTH, GuidedScenario.CONFIG_BACKUP);
        count += addTaskGroup(R.string.beginner_group_help, needle, GuidedScenario.ASK_AI);
        if (count == 0) {
            TextView empty = new TextView(requireContext());
            empty.setText(R.string.beginner_search_empty);
            empty.setPadding(dp(8), dp(24), dp(8), dp(24));
            tasksContainer.addView(empty);
        }
    }

    private int addTaskGroup(int title, String query, GuidedScenario... scenarios) {
        List<GuidedScenario> matches = new ArrayList<>();
        for (GuidedScenario scenario : scenarios) {
            String searchable = getString(BeginnerTaskCatalog.title(scenario)) + " "
                    + getString(BeginnerTaskCatalog.description(scenario)) + " "
                    + BeginnerTaskCatalog.searchAliases(scenario);
            if (searchable.toLowerCase(Locale.ROOT).contains(query)) matches.add(scenario);
        }
        if (matches.isEmpty()) return 0;
        TextView heading = new TextView(requireContext());
        heading.setText(title);
        TextViewCompat.setTextAppearance(heading, com.google.android.material.R.style.TextAppearance_Material3_TitleMedium);
        heading.setPadding(dp(4), dp(20), dp(4), dp(12));
        ViewCompat.setAccessibilityHeading(heading, true);
        tasksContainer.addView(heading);
        int columns = getResources().getConfiguration().screenWidthDp >= 600
                && getResources().getConfiguration().fontScale < 1.5f ? 2 : 1;
        for (int index = 0; index < matches.size(); index += columns) {
            LinearLayout row = new LinearLayout(requireContext());
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setBaselineAligned(false);
            LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            rowParams.bottomMargin = dp(10);
            tasksContainer.addView(row, rowParams);
            for (int column = 0; column < columns; column++) {
                int position = index + column;
                View card = position < matches.size() ? taskCard(matches.get(position))
                        : new View(requireContext());
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0,
                        ViewGroup.LayoutParams.MATCH_PARENT, 1f);
                if (column > 0) params.setMarginStart(dp(12));
                row.addView(card, params);
            }
        }
        return matches.size();
    }

    private View taskCard(GuidedScenario scenario) {
        MaterialCardView card = new MaterialCardView(requireContext());
        card.setRadius(dp(18));
        card.setCardElevation(0);
        card.setStrokeWidth(dp(1));
        card.setStrokeColor(MaterialColors.getColor(card, com.google.android.material.R.attr.colorOutlineVariant));
        card.setCardBackgroundColor(MaterialColors.getColor(card, com.google.android.material.R.attr.colorSurface));
        card.setClickable(true);
        card.setFocusable(true);
        card.setMinimumHeight(dp(112));
        card.setContentDescription(getString(BeginnerTaskCatalog.title(scenario)) + ". "
                + getString(BeginnerTaskCatalog.description(scenario)));
        card.setOnClickListener(v -> startActivity(new Intent(requireContext(), BeginnerGuideActivity.class)
                .putExtra(BeginnerGuideActivity.EXTRA_SCENARIO, scenario.name())));
        LinearLayout content = new LinearLayout(requireContext());
        content.setOrientation(LinearLayout.HORIZONTAL);
        content.setGravity(Gravity.CENTER_VERTICAL);
        content.setPadding(dp(16), dp(18), dp(16), dp(18));
        content.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS);
        ImageView icon = new ImageView(requireContext());
        icon.setImageResource(BeginnerTaskCatalog.icon(scenario));
        icon.setImageTintList(ColorStateList.valueOf(MaterialColors.getColor(card,
                androidx.appcompat.R.attr.colorPrimary)));
        content.addView(icon, new LinearLayout.LayoutParams(dp(28), dp(28)));
        LinearLayout text = new LinearLayout(requireContext());
        text.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(0,
                ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        textParams.setMarginStart(dp(16));
        content.addView(text, textParams);
        TextView label = new TextView(requireContext());
        label.setText(BeginnerTaskCatalog.title(scenario));
        TextViewCompat.setTextAppearance(label, com.google.android.material.R.style.TextAppearance_Material3_TitleMedium);
        label.setTextColor(MaterialColors.getColor(card, com.google.android.material.R.attr.colorOnSurface));
        text.addView(label);
        TextView description = new TextView(requireContext());
        description.setText(BeginnerTaskCatalog.description(scenario));
        TextViewCompat.setTextAppearance(description, com.google.android.material.R.style.TextAppearance_Material3_BodyMedium);
        description.setTextColor(MaterialColors.getColor(card, com.google.android.material.R.attr.colorOnSurfaceVariant));
        description.setPadding(0, dp(5), 0, 0);
        text.addView(description);
        card.addView(content, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        return card;
    }

    private void renderShortcutState() {
        shortcuts.setVisibility(shortcutsExpanded ? View.VISIBLE : View.GONE);
        shortcutsToggle.setText(shortcutsExpanded ? R.string.beginner_shortcuts_hide : R.string.beginner_shortcuts_show);
        ViewCompat.setStateDescription(shortcutsToggle,
                getString(shortcutsExpanded ? R.string.beginner_expanded : R.string.beginner_collapsed));
    }

    private void renderLists() {
        List<FeatureId> favorites = usage.favorites();
        if (favorites.isEmpty()) favorites = Arrays.asList(FeatureId.REMOTE_CONNECTIONS,
                FeatureId.AI_COPILOT, FeatureId.COMMAND_LIBRARY, FeatureId.NETWORK_TOOLS);
        render(favoritesContainer, favorites);
        List<FeatureId> recent = usage.recent();
        if (recent.isEmpty()) {
            recentContainer.removeAllViews();
            TextView empty = new TextView(requireContext());
            empty.setText(R.string.home_no_recent);
            recentContainer.addView(empty);
        } else render(recentContainer, recent);
    }

    private void render(LinearLayout container, List<FeatureId> ids) {
        container.removeAllViews();
        for (FeatureId id : ids) {
            FeatureDescriptor feature;
            try { feature = registry.require(id); } catch (IllegalArgumentException ignored) { continue; }
            View card = FeatureButtonFactory.create(requireContext(), feature, true);
            card.setOnClickListener(v -> open(id));
            card.setOnLongClickListener(v -> {
                boolean added = usage.toggleFavorite(id);
                Toast.makeText(requireContext(), added ? R.string.feature_favorite_added
                        : R.string.feature_favorite_removed, Toast.LENGTH_SHORT).show();
                renderLists();
                return true;
            });
            container.addView(card);
        }
    }

    private void open(FeatureId id) {
        usage.recordOpen(id);
        FeatureNavigator.open(requireActivity(), id);
    }

    private void renderStatus() {
        ActiveTerminalSnapshot snapshot = ActiveTerminalSessionStore.shared().latestConnected();
        deviceStatus.setText(snapshot == null ? getString(R.string.beginner_no_connection)
                : getString(R.string.beginner_connected, snapshot.getTransport().name(), snapshot.getTargetLabel()));
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    @Override public void onDestroyView() {
        tasksContainer = null;
        favoritesContainer = null;
        recentContainer = null;
        deviceStatus = null;
        shortcuts = null;
        shortcutsToggle = null;
        super.onDestroyView();
    }
}
