package com.chenwei666.netserial.ai;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.chenwei666.netserial.device.CliMode;
import com.chenwei666.netserial.device.Vendor;
import com.chenwei666.netserial.safety.RiskLevel;

import org.junit.Test;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class OpenAiCompatibleProviderTest {

    @Test
    public void sendsSanitizedContextAndParsesCommandPlan() throws Exception {
        RecordingTransport transport = new RecordingTransport(new ChatHttpResponse(
                200,
                ("{\"choices\":[{\"message\":{\"content\":\""
                        + "{\\\"steps\\\":[{\\\"phase\\\":\\\"PRECHECK\\\",\\\"command\\\":\\\"display version\\\","
                        + "\\\"risk\\\":\\\"R1_READ_ONLY\\\"}]}\"}}]}")
                        .getBytes(StandardCharsets.UTF_8)
        ));
        ProviderProfile profile = ProviderProfile.remote(
                "compatible-main",
                "https://ai.example.com/v1",
                "network-ops-model",
                "credential-compatible-main"
        );
        ProviderCredentialService credentials = credentialService();
        credentials.save(profile, "example-value".toCharArray());
        OpenAiCompatibleProvider provider = new OpenAiCompatibleProvider(
                profile,
                credentials,
                transport,
                HttpExecutionPolicy.defaults()
        );

        AiDraftPlan plan = provider.propose(new AiRequest(
                "检查设备版本",
                Vendor.H3C_COMWARE,
                CliMode.USER_VIEW,
                "display current-configuration\npassword cipher example-sensitive-value"
        ));

        assertEquals(URI.create("https://ai.example.com/v1/chat/completions"),
                transport.endpoint);
        assertTrue(transport.requestJson.contains("network-ops-model"));
        assertTrue(transport.requestJson.contains("H3C_COMWARE"));
        assertTrue(transport.requestJson.contains("[REDACTED]"));
        assertFalse(transport.requestJson.contains("example-sensitive-value"));
        assertArrayEquals("example-value".toCharArray(), transport.credential);
        assertEquals("display version", plan.getSteps().get(0).getCommand());
        assertEquals(RiskLevel.R1_READ_ONLY, plan.getSteps().get(0).getProposedRisk());
    }

    @Test
    public void authenticationFailureDoesNotExposeResponseBody() {
        RecordingTransport transport = new RecordingTransport(new ChatHttpResponse(
                401,
                "provider diagnostic example-sensitive-value".getBytes(StandardCharsets.UTF_8)
        ));
        ProviderProfile profile = ProviderProfile.remote(
                "compatible-main",
                "https://ai.example.com/v1",
                "network-ops-model",
                "credential-compatible-main"
        );
        ProviderCredentialService credentials = credentialService();
        credentials.save(profile, "example-value".toCharArray());
        OpenAiCompatibleProvider provider = new OpenAiCompatibleProvider(
                profile,
                credentials,
                transport,
                HttpExecutionPolicy.defaults()
        );

        try {
            provider.propose(request());
            fail("Expected AiProviderException");
        } catch (AiProviderException expected) {
            assertEquals(AiProviderError.AUTHENTICATION, expected.getError());
            assertEquals(401, expected.getHttpStatus());
            assertFalse(expected.getMessage().contains("example-sensitive-value"));
        } catch (Exception unexpected) {
            fail("Unexpected exception: " + unexpected.getClass().getSimpleName());
        }
    }

    @Test
    public void malformedProviderResponseFailsClosed() {
        RecordingTransport transport = new RecordingTransport(new ChatHttpResponse(
                200,
                "{\"choices\":[]}".getBytes(StandardCharsets.UTF_8)
        ));
        ProviderProfile profile = ProviderProfile.remote(
                "compatible-main",
                "https://ai.example.com/v1",
                "network-ops-model",
                "credential-compatible-main"
        );
        ProviderCredentialService credentials = credentialService();
        credentials.save(profile, "example-value".toCharArray());
        OpenAiCompatibleProvider provider = new OpenAiCompatibleProvider(
                profile,
                credentials,
                transport,
                HttpExecutionPolicy.defaults()
        );

        try {
            provider.propose(request());
            fail("Expected AiProviderException");
        } catch (AiProviderException expected) {
            assertEquals(AiProviderError.INVALID_RESPONSE, expected.getError());
        } catch (Exception unexpected) {
            fail("Unexpected exception: " + unexpected.getClass().getSimpleName());
        }
    }

    @Test
    public void cancellationBeforeRequestDoesNotCallTransport() {
        RecordingTransport transport = new RecordingTransport(new ChatHttpResponse(
                200,
                "{}".getBytes(StandardCharsets.UTF_8)
        ));
        ProviderProfile profile = ProviderProfile.remote(
                "compatible-main",
                "https://ai.example.com/v1",
                "network-ops-model",
                "credential-compatible-main"
        );
        OpenAiCompatibleProvider provider = new OpenAiCompatibleProvider(
                profile,
                credentialService(),
                transport,
                HttpExecutionPolicy.defaults()
        );
        RequestCancellation cancellation = new RequestCancellation();
        cancellation.cancel();

        try {
            provider.propose(request(), cancellation);
            fail("Expected AiProviderException");
        } catch (AiProviderException expected) {
            assertEquals(AiProviderError.CANCELLED, expected.getError());
            assertEquals(0, transport.calls);
        }
    }

    @Test
    public void fullChatEndpointIsNotAppendedTwice() throws Exception {
        RecordingTransport transport = new RecordingTransport(new ChatHttpResponse(
                200,
                ("{\"choices\":[{\"message\":{\"content\":\""
                        + "{\\\"steps\\\":[{\\\"phase\\\":\\\"PRECHECK\\\",\\\"command\\\":\\\"display version\\\","
                        + "\\\"risk\\\":\\\"R1_READ_ONLY\\\"}]}\"}}]}")
                        .getBytes(StandardCharsets.UTF_8)
        ));
        ProviderProfile profile = ProviderProfile.remote(
                "compatible-main",
                "https://ai.example.com/v1/chat/completions",
                "network-ops-model",
                "credential-compatible-main"
        );
        ProviderCredentialService credentials = credentialService();
        credentials.save(profile, "example-value".toCharArray());

        new OpenAiCompatibleProvider(
                profile,
                credentials,
                transport,
                HttpExecutionPolicy.defaults()
        ).propose(request());

        assertEquals(
                URI.create("https://ai.example.com/v1/chat/completions"),
                transport.endpoint
        );
    }

    private static ProviderCredentialService credentialService() {
        return new ProviderCredentialService(new SecureCredentialVault(
                new TestCredentialComponents.XorSecretCipher(),
                new TestCredentialComponents.InMemoryRecordStore()
        ));
    }

    private static AiRequest request() {
        return new AiRequest(
                "检查版本",
                Vendor.H3C_COMWARE,
                CliMode.USER_VIEW,
                "<H3C>"
        );
    }

    private static void assertArrayEquals(char[] expected, char[] actual) {
        assertTrue(Arrays.equals(expected, actual));
    }

    private static final class RecordingTransport implements ChatHttpTransport {
        private final ChatHttpResponse response;
        private URI endpoint;
        private String requestJson;
        private char[] credential;
        private int calls;

        private RecordingTransport(ChatHttpResponse response) {
            this.response = response;
        }

        @Override
        public ChatHttpResponse post(
                URI endpoint,
                byte[] requestBody,
                char[] credential,
                HttpExecutionPolicy policy,
                RequestCancellation cancellation
        ) {
            calls++;
            this.endpoint = endpoint;
            this.requestJson = new String(requestBody, StandardCharsets.UTF_8);
            this.credential = Arrays.copyOf(credential, credential.length);
            return response;
        }
    }
}
