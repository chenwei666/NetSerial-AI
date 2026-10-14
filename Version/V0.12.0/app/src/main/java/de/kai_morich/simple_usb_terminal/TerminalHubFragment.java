package de.kai_morich.simple_usb_terminal;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public final class TerminalHubFragment extends Fragment {
    @Nullable @Override public View onCreateView(@NonNull LayoutInflater inflater,
                                                  @Nullable ViewGroup container,
                                                  @Nullable Bundle state) {
        return inflater.inflate(R.layout.fragment_terminal_hub, container, false);
    }

    @Override public void onViewCreated(@NonNull View view, @Nullable Bundle state) {
        view.findViewById(R.id.terminal_hub_connections).setOnClickListener(v ->
                ((MainActivity) requireActivity()).selectConnections());
        view.findViewById(R.id.terminal_hub_remote).setOnClickListener(v ->
                FeatureNavigator.open(requireActivity(), com.chenwei666.netserial.navigation.FeatureId.REMOTE_CONNECTIONS));
        view.findViewById(R.id.terminal_hub_sessions).setOnClickListener(v ->
                FeatureNavigator.open(requireActivity(), com.chenwei666.netserial.navigation.FeatureId.SESSION_WORKSPACE));
    }
}
