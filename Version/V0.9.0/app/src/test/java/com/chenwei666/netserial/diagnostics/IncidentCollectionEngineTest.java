package com.chenwei666.netserial.diagnostics;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.chenwei666.netserial.device.Vendor;
import com.chenwei666.netserial.safety.RiskLevel;

import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

public class IncidentCollectionEngineTest {
    @Test public void producesBoundedDeduplicatedReadOnlyPlansForEveryVendor() {
        for (Vendor vendor : Vendor.values()) {
            IncidentCollectionPlan plan = new IncidentCollectionEngine().plan(vendor);
            assertEquals(RiskLevel.R1_READ_ONLY, plan.getRisk());
            assertTrue(plan.getSteps().size() >= 10);
            assertTrue(plan.getSteps().size() <= 30);
            Set<String> commands = new HashSet<>();
            for (IncidentCollectionStep step : plan.getSteps()) {
                assertTrue(commands.add(step.getCommand()));
                String lower = step.getCommand().toLowerCase();
                assertFalse(lower.startsWith("reboot"));
                assertFalse(lower.startsWith("reload"));
                assertFalse(lower.startsWith("delete"));
            }
        }
    }
}
