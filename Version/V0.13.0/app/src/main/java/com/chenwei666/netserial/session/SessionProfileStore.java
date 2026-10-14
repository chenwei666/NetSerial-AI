package com.chenwei666.netserial.session;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class SessionProfileStore {
    private static final String NAME = "remote_session_profiles_v1";
    private static final int MAX_PROFILES = 30;
    private static final Type LIST_TYPE = new TypeToken<ArrayList<RemoteSessionProfile>>() { }.getType();
    private final SharedPreferences preferences;
    private final Gson gson = new Gson();

    public SessionProfileStore(Context context) {
        preferences = context.getApplicationContext().getSharedPreferences(NAME, Context.MODE_PRIVATE);
    }

    public List<RemoteSessionProfile> load() {
        try {
            List<RemoteSessionProfile> profiles = gson.fromJson(preferences.getString("profiles", "[]"), LIST_TYPE);
            if (profiles == null || profiles.size() > MAX_PROFILES) return Collections.emptyList();
            List<RemoteSessionProfile> validated = new ArrayList<>();
            for (RemoteSessionProfile profile : profiles) validated.add(new RemoteSessionProfile(profile.getId(),
                    profile.getLabel(), profile.getProtocol(), profile.getHost(), profile.getPort(), profile.getUsername()));
            return validated;
        } catch (RuntimeException exception) {
            return Collections.emptyList();
        }
    }

    public void save(List<RemoteSessionProfile> profiles) {
        if (profiles == null || profiles.size() > MAX_PROFILES) throw new IllegalArgumentException("profiles");
        if (!preferences.edit().putString("profiles", gson.toJson(profiles)).commit()) {
            throw new IllegalStateException("Unable to save session profiles");
        }
    }
}
