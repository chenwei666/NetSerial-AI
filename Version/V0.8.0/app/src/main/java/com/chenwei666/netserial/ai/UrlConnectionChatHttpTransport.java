package com.chenwei666.netserial.ai;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.util.Objects;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLException;

public final class UrlConnectionChatHttpTransport implements ChatHttpTransport {
    private static final int BUFFER_SIZE = 8_192;

    @Override
    public ChatHttpResponse post(
            URI endpoint,
            byte[] requestBody,
            char[] credential,
            HttpExecutionPolicy policy,
            RequestCancellation cancellation
    ) {
        return post(endpoint, requestBody, credential, CredentialHeaderMode.BEARER,
                policy, cancellation);
    }

    public ChatHttpResponse post(
            URI endpoint,
            byte[] requestBody,
            char[] credential,
            CredentialHeaderMode headerMode,
            HttpExecutionPolicy policy,
            RequestCancellation cancellation
    ) {
        Objects.requireNonNull(endpoint, "endpoint");
        Objects.requireNonNull(requestBody, "requestBody");
        Objects.requireNonNull(credential, "credential");
        Objects.requireNonNull(policy, "policy");
        Objects.requireNonNull(cancellation, "cancellation");
        if (!"https".equalsIgnoreCase(endpoint.getScheme())) {
            throw new IllegalArgumentException("endpoint must use HTTPS");
        }
        Objects.requireNonNull(headerMode, "headerMode");
        if (credential.length == 0 && headerMode != CredentialHeaderMode.NONE) {
            throw new IllegalArgumentException("credential must not be empty");
        }
        if (headerMode != CredentialHeaderMode.NONE) {
            validateCredential(credential);
        }
        if (cancellation.isCancelled()) {
            throw cancelled();
        }
        HttpsURLConnection connection = null;
        try {
            connection = (HttpsURLConnection) endpoint.toURL().openConnection();
            HttpsURLConnection activeConnection = connection;
            cancellation.setCancelAction(activeConnection::disconnect);
            connection.setInstanceFollowRedirects(false);
            connection.setRequestMethod("POST");
            connection.setConnectTimeout(policy.getConnectTimeoutMillis());
            connection.setReadTimeout(policy.getReadTimeoutMillis());
            connection.setDoOutput(true);
            connection.setFixedLengthStreamingMode(requestBody.length);
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
            connection.setRequestProperty("User-Agent", "NetSerial-AI/0.8.0");
            if (headerMode == CredentialHeaderMode.BEARER) {
                connection.setRequestProperty("Authorization", "Bearer " + new String(credential));
            } else if (headerMode == CredentialHeaderMode.ANTHROPIC_X_API_KEY) {
                connection.setRequestProperty("x-api-key", new String(credential));
                connection.setRequestProperty("anthropic-version", "2023-06-01");
            }

            try (OutputStream output = connection.getOutputStream()) {
                output.write(requestBody);
            }
            if (cancellation.isCancelled()) {
                throw cancelled();
            }
            int status = connection.getResponseCode();
            if (status < 100 || status > 599) {
                throw new AiProviderException(
                        AiProviderError.INVALID_RESPONSE,
                        "AI provider returned an invalid HTTP status",
                        0,
                        false
                );
            }
            if (status < 200 || status >= 300) {
                closeQuietly(connection.getErrorStream());
                return new ChatHttpResponse(status, new byte[0]);
            }
            InputStream responseStream = connection.getInputStream();
            byte[] responseBody = responseStream == null
                    ? new byte[0]
                    : readLimited(
                            responseStream,
                            policy.getMaxResponseBytes(),
                            cancellation
                    );
            return new ChatHttpResponse(status, responseBody);
        } catch (AiProviderException exception) {
            throw exception;
        } catch (SocketTimeoutException exception) {
            throw new AiProviderException(
                    AiProviderError.TIMEOUT,
                    "AI provider request timed out",
                    true,
                    exception
            );
        } catch (SSLException exception) {
            throw new AiProviderException(
                    AiProviderError.TLS,
                    "AI provider TLS connection failed",
                    false,
                    exception
            );
        } catch (IOException exception) {
            if (cancellation.isCancelled()) {
                throw cancelled();
            }
            throw new AiProviderException(
                    AiProviderError.NETWORK,
                    "AI provider network request failed",
                    true,
                    exception
            );
        } finally {
            cancellation.clearCancelAction();
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    static byte[] readLimited(
            InputStream input,
            int maximumBytes,
            RequestCancellation cancellation
    ) throws IOException {
        try (InputStream source = input;
             ByteArrayOutputStream output = new ByteArrayOutputStream(
                     Math.min(BUFFER_SIZE, maximumBytes)
             )) {
            byte[] buffer = new byte[BUFFER_SIZE];
            int total = 0;
            int read;
            while ((read = source.read(buffer)) != -1) {
                if (cancellation.isCancelled()) {
                    throw cancelled();
                }
                total += read;
                if (total > maximumBytes) {
                    throw new AiProviderException(
                            AiProviderError.RESPONSE_TOO_LARGE,
                            "AI provider response exceeded the size limit",
                            0,
                            false
                    );
                }
                output.write(buffer, 0, read);
            }
            return output.toByteArray();
        }
    }

    static void validateCredential(char[] credential) {
        for (char character : credential) {
            if (character < 0x21 || character > 0x7E) {
                throw new IllegalArgumentException(
                        "credential must contain visible ASCII characters only"
                );
            }
        }
    }

    private static void closeQuietly(InputStream input) {
        if (input == null) {
            return;
        }
        try {
            input.close();
        } catch (IOException ignored) {
            // The connection is disconnected immediately afterward.
        }
    }

    private static AiProviderException cancelled() {
        return new AiProviderException(
                AiProviderError.CANCELLED,
                "AI provider request was cancelled",
                0,
                false
        );
    }
}
