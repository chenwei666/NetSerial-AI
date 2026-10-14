package com.chenwei666.netserial.device;

import java.util.Objects;

public final class DeviceFingerprint {
    private final Vendor vendor;
    private final int confidence;
    private final String platform;
    private final String evidence;

    public DeviceFingerprint(Vendor vendor, int confidence, String platform, String evidence) {
        this.vendor = Objects.requireNonNull(vendor, "vendor");
        if (confidence < 0 || confidence > 100) throw new IllegalArgumentException("confidence");
        this.confidence = confidence;
        this.platform = platform == null ? "" : platform.trim();
        this.evidence = evidence == null ? "" : evidence.trim();
    }

    public static DeviceFingerprint unknown() {
        return new DeviceFingerprint(Vendor.GENERIC, 0, "", "");
    }

    public Vendor getVendor() { return vendor; }
    public int getConfidence() { return confidence; }
    public String getPlatform() { return platform; }
    public String getEvidence() { return evidence; }
    public boolean isHighConfidence() { return vendor != Vendor.GENERIC && confidence >= 80; }
}
