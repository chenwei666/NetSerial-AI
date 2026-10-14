package com.chenwei666.netserial.ai;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/** Stores model identifiers only. API credentials are never written to this cache. */
public final class AiModelCacheStore {
    private static final String NAME = "ai_model_catalog_v1";
    private static final char[] HEX = "0123456789abcdef".toCharArray();
    private static final Type LIST_TYPE = new TypeToken<List<String>>() { }.getType();
    private final SharedPreferences preferences;
    private final Gson gson = new Gson();

    public AiModelCacheStore(Context context) {
        preferences = Objects.requireNonNull(context, "context").getApplicationContext()
                .getSharedPreferences(NAME, Context.MODE_PRIVATE);
    }

    public CachedAiModels load(ProviderProfile profile) {
        String key = key(profile);
        try {
            List<String> models = gson.fromJson(preferences.getString(key + ".models", "[]"), LIST_TYPE);
            if (models == null) models = Collections.emptyList();
            return new CachedAiModels(models, preferences.getLong(key + ".fetched_at", 0L));
        } catch (RuntimeException exception) {
            return new CachedAiModels(Collections.emptyList(), 0L);
        }
    }

    public void save(ProviderProfile profile, List<String> models, long fetchedAtMillis) {
        List<String> safeCopy = new ArrayList<>(Objects.requireNonNull(models, "models"));
        boolean saved = preferences.edit()
                .putString(key(profile) + ".models", gson.toJson(safeCopy))
                .putLong(key(profile) + ".fetched_at", fetchedAtMillis)
                .commit();
        if (!saved) throw new IllegalStateException("Unable to cache AI models");
    }

    public void clearAll() {
        if (!preferences.edit().clear().commit()) {
            throw new IllegalStateException("Unable to clear AI model cache");
        }
    }

    static String key(ProviderProfile profile) {
        ProviderProfile value = Objects.requireNonNull(profile, "profile");
        String scope = value.getProviderId() + "\n" + value.getEndpoint()
                + "\n" + value.getCredentialAlias();
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(scope.getBytes(StandardCharsets.UTF_8));
            StringBuilder result = new StringBuilder("profile.");
            for (byte item : digest) {
                result.append(HEX[(item >>> 4) & 0x0f]).append(HEX[item & 0x0f]);
            }
            return result.toString();
        } catch (NoSuchAlgorithmException impossible) {
            throw new IllegalStateException("SHA-256 is unavailable", impossible);
        }
    }
}
