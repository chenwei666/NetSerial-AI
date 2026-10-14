package com.chenwei666.netserial.automation;

import com.chenwei666.netserial.device.Vendor;
import com.chenwei666.netserial.safety.RiskLevel;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.*;

public class SafePlaybookEngineTest {
    @Test public void discoveryPlaybooksRemainReadOnly() {
        PlaybookPlan plan = new SafePlaybookEngine().plan(Vendor.CISCO_IOS,
                PlaybookType.NEIGHBOR_DISCOVERY, "");
        assertEquals(RiskLevel.R1_READ_ONLY, plan.getRisk());
        assertTrue(plan.commandBatch().contains("show lldp neighbors detail"));
    }

    @Test public void validatesInterfaceAndVlanParameters() {
        SafePlaybookEngine engine = new SafePlaybookEngine();
        assertTrue(engine.plan(Vendor.H3C_COMWARE, PlaybookType.INTERFACE_DIAGNOSIS,
                "GigabitEthernet1/0/1").commandBatch().contains("GigabitEthernet1/0/1"));
        assertTrue(engine.plan(Vendor.HUAWEI_VRP, PlaybookType.VLAN_AUDIT, "4094")
                .commandBatch().contains("4094"));
    }

    @Test public void batchPlannerUsesCanaryAndDeduplicates() {
        PlaybookPlan playbook = new SafePlaybookEngine().plan(Vendor.CISCO_IOS,
                PlaybookType.PRE_CHANGE_HEALTH, "");
        BatchExecutionPlan plan = new BatchTaskPlanner().plan(
                Arrays.asList("sw1.example", "sw2.example", "sw1.example"), playbook);
        assertEquals("sw1.example", plan.getCanaryTarget());
        assertEquals(1, plan.getRemainingTargets().size());
        assertTrue(plan.requiresPerTargetApproval());
        assertTrue(plan.stopsOnFirstFailure());
    }
}
