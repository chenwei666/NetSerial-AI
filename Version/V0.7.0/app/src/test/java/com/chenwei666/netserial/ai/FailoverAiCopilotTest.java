package com.chenwei666.netserial.ai;

import com.chenwei666.netserial.device.CliMode;
import com.chenwei666.netserial.device.Vendor;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;

public class FailoverAiCopilotTest {
    @Test public void retriesTransientFailureThenUsesNextProvider() throws Exception {
        AtomicInteger firstCalls = new AtomicInteger();
        AiCopilot first = request -> {
            firstCalls.incrementAndGet();
            throw new AiProviderException(AiProviderError.NETWORK, "network", true, null);
        };
        CommandPlan expected = new CommandPlan(Collections.emptyList());
        AiCopilot second = request -> expected;
        AiFailoverResult result = new FailoverAiCopilot(Arrays.asList(
                new AiProviderCandidate("first", first), new AiProviderCandidate("second", second)), 2)
                .propose(new AiRequest("diagnose", Vendor.H3C_COMWARE, CliMode.USER_VIEW, ""));
        assertSame(expected, result.getPlan());
        assertEquals("second", result.getProviderAlias());
        assertEquals(2, firstCalls.get());
        assertEquals(3, result.getAttempts().size());
    }
}
