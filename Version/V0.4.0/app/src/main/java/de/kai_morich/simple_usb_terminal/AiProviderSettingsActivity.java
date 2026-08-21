package de.kai_morich.simple_usb_terminal;

import android.app.AlertDialog;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.chenwei666.netserial.settings.AppLocaleController;

import com.chenwei666.netserial.ai.AiProviderCatalog;
import com.chenwei666.netserial.ai.AiProviderError;
import com.chenwei666.netserial.ai.AiProviderPreset;
import com.chenwei666.netserial.ai.AiProviderPresetCatalog;
import com.chenwei666.netserial.ai.CredentialVault;
import com.chenwei666.netserial.ai.CredentialVaultException;
import com.chenwei666.netserial.ai.CredentialVaultFactory;
import com.chenwei666.netserial.ai.ProviderCredentialService;
import com.chenwei666.netserial.ai.ProviderProfile;
import com.chenwei666.netserial.ai.ProviderProfileManager;
import com.chenwei666.netserial.ai.ProviderProfileStoreException;
import com.chenwei666.netserial.ai.ProviderProfilesState;
import com.chenwei666.netserial.ai.SharedPreferencesProviderProfilePersistence;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public final class AiProviderSettingsActivity extends AppCompatActivity {
    private final AiProviderCatalog providerCatalog = AiProviderCatalog.createDefault();
    private final AiProviderPresetCatalog presetCatalog = AiProviderPresetCatalog.createDefault();
    private final List<String> providerIds = new ArrayList<>();
    private final List<ProfileOption> profileOptions = new ArrayList<>();
    private final AiConnectionTestCoordinator connectionTestCoordinator =
            new AiConnectionTestCoordinator();

    private ProviderProfileManager profileManager;
    private ProviderCredentialService credentialService;
    private ProviderProfilesState profilesState = ProviderProfilesState.empty();
    private String selectedCredentialAlias;
    private boolean updatingUi;

    private Spinner profileSpinner;
    private Spinner providerSpinner;
    private EditText endpointInput;
    private EditText modelInput;
    private EditText keyInput;
    private TextView activeStatus;
    private TextView credentialStatus;
    private TextView adapterStatus;
    private TextView operationStatus;
    private Button setActiveButton;
    private Button testButton;
    private Button deleteButton;
    private ProgressBar testProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppLocaleController.applyStoredLanguage(this);
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
        setContentView(R.layout.activity_ai_provider_settings);
        configureInsets();
        configureToolbar();
        bindViews();

        profileManager = new ProviderProfileManager(
                new SharedPreferencesProviderProfilePersistence(this)
        );
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                CredentialVault vault = CredentialVaultFactory.create(this);
                credentialService = new ProviderCredentialService(vault);
            } catch (CredentialVaultException exception) {
                credentialService = null;
            }
        }

        configureProviderSpinner();
        configureProfileSpinner();
        bindActions();
        reloadProfiles(null);
    }

    private void configureInsets() {
        View root = findViewById(R.id.ai_settings_root);
        ViewCompat.setOnApplyWindowInsetsListener(root, (view, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            Insets ime = insets.getInsets(WindowInsetsCompat.Type.ime());
            view.setPadding(
                    bars.left,
                    bars.top,
                    bars.right,
                    Math.max(bars.bottom, ime.bottom)
            );
            return insets;
        });
    }

    private void configureToolbar() {
        Toolbar toolbar = findViewById(R.id.ai_settings_toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(view -> finish());
    }

    private void bindViews() {
        profileSpinner = findViewById(R.id.ai_profile_spinner);
        providerSpinner = findViewById(R.id.ai_provider_spinner);
        endpointInput = findViewById(R.id.ai_endpoint_input);
        modelInput = findViewById(R.id.ai_model_input);
        keyInput = findViewById(R.id.ai_key_input);
        activeStatus = findViewById(R.id.ai_active_status);
        credentialStatus = findViewById(R.id.ai_credential_status);
        adapterStatus = findViewById(R.id.ai_adapter_status);
        operationStatus = findViewById(R.id.ai_operation_status);
        setActiveButton = findViewById(R.id.ai_set_active_button);
        testButton = findViewById(R.id.ai_test_button);
        deleteButton = findViewById(R.id.ai_delete_button);
        testProgress = findViewById(R.id.ai_test_progress);
    }

    private void configureProviderSpinner() {
        providerIds.addAll(providerCatalog.getProviderIds());
        List<String> labels = new ArrayList<>();
        for (String providerId : providerIds) {
            labels.add(providerCatalog.require(providerId).getDisplayName());
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                labels
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        providerSpinner.setAdapter(adapter);
        providerSpinner.setOnItemSelectedListener(new SimpleItemSelectedListener(position -> {
            if (!updatingUi) {
                applyPreset(providerIds.get(position));
            }
            updateAdapterStatus();
        }));
    }

    private void configureProfileSpinner() {
        profileSpinner.setOnItemSelectedListener(new SimpleItemSelectedListener(position -> {
            if (!updatingUi && position >= 0 && position < profileOptions.size()) {
                renderProfile(profileOptions.get(position).profile);
            }
        }));
    }

    private void bindActions() {
        findViewById(R.id.ai_save_button).setOnClickListener(view -> saveProfile());
        setActiveButton.setOnClickListener(view -> setActiveProfile());
        deleteButton.setOnClickListener(view -> confirmDeleteProfile());
        testButton.setOnClickListener(view -> {
            if (connectionTestCoordinator.isRunning()) {
                connectionTestCoordinator.cancel();
            } else {
                confirmConnectionTest();
            }
        });
    }

    private void reloadProfiles(String preferredAlias) {
        try {
            profilesState = profileManager.load();
        } catch (ProviderProfileStoreException exception) {
            showStatus(R.string.ai_profiles_load_failed);
            profilesState = ProviderProfilesState.empty();
        }

        updatingUi = true;
        profileOptions.clear();
        profileOptions.add(new ProfileOption(getString(R.string.ai_new_profile), null));
        int selectedIndex = 0;
        String targetAlias = preferredAlias == null
                ? profilesState.getActiveCredentialAlias()
                : preferredAlias;
        for (ProviderProfile profile : profilesState.getProfiles()) {
            String providerName = providerCatalog.require(profile.getProviderId()).getDisplayName();
            String label = getString(
                    profilesState.isActive(profile)
                            ? R.string.ai_profile_list_active
                            : R.string.ai_profile_list_item,
                    providerName,
                    profile.getModel()
            );
            profileOptions.add(new ProfileOption(label, profile));
            if (profile.getCredentialAlias().equals(targetAlias)) {
                selectedIndex = profileOptions.size() - 1;
            }
        }
        ArrayAdapter<ProfileOption> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                profileOptions
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        profileSpinner.setAdapter(adapter);
        profileSpinner.setSelection(selectedIndex, false);
        updatingUi = false;
        renderProfile(profileOptions.get(selectedIndex).profile);
    }

    private void renderProfile(ProviderProfile profile) {
        updatingUi = true;
        selectedCredentialAlias = profile == null ? null : profile.getCredentialAlias();
        keyInput.setText("");
        if (profile == null) {
            providerSpinner.setSelection(0, false);
            applyPreset(providerIds.get(0));
        } else {
            selectProvider(profile.getProviderId());
            endpointInput.setText(profile.getEndpoint().toString());
            modelInput.setText(profile.getModel());
        }
        updatingUi = false;
        updateProfileStatus(profile);
        updateAdapterStatus();
    }

    private void applyPreset(String providerId) {
        AiProviderPreset preset = presetCatalog.require(providerId);
        endpointInput.setText(preset.getEndpoint());
        modelInput.setText(preset.getModel());
    }

    private void selectProvider(String providerId) {
        int index = providerIds.indexOf(providerId);
        if (index >= 0) {
            providerSpinner.setSelection(index, false);
        }
    }

    private void updateProfileStatus(ProviderProfile profile) {
        boolean existing = profile != null;
        boolean active = existing && profilesState.isActive(profile);
        activeStatus.setText(active
                ? R.string.ai_profile_active
                : R.string.ai_profile_not_active);
        setActiveButton.setEnabled(existing && !active);
        deleteButton.setEnabled(existing);

        if (credentialService == null) {
            keyInput.setEnabled(false);
            credentialStatus.setText(R.string.ai_key_storage_unavailable);
            testButton.setEnabled(false);
            return;
        }
        keyInput.setEnabled(true);
        boolean hasCredential = "ollama".equals(profile == null ? null : profile.getProviderId());
        if (existing) {
            try {
                hasCredential = credentialService.hasCredential(profile);
            } catch (CredentialVaultException exception) {
                credentialStatus.setText(R.string.ai_key_status_failed);
                testButton.setEnabled(false);
                return;
            }
        }
        credentialStatus.setText(hasCredential
                ? R.string.ai_key_saved
                : R.string.ai_key_not_saved);
        testButton.setEnabled(existing && hasCredential);
    }

    private void updateAdapterStatus() {
        if (providerIds.isEmpty()) {
            return;
        }
        String providerId = providerIds.get(providerSpinner.getSelectedItemPosition());
        boolean compatible = presetCatalog.require(providerId).isOpenAiCompatible();
        adapterStatus.setText(compatible
                ? R.string.ai_adapter_compatible
                : R.string.ai_adapter_native_ready);
        ProviderProfile selected = findProfile(selectedCredentialAlias);
        if (selected != null && credentialService != null) {
            try {
                testButton.setEnabled("ollama".equals(selected.getProviderId())
                        || credentialService.hasCredential(selected));
            } catch (CredentialVaultException exception) {
                testButton.setEnabled(false);
            }
        }
    }

    private void saveProfile() {
        ProviderProfile existing = findProfile(selectedCredentialAlias);
        String alias = existing == null ? newCredentialAlias() : existing.getCredentialAlias();
        ProviderProfile profile;
        try {
            profile = buildProfile(alias);
        } catch (RuntimeException exception) {
            showStatus(R.string.ai_profile_validation_failed);
            return;
        }

        char[] credential = copyCredential();
        try {
            if (credential.length > 0 && credentialService == null) {
                showStatus(R.string.ai_key_storage_unavailable);
                return;
            }
            if (existing != null
                    && credentialDestinationChanged(existing, profile)) {
                boolean existingHasCredential;
                try {
                    existingHasCredential = hasCredential(existing);
                } catch (CredentialVaultException exception) {
                    showStatus(R.string.ai_key_status_failed);
                    return;
                }
                if (existingHasCredential) {
                    replaceCredentialDestination(existing, credential);
                    return;
                }
            }
            try {
                profileManager.upsert(profile);
            } catch (ProviderProfileStoreException exception) {
                showStatus(R.string.ai_profile_save_failed);
                return;
            }
            if (credential.length > 0) {
                try {
                    credentialService.save(profile, credential);
                } catch (CredentialVaultException exception) {
                    reloadProfiles(profile.getCredentialAlias());
                    showStatus(R.string.ai_profile_saved_key_failed);
                    return;
                }
            }
            keyInput.setText("");
            reloadProfiles(profile.getCredentialAlias());
            showStatus(R.string.ai_profile_saved);
        } finally {
            Arrays.fill(credential, '\0');
        }
    }

    private void replaceCredentialDestination(
            ProviderProfile existing,
            char[] replacementCredential
    ) {
        if (replacementCredential.length == 0 || credentialService == null) {
            showStatus(R.string.ai_key_required_for_destination_change);
            return;
        }
        ProviderProfile replacement;
        try {
            replacement = buildProfile(newCredentialAlias());
            credentialService.save(replacement, replacementCredential);
        } catch (RuntimeException exception) {
            showStatus(R.string.ai_profile_saved_key_failed);
            return;
        }

        try {
            profileManager.replace(existing.getCredentialAlias(), replacement);
        } catch (RuntimeException exception) {
            try {
                credentialService.delete(replacement);
            } catch (RuntimeException ignored) {
                // The replacement profile was not persisted, so this ciphertext is unreachable.
            }
            showStatus(R.string.ai_profile_save_failed);
            return;
        }

        boolean oldCredentialDeleted = true;
        try {
            credentialService.delete(existing);
        } catch (RuntimeException exception) {
            oldCredentialDeleted = false;
        }
        keyInput.setText("");
        reloadProfiles(replacement.getCredentialAlias());
        showStatus(oldCredentialDeleted
                ? R.string.ai_profile_saved
                : R.string.ai_profile_saved_old_key_cleanup_failed);
    }

    private boolean hasCredential(ProviderProfile profile) {
        return credentialService != null && credentialService.hasCredential(profile);
    }

    private static boolean credentialDestinationChanged(
            ProviderProfile existing,
            ProviderProfile replacement
    ) {
        return !existing.getProviderId().equals(replacement.getProviderId())
                || !existing.getEndpoint().equals(replacement.getEndpoint());
    }

    private static String newCredentialAlias() {
        return "ai-profile-" + UUID.randomUUID();
    }

    private ProviderProfile buildProfile(String alias) {
        int providerPosition = providerSpinner.getSelectedItemPosition();
        if (providerPosition < 0 || providerPosition >= providerIds.size()) {
            throw new IllegalArgumentException("provider is missing");
        }
        String providerId = providerIds.get(providerPosition);
        providerCatalog.require(providerId);
        return ProviderProfile.remote(
                providerId,
                endpointInput.getText().toString(),
                modelInput.getText().toString(),
                alias
        );
    }

    private char[] copyCredential() {
        Editable editable = keyInput.getText();
        char[] credential = new char[editable.length()];
        for (int index = 0; index < editable.length(); index++) {
            credential[index] = editable.charAt(index);
        }
        return credential;
    }

    private void setActiveProfile() {
        if (selectedCredentialAlias == null) {
            showStatus(R.string.ai_save_before_action);
            return;
        }
        try {
            profileManager.setActive(selectedCredentialAlias);
            reloadProfiles(selectedCredentialAlias);
            showStatus(R.string.ai_profile_activated);
        } catch (ProviderProfileStoreException exception) {
            showStatus(R.string.ai_profile_activate_failed);
        }
    }

    private void confirmDeleteProfile() {
        if (selectedCredentialAlias == null) {
            return;
        }
        new AlertDialog.Builder(this)
                .setTitle(R.string.ai_delete_profile)
                .setMessage(R.string.ai_delete_confirmation)
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(R.string.ai_delete_confirm, (dialog, which) -> deleteProfile())
                .show();
    }

    private void deleteProfile() {
        ProviderProfile profile = findProfile(selectedCredentialAlias);
        if (profile == null) {
            showStatus(R.string.ai_profile_delete_failed);
            return;
        }
        try {
            if (credentialService != null && credentialService.hasCredential(profile)) {
                credentialService.delete(profile);
            }
            profileManager.delete(profile.getCredentialAlias());
            reloadProfiles(null);
            showStatus(R.string.ai_profile_deleted);
        } catch (CredentialVaultException | ProviderProfileStoreException exception) {
            showStatus(R.string.ai_profile_delete_failed);
        }
    }

    private ProviderProfile findProfile(String alias) {
        if (alias == null) {
            return null;
        }
        for (ProviderProfile profile : profilesState.getProfiles()) {
            if (alias.equals(profile.getCredentialAlias())) {
                return profile;
            }
        }
        return null;
    }

    private void confirmConnectionTest() {
        if (selectedCredentialAlias == null) {
            showStatus(R.string.ai_save_before_action);
            return;
        }
        new AlertDialog.Builder(this)
                .setTitle(R.string.ai_test_connection)
                .setMessage(R.string.ai_test_confirmation)
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(R.string.ai_test_confirm, (dialog, which) -> startConnectionTest())
                .show();
    }

    private void startConnectionTest() {
        ProviderProfile profile;
        try {
            profile = buildProfile(selectedCredentialAlias);
            if (credentialService == null || (!"ollama".equals(profile.getProviderId())
                    && !credentialService.hasCredential(profile))) {
                showStatus(R.string.ai_key_required);
                return;
            }
        } catch (RuntimeException exception) {
            showStatus(R.string.ai_profile_validation_failed);
            return;
        }

        testProgress.setVisibility(View.VISIBLE);
        testButton.setText(R.string.ai_cancel_test);
        showStatus(R.string.ai_test_running);
        connectionTestCoordinator.start(
                profile,
                credentialService,
                new AiConnectionTestCoordinator.Listener() {
                    @Override
                    public void onSuccess(int stepCount) {
                        postTestSuccess(stepCount);
                    }

                    @Override
                    public void onProviderFailure(AiProviderError error) {
                        postTestError(AiProviderErrorMessageResolver.resolve(error));
                    }

                    @Override
                    public void onCredentialFailure() {
                        postTestError(R.string.ai_key_status_failed);
                    }

                    @Override
                    public void onUnexpectedFailure() {
                        postTestError(R.string.ai_test_error_unknown);
                    }
                }
        );
    }

    private void postTestSuccess(int stepCount) {
        runOnUiThread(() -> {
            if (isFinishingOrDestroyed()) {
                return;
            }
            finishConnectionTest();
            operationStatus.setText(getString(R.string.ai_test_success, stepCount));
        });
    }

    private void postTestError(@StringRes int message) {
        runOnUiThread(() -> {
            if (isFinishingOrDestroyed()) {
                return;
            }
            finishConnectionTest();
            showStatus(message);
        });
    }

    private void finishConnectionTest() {
        testProgress.setVisibility(View.GONE);
        testButton.setText(R.string.ai_test_connection);
    }

    private boolean isFinishingOrDestroyed() {
        return isFinishing() || isDestroyed();
    }

    private void showStatus(@StringRes int message) {
        operationStatus.setText(message);
    }

    @Override
    protected void onStop() {
        keyInput.setText("");
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        connectionTestCoordinator.close();
        super.onDestroy();
    }

    private static final class ProfileOption {
        private final String label;
        private final ProviderProfile profile;

        private ProfileOption(String label, ProviderProfile profile) {
            this.label = label;
            this.profile = profile;
        }

        @Override
        public String toString() {
            return label;
        }
    }
}
