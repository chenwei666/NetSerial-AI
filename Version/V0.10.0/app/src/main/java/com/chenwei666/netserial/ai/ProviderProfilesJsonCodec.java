package com.chenwei666.netserial.ai;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

import java.util.ArrayList;
import java.util.List;

public final class ProviderProfilesJsonCodec {
    private static final int SCHEMA_VERSION = 1;
    private static final int MAX_DOCUMENT_CHARACTERS = 65_536;

    private final Gson gson = new GsonBuilder().disableHtmlEscaping().create();

    public String encode(ProviderProfilesState state) {
        JsonObject root = new JsonObject();
        root.addProperty("version", SCHEMA_VERSION);
        if (state.getActiveCredentialAlias() != null) {
            root.addProperty("activeCredentialAlias", state.getActiveCredentialAlias());
        }
        JsonArray profiles = new JsonArray();
        for (ProviderProfile profile : state.getProfiles()) {
            JsonObject encoded = new JsonObject();
            encoded.addProperty("providerId", profile.getProviderId());
            encoded.addProperty("endpoint", profile.getEndpoint().toString());
            encoded.addProperty("model", profile.getModel());
            encoded.addProperty("credentialAlias", profile.getCredentialAlias());
            profiles.add(encoded);
        }
        root.add("profiles", profiles);
        return gson.toJson(root);
    }

    public ProviderProfilesState decode(String document) {
        if (document == null || document.trim().isEmpty()) {
            return ProviderProfilesState.empty();
        }
        if (document.length() > MAX_DOCUMENT_CHARACTERS) {
            throw invalidDocument(null);
        }
        try {
            JsonObject root = JsonParser.parseString(document).getAsJsonObject();
            if (requiredInt(root, "version") != SCHEMA_VERSION) {
                throw invalidDocument(null);
            }
            JsonArray encodedProfiles = requiredArray(root, "profiles");
            if (encodedProfiles.size() > ProviderProfilesState.MAX_PROFILES) {
                throw invalidDocument(null);
            }
            List<ProviderProfile> profiles = new ArrayList<>();
            for (JsonElement element : encodedProfiles) {
                JsonObject encoded = element.getAsJsonObject();
                profiles.add(ProviderProfile.remote(
                        requiredString(encoded, "providerId"),
                        requiredString(encoded, "endpoint"),
                        requiredString(encoded, "model"),
                        requiredString(encoded, "credentialAlias")
                ));
            }
            String activeAlias = optionalString(root, "activeCredentialAlias");
            return new ProviderProfilesState(profiles, activeAlias);
        } catch (ProviderProfileStoreException exception) {
            throw exception;
        } catch (JsonParseException | IllegalStateException | IllegalArgumentException exception) {
            throw invalidDocument(exception);
        }
    }

    private static int requiredInt(JsonObject object, String name) {
        JsonElement value = object.get(name);
        if (value == null || !value.isJsonPrimitive() || !value.getAsJsonPrimitive().isNumber()) {
            throw invalidDocument(null);
        }
        return value.getAsInt();
    }

    private static JsonArray requiredArray(JsonObject object, String name) {
        JsonElement value = object.get(name);
        if (value == null || !value.isJsonArray()) {
            throw invalidDocument(null);
        }
        return value.getAsJsonArray();
    }

    private static String requiredString(JsonObject object, String name) {
        String value = optionalString(object, name);
        if (value == null) {
            throw invalidDocument(null);
        }
        return value;
    }

    private static String optionalString(JsonObject object, String name) {
        JsonElement value = object.get(name);
        if (value == null || value.isJsonNull()) {
            return null;
        }
        if (!value.isJsonPrimitive() || !value.getAsJsonPrimitive().isString()) {
            throw invalidDocument(null);
        }
        return value.getAsString();
    }

    private static ProviderProfileStoreException invalidDocument(Throwable cause) {
        return cause == null
                ? new ProviderProfileStoreException("Stored AI provider profiles are invalid")
                : new ProviderProfileStoreException("Stored AI provider profiles are invalid", cause);
    }
}
