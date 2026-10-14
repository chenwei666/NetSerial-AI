package com.chenwei666.netserial.ai;

import java.net.URI;
import java.net.URISyntaxException;

final class ChatEndpointResolver {
    private static final String CHAT_COMPLETIONS_PATH = "/chat/completions";

    private ChatEndpointResolver() {
    }

    static URI resolve(ProviderProfile profile) {
        URI base = profile.getEndpoint();
        String path = base.getPath();
        if (path == null || path.isEmpty() || "/".equals(path)) {
            path = CHAT_COMPLETIONS_PATH;
        } else if (!path.endsWith(CHAT_COMPLETIONS_PATH)) {
            path = path.endsWith("/")
                    ? path + CHAT_COMPLETIONS_PATH.substring(1)
                    : path + CHAT_COMPLETIONS_PATH;
        }
        try {
            return new URI(
                    base.getScheme(),
                    null,
                    base.getHost(),
                    base.getPort(),
                    path,
                    null,
                    null
            );
        } catch (URISyntaxException exception) {
            throw new IllegalArgumentException("Unable to resolve chat endpoint", exception);
        }
    }
}
