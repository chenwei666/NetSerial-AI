package com.chenwei666.netserial.update;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public final class GitHubReleaseParser {
    public ReleaseInfo parse(String json) {
        if (json == null || json.length() > 1_000_000) throw new IllegalArgumentException("release response size");
        JsonObject root = new JsonParser().parse(json).getAsJsonObject();
        String apk = "";
        JsonArray assets = root.has("assets") && root.get("assets").isJsonArray()
                ? root.getAsJsonArray("assets") : new JsonArray();
        for (JsonElement element : assets) {
            JsonObject asset = element.getAsJsonObject();
            String name = text(asset, "name");
            String url = text(asset, "browser_download_url");
            if (name.toLowerCase(java.util.Locale.ROOT).endsWith(".apk")
                    && url.startsWith("https://github.com/")) {
                apk = url;
                break;
            }
        }
        return new ReleaseInfo(text(root, "tag_name"), text(root, "name"),
                text(root, "html_url"), apk,
                root.has("prerelease") && root.get("prerelease").getAsBoolean());
    }

    private static String text(JsonObject value, String name) {
        return value.has(name) && !value.get(name).isJsonNull() ? value.get(name).getAsString() : "";
    }
}
