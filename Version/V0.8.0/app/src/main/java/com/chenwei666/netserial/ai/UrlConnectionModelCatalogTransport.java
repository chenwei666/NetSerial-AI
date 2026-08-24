package com.chenwei666.netserial.ai;

import java.io.IOException;
import java.io.InputStream;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.util.Objects;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLException;

final class UrlConnectionModelCatalogTransport implements ModelCatalogHttpTransport {
    @Override public ChatHttpResponse get(
            URI endpoint,
            char[] credential,
            CredentialHeaderMode headerMode,
            HttpExecutionPolicy policy,
            RequestCancellation cancellation
    ) {
        Objects.requireNonNull(endpoint, "endpoint");
        Objects.requireNonNull(credential, "credential");
        Objects.requireNonNull(headerMode, "headerMode");
        Objects.requireNonNull(policy, "policy");
        Objects.requireNonNull(cancellation, "cancellation");
        if (!"https".equalsIgnoreCase(endpoint.getScheme())) {
            throw new IllegalArgumentException("model catalog endpoint must use HTTPS");
        }
        if (headerMode != CredentialHeaderMode.NONE) {
            if (credential.length == 0) throw new IllegalArgumentException("credential must not be empty");
            UrlConnectionChatHttpTransport.validateCredential(credential);
        }
        if (cancellation.isCancelled()) throw cancelled();

        HttpsURLConnection connection = null;
        try {
            connection = (HttpsURLConnection) endpoint.toURL().openConnection();
            HttpsURLConnection active = connection;
            cancellation.setCancelAction(active::disconnect);
            connection.setInstanceFollowRedirects(false);
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(policy.getConnectTimeoutMillis());
            connection.setReadTimeout(policy.getReadTimeoutMillis());
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("User-Agent", "NetSerial-AI/0.8.0");
            if (headerMode == CredentialHeaderMode.BEARER) {
                connection.setRequestProperty("Authorization", "Bearer " + new String(credential));
            } else if (headerMode == CredentialHeaderMode.ANTHROPIC_X_API_KEY) {
                connection.setRequestProperty("x-api-key", new String(credential));
                connection.setRequestProperty("anthropic-version", "2023-06-01");
            }
            int status = connection.getResponseCode();
            if (status < 100 || status > 599) {
                throw new AiProviderException(AiProviderError.INVALID_RESPONSE,
                        "AI provider returned an invalid HTTP status", 0, false);
            }
            if (status < 200 || status >= 300) {
                closeQuietly(connection.getErrorStream());
                return new ChatHttpResponse(status, new byte[0]);
            }
            InputStream input = connection.getInputStream();
            byte[] body = input == null ? new byte[0]
                    : UrlConnectionChatHttpTransport.readLimited(
                            input, policy.getMaxResponseBytes(), cancellation);
            return new ChatHttpResponse(status, body);
        } catch (AiProviderException exception) {
            throw exception;
        } catch (SocketTimeoutException exception) {
            throw new AiProviderException(AiProviderError.TIMEOUT,
                    "Model catalog request timed out", true, exception);
        } catch (SSLException exception) {
            throw new AiProviderException(AiProviderError.TLS,
                    "Model catalog TLS connection failed", false, exception);
        } catch (IOException exception) {
            if (cancellation.isCancelled()) throw cancelled();
            throw new AiProviderException(AiProviderError.NETWORK,
                    "Model catalog network request failed", true, exception);
        } finally {
            cancellation.clearCancelAction();
            if (connection != null) connection.disconnect();
        }
    }

    private static void closeQuietly(InputStream input) {
        if (input == null) return;
        try { input.close(); } catch (IOException ignored) { }
    }

    private static AiProviderException cancelled() {
        return new AiProviderException(AiProviderError.CANCELLED,
                "Model catalog request was cancelled", 0, false);
    }
}
