package com.chenwei666.netserial.ai;

import static org.junit.Assert.assertEquals;
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
