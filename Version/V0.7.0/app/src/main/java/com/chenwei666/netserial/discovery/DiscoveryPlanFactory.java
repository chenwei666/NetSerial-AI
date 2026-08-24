package com.chenwei666.netserial.discovery;

import com.chenwei666.netserial.automation.PlaybookType;
import com.chenwei666.netserial.automation.SafePlaybookEngine;
import com.chenwei666.netserial.device.Vendor;

/** Read-only CLI discovery adapter for devices already connected through a terminal. */
public final class DiscoveryPlanFactory {
    public DiscoveryPlan create(Vendor vendor) {
        return new DiscoveryPlan(new SafePlaybookEngine()
                .plan(vendor, PlaybookType.NEIGHBOR_DISCOVERY, "").getCommands());
    }
}
