package com.chenwei666.netserial.device;

import org.junit.Test;

import static org.junit.Assert.*;

public class DeviceFingerprintEngineTest {
    private final DeviceFingerprintEngine engine = new DeviceFingerprintEngine();

    @Test public void identifiesSupportedVendorBanners() {
        assertEquals(Vendor.H3C_COMWARE, engine.identify("H3C Comware Software, Version 7.1").getVendor());
        assertEquals(Vendor.HUAWEI_VRP, engine.identify("Huawei Versatile Routing Platform Software VRP").getVendor());
        assertEquals(Vendor.CISCO_IOS, engine.identify("Cisco IOS XE Software, Version 17.9").getVendor());
        assertEquals(Vendor.RUIJIE_RGOS, engine.identify("Ruijie Networks RGOS 11.4").getVendor());
    }

    @Test public void ambiguousPromptDoesNotOverrideProfile() {
        DeviceFingerprint result = engine.identify("Switch# show version");
        assertEquals(Vendor.GENERIC, result.getVendor());
        assertFalse(result.isHighConfidence());
    }

    @Test public void recognizesComwareMajorVersion() {
        assertEquals("Comware 7", engine.identify("H3C Comware Software, Version 7.1").getPlatform());
    }
}
