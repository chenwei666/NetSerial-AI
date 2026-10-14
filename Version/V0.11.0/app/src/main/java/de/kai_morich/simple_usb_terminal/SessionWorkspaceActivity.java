package de.kai_morich.simple_usb_terminal;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import com.chenwei666.netserial.remote.RemoteProtocol;
import com.chenwei666.netserial.session.RemoteSessionProfile;
import com.chenwei666.netserial.session.SessionProfileStore;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class SessionWorkspaceActivity extends ThemedActivity {
    private final List<RemoteSessionProfile> profiles = new ArrayList<>();
    private ArrayAdapter<RemoteSessionProfile> adapter;
    private EditText label;
    private EditText host;
    private EditText port;
    private EditText username;
    private Spinner protocol;
    private SessionProfileStore store;

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_session_workspace);
        setTitle(R.string.sessions_title);
        store = new SessionProfileStore(this);
        label = findViewById(R.id.session_label);
        host = findViewById(R.id.session_host);
        port = findViewById(R.id.session_port);
        username = findViewById(R.id.session_username);
        protocol = findViewById(R.id.session_protocol);
        ArrayAdapter<String> protocols = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item,
                new String[]{"SSH", "Telnet"});
        protocols.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        protocol.setAdapter(protocols);
        profiles.addAll(store.load());
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, profiles);
        ListView list = findViewById(R.id.session_list);
        list.setAdapter(adapter);
        list.setOnItemClickListener((parent, view, position, id) -> open(profiles.get(position)));
        list.setOnItemLongClickListener((parent, view, position, id) -> {
            RemoteSessionProfile selected = profiles.get(position);
            new android.app.AlertDialog.Builder(this).setTitle(R.string.sessions_delete_title)
                    .setMessage(getString(R.string.sessions_delete_message, selected.getLabel()))
                    .setNegativeButton(android.R.string.cancel, null)
                    .setPositiveButton(R.string.sessions_delete, (dialog, which) -> {
                        profiles.remove(selected);
                        persist();
                    }).show();
            return true;
        });
        findViewById(R.id.session_add).setOnClickListener(view -> add());
    }

    private void add() {
        try {
            RemoteProtocol selected = protocol.getSelectedItemPosition() == 0
                    ? RemoteProtocol.SSH : RemoteProtocol.TELNET;
            String portText = port.getText().toString().trim();
            int value = portText.isEmpty() ? selected.getDefaultPort() : Integer.parseInt(portText);
            RemoteSessionProfile profile = new RemoteSessionProfile(UUID.randomUUID().toString(),
                    label.getText().toString(), selected, host.getText().toString(), value,
                    username.getText().toString());
            profiles.add(profile);
            persist();
            label.setText(""); host.setText(""); port.setText(""); username.setText("");
        } catch (RuntimeException exception) {
            Toast.makeText(this, R.string.sessions_invalid, Toast.LENGTH_LONG).show();
        }
    }

    private void persist() {
        try {
            store.save(profiles);
            adapter.notifyDataSetChanged();
        } catch (RuntimeException exception) {
            Toast.makeText(this, R.string.sessions_save_failed, Toast.LENGTH_LONG).show();
        }
    }

    private void open(RemoteSessionProfile profile) {
        Intent intent = new Intent(this, RemoteTerminalActivity.class)
                .putExtra(RemoteTerminalActivity.EXTRA_PROTOCOL, profile.getProtocol().name())
                .putExtra(RemoteTerminalActivity.EXTRA_HOST, profile.getHost())
                .putExtra(RemoteTerminalActivity.EXTRA_PORT, profile.getPort())
                .putExtra(RemoteTerminalActivity.EXTRA_USERNAME, profile.getUsername())
                .putExtra(RemoteTerminalActivity.EXTRA_SESSION_LABEL, profile.getLabel())
                .addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        startActivity(intent);
    }
}
