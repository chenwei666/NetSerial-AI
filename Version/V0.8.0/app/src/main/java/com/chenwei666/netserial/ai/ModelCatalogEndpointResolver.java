package com.chenwei666.netserial.ai;

import java.net.URI;
import java.net.URISyntaxException;

public final class ModelCatalogEndpointResolver {
    private static final String MODELS_PATH = "/models";
    private static final String OLLAMA_TAGS_PATH = "/api/tags";

    private ModelCatalogEndpointResolver() { }

    public static URI resolve(ProviderProfile profile) {
        URI base = profile.getEndpoint();
        String path = base.getPath();
        if ("ollama".equals(profile.getProviderId())) {
            path = OLLAMA_TAGS_PATH;
        } else if ("qwen".equals(profile.getProviderId())) {
            path = "/api/v1/models";
        } else if (path == null || path.isEmpty() || "/".equals(path)) {
            path = MODELS_PATH;
        } else if (!path.endsWith(MODELS_PATH)) {
            path = path.endsWith("/") ? path + "models" : path + MODELS_PATH;
        }
        try {
            return new URI(base.getScheme(), null, base.getHost(), base.getPort(), path, null, null);
        } catch (URISyntaxException exception) {
            throw new IllegalArgumentException("Unable to resolve model catalog endpoint", exception);
        }
    }
}
