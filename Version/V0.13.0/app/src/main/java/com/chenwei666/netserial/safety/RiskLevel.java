package com.chenwei666.netserial.safety;

public enum RiskLevel {
    R0_INFORMATIONAL,
    R1_READ_ONLY,
    R2_CONFIGURATION,
    R3_HIGH,
    R4_CRITICAL;

    public static RiskLevel max(RiskLevel first, RiskLevel second) {
        return first.ordinal() >= second.ordinal() ? first : second;
    }
}
