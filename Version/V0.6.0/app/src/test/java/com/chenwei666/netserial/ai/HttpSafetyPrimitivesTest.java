package com.chenwei666.netserial.ai;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.util.concurrent.atomic.AtomicInteger;

public class HttpSafetyPrimitivesTest {

    @Test(expected = IllegalArgumentException.class)
    public void policyRejectsExcessiveResponseLimit() {
        new HttpExecutionPolicy(10_000, 60_000, 1_048_577);
    }

    @Test
    public void oversizedResponseIsRejected() {
        byte[] response = new byte[2_048];

        try {
            UrlConnectionChatHttpTransport.readLimited(
                    new ByteArrayInputStream(response),
                    1_024,
                    new RequestCancellation()
            );
            fail("Expected AiProviderException");
        } catch (AiProviderException expected) {
            assertEquals(AiProviderError.RESPONSE_TOO_LARGE, expected.getError());
        } catch (Exception unexpected) {
            fail("Unexpected exception: " + unexpected.getClass().getSimpleName());
        }
    }

    @Test
    public void cancellationInvokesRegisteredDisconnectActionOnce() {
        RequestCancellation cancellation = new RequestCancellation();
        AtomicInteger calls = new AtomicInteger();
        cancellation.setCancelAction(calls::incrementAndGet);

        cancellation.cancel();
        cancellation.cancel();

        assertTrue(cancellation.isCancelled());
        assertEquals(1, calls.get());
    }

    @Test
    public void actionRegisteredAfterCancellationRunsImmediately() {
        RequestCancellation cancellation = new RequestCancellation();
        AtomicInteger calls = new AtomicInteger();
        cancellation.cancel();

        cancellation.setCancelAction(calls::incrementAndGet);

        assertEquals(1, calls.get());
    }

    @Test(expected = IllegalArgumentException.class)
    public void credentialHeaderInjectionIsRejectedBeforeNetworkAccess() {
        new UrlConnectionChatHttpTransport().post(
                URI.create("https://ai.example.com/v1/chat/completions"),
                new byte[]{'{', '}'},
                "example\r\nunsafe".toCharArray(),
                HttpExecutionPolicy.defaults(),
                new RequestCancellation()
        );
    }
}
