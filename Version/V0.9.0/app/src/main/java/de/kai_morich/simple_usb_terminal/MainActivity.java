package de.kai_morich.simple_usb_terminal;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.chenwei666.netserial.navigation.FeatureCategory;
import com.google.android.material.navigation.NavigationBarView;

public class MainActivity extends ThemedActivity {
    private static final String STATE_NAVIGATION = "navigation";
    private static final String TAG_HOME = "nav-home";
    private static final String TAG_CONNECTIONS = "nav-connections";
    private static final String TAG_TERMINAL_HUB = "nav-terminal-hub";
    private static final String TAG_TERMINAL = "terminal";
    private static final String TAG_TOOLBOX = "nav-toolbox";
    private static final String TAG_SETTINGS = "nav-settings";

    private NavigationBarView bottomNavigation;
    private int selectedNavigation = R.id.nav_home;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            Insets ime = insets.getInsets(WindowInsetsCompat.Type.ime());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, Math.max(systemBars.bottom, ime.bottom));
            return insets;
        });
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        bottomNavigation = findViewById(R.id.bottom_navigation);
        bottomNavigation.setOnItemSelectedListener(item -> showNavigation(item.getItemId()));
        selectedNavigation = savedInstanceState == null ? R.id.nav_home
                : savedInstanceState.getInt(STATE_NAVIGATION, R.id.nav_home);
        bottomNavigation.setSelectedItemId(selectedNavigation);
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override public void handleOnBackPressed() {
                if (selectedNavigation != R.id.nav_home) {
                    bottomNavigation.setSelectedItemId(R.id.nav_home);
                    return;
                }
                setEnabled(false);
                getOnBackPressedDispatcher().onBackPressed();
            }
        });
        UpdateCheckCoordinator.checkOnStartup(this);
    }

    @Override protected void onSaveInstanceState(@NonNull Bundle state) {
        state.putInt(STATE_NAVIGATION, selectedNavigation);
        super.onSaveInstanceState(state);
    }

    @Override public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.feature_search) {
            FeatureSearchDialog.show(getSupportFragmentManager());
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void selectConnections() { bottomNavigation.setSelectedItemId(R.id.nav_connections); }
    public void selectTerminal() { bottomNavigation.setSelectedItemId(R.id.nav_terminal); }

    public void openUsbTerminal(Bundle arguments) {
        Fragment previous = getSupportFragmentManager().findFragmentByTag(TAG_TERMINAL);
        TerminalFragment terminal = new TerminalFragment();
        terminal.setArguments(arguments);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        if (previous != null) transaction.remove(previous);
        hideContainerFragments(transaction);
        transaction.add(R.id.fragment, terminal, TAG_TERMINAL).commit();
        selectedNavigation = R.id.nav_terminal;
        bottomNavigation.getMenu().findItem(R.id.nav_terminal).setChecked(true);
        updateTitle(R.string.nav_terminal);
    }

    private boolean showNavigation(int itemId) {
        selectedNavigation = itemId;
        Fragment fragment;
        String tag;
        int title;
        if (itemId == R.id.nav_connections) {
            tag = TAG_CONNECTIONS;
            fragment = findOrCreate(tag, new DevicesFragment());
            title = R.string.nav_connections;
        } else if (itemId == R.id.nav_terminal) {
            Fragment active = getSupportFragmentManager().findFragmentByTag(TAG_TERMINAL);
            tag = active == null ? TAG_TERMINAL_HUB : TAG_TERMINAL;
            fragment = active == null ? findOrCreate(tag, new TerminalHubFragment()) : active;
            title = R.string.nav_terminal;
        } else if (itemId == R.id.nav_toolbox) {
            tag = TAG_TOOLBOX;
            fragment = findOrCreate(tag, FeatureHubFragment.newInstance(FeatureCategory.TOOLBOX));
            title = R.string.nav_toolbox;
        } else if (itemId == R.id.nav_settings) {
            tag = TAG_SETTINGS;
            fragment = findOrCreate(tag, FeatureHubFragment.newInstance(FeatureCategory.SETTINGS));
            title = R.string.nav_settings;
        } else {
            tag = TAG_HOME;
            fragment = findOrCreate(tag, new HomeFragment());
            title = R.string.nav_home;
        }
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        hideContainerFragments(transaction);
        if (fragment.isAdded()) transaction.show(fragment);
        else transaction.add(R.id.fragment, fragment, tag);
        transaction.commit();
        updateTitle(title);
        return true;
    }

    private Fragment findOrCreate(String tag, Fragment fallback) {
        Fragment existing = getSupportFragmentManager().findFragmentByTag(tag);
        return existing == null ? fallback : existing;
    }

    private void hideContainerFragments(FragmentTransaction transaction) {
        for (Fragment fragment : getSupportFragmentManager().getFragments()) {
            if (fragment.isAdded() && fragment.getId() == R.id.fragment) transaction.hide(fragment);
        }
    }

    private void updateTitle(int title) {
        if (getSupportActionBar() != null) getSupportActionBar().setTitle(title);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        if ("android.hardware.usb.action.USB_DEVICE_ATTACHED".equals(intent.getAction())) {
            TerminalFragment terminal = (TerminalFragment)getSupportFragmentManager().findFragmentByTag(TAG_TERMINAL);
            if (terminal != null)
                terminal.status("USB device detected");
        }
        super.onNewIntent(intent);
    }

}
