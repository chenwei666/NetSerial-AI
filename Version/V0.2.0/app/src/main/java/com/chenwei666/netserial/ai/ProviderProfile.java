package com.chenwei666.netserial.ai;

import java.net.URI;
import java.util.Objects;

public final class ProviderProfile {
    private static final int MAX_PROVIDER_ID_LENGTH = 64;
    private static final int MAX_ENDPOINT_LENGTH = 2_048;
    private static final int MAX_MODEL_LENGTH = 256;

    private final String providerId;
    private final URI endpoint;
    private final String model;
    private final String credentialAlias;

    private ProviderProfile(String providerId, URI endpoint, String model,
                            String credentialAlias) {
        this.providerId = requireText(providerId, "providerId");
        this.endpoint = Objects.requireNonNull(endpoint, "endpoint");
        this.model = requireText(model, "model");
        this.credentialAlias = requireText(credentialAlias, "credentialAlias");
    }

    public static ProviderProfile remote(String providerId, String endpoint, String model,
                                         String credentialAlias) {
        URI parsedEndpoint = URI.create(requireText(endpoint, "endpoint", MAX_ENDPOINT_LENGTH));
        if (!parsedEndpoint.isAbsolute()
                || !"https".equalsIgnoreCase(parsedEndpoint.getScheme())
                || parsedEndpoint.getHost() == null
                || parsedEndpoint.getUserInfo() != null
                || parsedEndpoint.getQuery() != null
                || parsedEndpoint.getFragment() != null) {
            throw new IllegalArgumentException("remote AI endpoint must be an absolute HTTPS URL");
        }
        return new ProviderProfile(
                requireText(providerId, "providerId", MAX_PROVIDER_ID_LENGTH),
                parsedEndpoint,
                requireText(model, "model", MAX_MODEL_LENGTH),
                CredentialAliases.normalize(credentialAlias)
        );
    }

    public String getProviderId() {
        return providerId;
    }

    public URI getEndpoint() {
        return endpoint;
    }

    public String getModel() {
        return model;
    }

    public String getCredentialAlias() {
        return credentialAlias;
    }

    private static String requireText(String value, String name) {
        return requireText(value, name, Integer.MAX_VALUE);
    }

    private static String requireText(String value, String name, int maximumLength) {
        String normalized = Objects.requireNonNull(value, name).trim();
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException(name + " must not be empty");
        }
        if (normalized.length() > maximumLength) {
            throw new IllegalArgumentException(name + " is too long");
        }
        return normalized;
    }
}
