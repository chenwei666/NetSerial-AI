package de.kai_morich.simple_usb_terminal;

import android.content.Intent;

import androidx.fragment.app.FragmentActivity;

import com.chenwei666.netserial.navigation.FeatureId;

/** The only UI seam that maps a feature identity to Android navigation. */
public final class FeatureNavigator {
    private FeatureNavigator() { }

    public static void open(FragmentActivity activity, FeatureId id) {
        if (activity == null || id == null) return;
        if (activity instanceof MainActivity) {
            MainActivity main = (MainActivity) activity;
            if (id == FeatureId.USB_CONNECTIONS) { main.selectConnections(); return; }
            if (id == FeatureId.TERMINAL_WORKSPACE) { main.selectTerminal(); return; }
        }
        Class<?> destination;
        switch (id) {
            case REMOTE_CONNECTIONS: destination = RemoteTerminalActivity.class; break;
            case SESSION_WORKSPACE: destination = SessionWorkspaceActivity.class; break;
            case AI_COPILOT: destination = AiCopilotActivity.class; break;
            case COMMAND_LIBRARY: destination = CommandLibraryActivity.class; break;
            case NETWORK_TOOLS: destination = NetworkToolsActivity.class; break;
            case OPERATIONS_CENTER: destination = OperationsCenterActivity.class; break;
            case ADVANCED_TOOLKIT: destination = AdvancedToolkitActivity.class; break;
            case CHANGE_TASK: destination = ChangeTaskActivity.class; break;
            case CONFIG_DIFF: destination = ConfigDiffActivity.class; break;
            case CONFIG_SNAPSHOTS: destination = ConfigSnapshotCenterActivity.class; break;
            case DEVICE_MEMORY: destination = DeviceMemoryActivity.class; break;
            case APP_SETTINGS: destination = AppSettingsActivity.class; break;
            case AI_SETTINGS: destination = AiProviderSettingsActivity.class; break;
            case USB_CONNECTIONS:
            case TERMINAL_WORKSPACE:
            default: return;
        }
        activity.startActivity(new Intent(activity, destination));
    }
}
