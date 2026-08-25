package com.chenwei666.netserial.ai;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import com.chenwei666.netserial.device.CliMode;
import com.chenwei666.netserial.device.Vendor;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;

public class FailoverAiChatServiceTest {
    @Test public void retriesTransientFailureThenFailsOver() throws Exception {
        AtomicInteger firstCalls = new AtomicInteger();
        AiChatProvider first = (request, cancellation) -> {
            firstCalls.incrementAndGet();
            throw new AiProviderException(AiProviderError.SERVER, "temporary", 503, true);
        };
        AiChatProvider second = (request, cancellation) -> new AiChatResponse("safe response");
        AiChatResult result = new FailoverAiChatService(Arrays.asList(
                new AiChatProviderCandidate("first", first),
                new AiChatProviderCandidate("second", second)), 2).chat(request(), new RequestCancellation());

        assertEquals(2, firstCalls.get());
        assertEquals("second", result.getProviderAlias());
        assertEquals(3, result.getAttempts().size());
    }

    @Test public void cancellationNeverFailsOver() {
        AtomicInteger secondCalls = new AtomicInteger();
        AiChatProvider cancelled = (request, cancellation) -> {
            throw new AiProviderException(AiProviderError.CANCELLED, "cancelled", 0, false);
        };
        AiChatProvider second = (request, cancellation) -> {
            secondCalls.incrementAndGet();
            return new AiChatResponse("unexpected");
        };
        try {
            new FailoverAiChatService(Arrays.asList(new AiChatProviderCandidate("first", cancelled),
                    new AiChatProviderCandidate("second", second)), 2)
                    .chat(request(), new RequestCancellation());
            fail("Expected cancellation");
        } catch (AiProviderException expected) {
            assertEquals(AiProviderError.CANCELLED, expected.getError());
            assertEquals(0, secondCalls.get());
        } catch (Exception unexpected) {
            fail(unexpected.getClass().getSimpleName());
        }
    }

    private static AiChatRequest request() {
        return new AiChatRequest(Collections.singletonList(
                new AiChatMessage(AiChatRole.USER, "check version", 1)), Vendor.CISCO_IOS,
                CliMode.USER_VIEW, "switch", "", "", "English");
    }
}
