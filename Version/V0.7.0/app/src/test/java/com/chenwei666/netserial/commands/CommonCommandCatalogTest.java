package com.chenwei666.netserial.commands;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.chenwei666.netserial.device.Vendor;
import com.chenwei666.netserial.safety.RiskLevel;

import org.junit.Test;

import java.util.List;

public class CommonCommandCatalogTest {
    private final CommonCommandCatalog catalog = CommonCommandCatalog.createDefault();

    @Test
    public void coversEveryPriorityVendorAndCategory() {
        for (Vendor vendor : new Vendor[]{Vendor.H3C_COMWARE, Vendor.HUAWEI_VRP,
                Vendor.CISCO_IOS, Vendor.RUIJIE_RGOS}) {
            assertFalse(catalog.search(vendor, null, "", 200).isEmpty());
            for (CommandCategory category : CommandCategory.values()) {
                assertFalse(vendor + " lacks " + category,
                        catalog.search(vendor, category, "", 200).isEmpty());
            }
        }
    }

    @Test
    public void searchIsScopedToVendorAndCategory() {
        List<CommonCommand> results = catalog.search(Vendor.H3C_COMWARE,
                CommandCategory.INTERFACE, "errors", 20);
        assertEquals(1, results.size());
        assertEquals(Vendor.H3C_COMWARE, results.get(0).getVendor());
        assertEquals(CommandCategory.INTERFACE, results.get(0).getCategory());
    }

    @Test
    public void disruptiveTemplatesAreMarkedHighRisk() {
        List<CommonCommand> results = catalog.search(Vendor.CISCO_IOS,
                CommandCategory.INTERFACE, "shutdown", 20);
        assertTrue(results.stream().anyMatch(command -> command.getCommand().equals("shutdown")
                && command.getRiskLevel() == RiskLevel.R3_HIGH));
    }
}
