package com.chenwei666.netserial.update;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executor;

public final class GitHubUpdateChecker {
    public interface Callback {
        void onSuccess(ReleaseInfo release);
        void onFailure(String safeReason);
    }

    private static final String ENDPOINT =
            "https://api.github.com/repos/chenwei666/NetSerial-AI/releases/latest";
    private final Executor executor;

    public GitHubUpdateChecker(Executor executor) { this.executor = executor; }

    public void check(Callback callback) {
        executor.execute(() -> {
            HttpURLConnection connection = null;
            try {
                connection = (HttpURLConnection) new URL(ENDPOINT).openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(10_000);
                connection.setReadTimeout(15_000);
                connection.setInstanceFollowRedirects(false);
                connection.setRequestProperty("Accept", "application/vnd.github+json");
                connection.setRequestProperty("X-GitHub-Api-Version", "2022-11-28");
                connection.setRequestProperty("User-Agent", "NetSerial-AI-Android");
                int status = connection.getResponseCode();
                if (status != 200) throw new IllegalStateException("HTTP " + status);
                try (InputStream input = connection.getInputStream()) {
                    callback.onSuccess(new GitHubReleaseParser().parse(readBounded(input, 1_000_000)));
                }
            } catch (Exception exception) {
                callback.onFailure(exception instanceof java.net.SocketTimeoutException
                        ? "timeout" : "network");
            } finally {
                if (connection != null) connection.disconnect();
            }
        });
    }

    private static String readBounded(InputStream input, int limit) throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] buffer = new byte[8192];
        int count;
        while ((count = input.read(buffer)) != -1) {
            if (output.size() + count > limit) throw new IllegalArgumentException("response too large");
            output.write(buffer, 0, count);
        }
        return new String(output.toByteArray(), StandardCharsets.UTF_8);
    }
}
