package com.chenwei666.netserial.web;

import com.chenwei666.netserial.device.Vendor;
import org.junit.Test;

import static org.junit.Assert.*;

public class WebAccessPlanFactoryTest {
    @Test public void h3cHttpsPlanRedactsPassword() {
        char[] password = "Strong9@Pass".toCharArray();
        try (WebAccessRequest request = new WebAccessRequest(Vendor.H3C_COMWARE, "Comware 7",
                "netadmin", password, true, false)) {
            WebAccessPlan plan = new WebAccessPlanFactory().create(request);
            assertTrue(plan.commandBatch().contains("local-user netadmin class manage"));
            assertTrue(plan.commandBatch().contains("ip https enable"));
            assertFalse(plan.commandBatch().contains("ip http enable"));
            assertFalse(plan.redactedBatch().contains("Strong9@Pass"));
            assertTrue(plan.redactedBatch().contains("[REDACTED]"));
            plan.destroy();
            assertTrue(plan.getCommands().isEmpty());
        }
    }

    @Test public void vendorTemplatesContainVerifiedHttpsCommands() {
        assertTemplate(Vendor.HUAWEI_VRP, "http secure-server enable");
        assertTemplate(Vendor.CISCO_IOS, "ip http secure-server");
        assertTemplate(Vendor.RUIJIE_RGOS, "enable service web-server https");
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsCliInjectionInUsername() {
        new WebAccessRequest(Vendor.CISCO_IOS, "IOS", "admin\nreload",
                "Strong9@Pass".toCharArray(), true, false);
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsUnsafePasswordCharacters() {
        new WebAccessRequest(Vendor.CISCO_IOS, "IOS", "admin",
                "Strong9;reload".toCharArray(), true, false);
    }

    private static void assertTemplate(Vendor vendor, String command) {
        try (WebAccessRequest request = new WebAccessRequest(vendor, "", "admin",
                "Strong9@Pass".toCharArray(), true, false)) {
            try (WebAccessPlan plan = new WebAccessPlanFactory().create(request)) {
                assertTrue(plan.commandBatch().contains(command));
            }
        }
    }
}
