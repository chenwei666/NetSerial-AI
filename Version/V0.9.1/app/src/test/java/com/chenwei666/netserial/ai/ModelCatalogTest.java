package com.chenwei666.netserial.ai;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

public class ModelCatalogTest {
    @Test public void resolvesCompatibleAndOllamaCatalogEndpoints() {
        ProviderProfile compatible = ProviderProfile.remote(
                "zhipu", "https://open.bigmodel.cn/api/paas/v4", "glm", "zhipu-key");
        ProviderProfile ollama = ProviderProfile.remote(
                "ollama", "https://ollama.example.net/v1", "llama", "ollama-local");

        assertEquals("https://open.bigmodel.cn/api/paas/v4/models",
                ModelCatalogEndpointResolver.resolve(compatible).toString());
        assertEquals("https://ollama.example.net/api/tags",
                ModelCatalogEndpointResolver.resolve(ollama).toString());
    }

    @Test public void parsesSortsAndDeduplicatesOpenAiModels() {
        String json = "{\"data\":[{\"id\":\"glm-5\"},{\"id\":\"GLM-4\"},{\"id\":\"glm-5\"}]}";
        List<String> models = new ModelCatalogJsonCodec().decode(
                json.getBytes(StandardCharsets.UTF_8), ModelCatalogFormat.OPENAI);

        assertEquals(Arrays.asList("GLM-4", "glm-5"), models);
    }

    @Test public void parsesOllamaTags() {
        String json = "{\"models\":[{\"name\":\"qwen3:8b\"},{\"name\":\"llama3.2:latest\"}]}";
        List<String> models = new ModelCatalogJsonCodec().decode(
                json.getBytes(StandardCharsets.UTF_8), ModelCatalogFormat.OLLAMA);

        assertEquals(Arrays.asList("llama3.2:latest", "qwen3:8b"), models);
    }

    @Test public void resolvesAndParsesQwenModelCatalog() {
        ProviderProfile profile = ProviderProfile.remote(
                "qwen", "https://dashscope.aliyuncs.com/compatible-mode/v1",
                "qwen-plus", "qwen-key");
        String json = "{\"output\":{\"models\":[{\"model\":\"qwen-plus\"},"
                + "{\"model\":\"qwen-max\"}]}}";

        assertEquals("https://dashscope.aliyuncs.com/api/v1/models",
                ModelCatalogEndpointResolver.resolve(profile).toString());
        assertEquals(Arrays.asList("qwen-max", "qwen-plus"),
                new ModelCatalogJsonCodec().decode(
                        json.getBytes(StandardCharsets.UTF_8), ModelCatalogFormat.QWEN));
    }

    @Test public void invalidatedModelSyncCannotUpdateUi() {
        ModelSyncGuard guard = new ModelSyncGuard();
        long firstRequest = guard.begin();
        guard.invalidate();
        long secondRequest = guard.begin();

        assertTrue(!guard.isCurrent(firstRequest));
        assertTrue(guard.isCurrent(secondRequest));
    }

    @Test public void detectsCredentialDestinationChangesBeforeModelRefresh() {
        ProviderProfile existing = ProviderProfile.remote(
                "openai", "https://api.openai.com/v1", "model-a", "profile-a");
        ProviderProfile changedHost = ProviderProfile.remote(
                "openai", "https://untrusted.example/v1", "model-a", "profile-a");
        ProviderProfile modelOnly = ProviderProfile.remote(
                "openai", "https://api.openai.com/v1", "model-b", "profile-a");

        assertTrue(CredentialDestinationPolicy.hasChanged(existing, changedHost));
        assertTrue(!CredentialDestinationPolicy.hasChanged(existing, modelOnly));
    }

    @Test public void serviceUsesAnthropicHeaderAndParsesCatalog() {
        RecordingTransport transport = new RecordingTransport();
        AiModelCatalogService service = new AiModelCatalogService(
                transport, new ModelCatalogJsonCodec());
        ProviderProfile profile = ProviderProfile.remote(
                "anthropic", "https://api.anthropic.com/v1", "claude", "anthropic-key");

        List<String> models = service.fetch(profile, "secret".toCharArray(),
                new RequestCancellation());

        assertEquals(Arrays.asList("claude-a", "claude-b"), models);
        assertEquals(CredentialHeaderMode.ANTHROPIC_X_API_KEY, transport.headerMode);
        assertEquals(URI.create("https://api.anthropic.com/v1/models"), transport.endpoint);
    }

    @Test(expected = AiProviderException.class)
    public void rejectsCatalogWithoutModels() {
        new ModelCatalogJsonCodec().decode("{\"data\":[]}".getBytes(StandardCharsets.UTF_8),
                ModelCatalogFormat.OPENAI);
    }

    @Test public void isolatesModelCacheByCredentialAlias() {
        ProviderProfile first = ProviderProfile.remote(
                "openai-compatible", "https://gateway.example/v1", "model-a", "account-a");
        ProviderProfile second = ProviderProfile.remote(
                "openai-compatible", "https://gateway.example/v1", "model-b", "account-b");

        assertNotEquals(AiModelCacheStore.key(first), AiModelCacheStore.key(second));
    }

    private static final class RecordingTransport implements ModelCatalogHttpTransport {
        private URI endpoint;
        private CredentialHeaderMode headerMode;

        @Override public ChatHttpResponse get(URI endpoint, char[] credential,
                                               CredentialHeaderMode headerMode,
                                               HttpExecutionPolicy policy,
                                               RequestCancellation cancellation) {
            this.endpoint = endpoint;
            this.headerMode = headerMode;
            assertTrue(credential.length > 0);
            return new ChatHttpResponse(200,
                    "{\"data\":[{\"id\":\"claude-b\"},{\"id\":\"claude-a\"}]}"
                            .getBytes(StandardCharsets.UTF_8));
        }
    }
}
