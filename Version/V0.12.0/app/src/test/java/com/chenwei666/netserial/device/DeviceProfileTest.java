package com.chenwei666.netserial.device;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class DeviceProfileTest {
    @Test
    public void acceptsOperationalSerialContext() {
        DeviceProfile profile = new DeviceProfile("Core switch", Vendor.HUAWEI_VRP,
                CliMode.SYSTEM_VIEW, 115200);
        assertEquals("Core switch", profile.getName());
        assertEquals(Vendor.HUAWEI_VRP, profile.getVendor());
        assertEquals(115200, profile.getBaudRate());
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsOutOfRangeBaudRate() {
        new DeviceProfile("Switch", Vendor.GENERIC, CliMode.UNKNOWN, 1);
    }
}
