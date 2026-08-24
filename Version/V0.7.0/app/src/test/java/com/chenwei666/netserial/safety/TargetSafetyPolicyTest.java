package com.chenwei666.netserial.safety;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.chenwei666.netserial.change.ChangeTask;
import com.chenwei666.netserial.change.ChangeTaskStatus;
import com.chenwei666.netserial.device.CliMode;
import com.chenwei666.netserial.device.DeviceEnvironment;
import com.chenwei666.netserial.device.DeviceProfile;
import com.chenwei666.netserial.device.Vendor;

import org.junit.Test;

import java.util.ArrayList;

public class TargetSafetyPolicyTest {
    private final DeviceProfile protectedDevice = new DeviceProfile("SW-CORE-01",
            Vendor.H3C_COMWARE, CliMode.SYSTEM_VIEW, 9600, DeviceEnvironment.PRODUCTION, true);

    @Test public void blocksHighRiskWithoutChangeTask() {
        assertFalse(new TargetSafetyPolicy().evaluate(protectedDevice, "SSH · SW-CORE-01 · 10.0.0.1",
                RiskLevel.R3_HIGH, null, 5_000).isAllowed());
    }

    @Test public void allowsReadOnlyWithoutChangeTask() {
        assertTrue(new TargetSafetyPolicy().evaluate(protectedDevice, "SW-CORE-01",
                RiskLevel.R1_READ_ONLY, null, 5_000).isAllowed());
    }

    @Test public void allowsHighRiskInsideMatchingWindow() {
        ChangeTask task = new ChangeTask("id", "CHG", "DC", "SW-CORE-01", "operator", "goal",
                "pre", "change", "verify", "rollback", 1_000, 10_000,
                ChangeTaskStatus.DRAFT, new ArrayList<>()).start(1_100);
        assertTrue(new TargetSafetyPolicy().evaluate(protectedDevice,
                "SW-CORE-01", RiskLevel.R3_HIGH, task, 5_000).isAllowed());
    }

    @Test public void blocksRemoteHostThatDiffersFromSavedManagementAddress() {
        DeviceProfile pinned = new DeviceProfile("SW-CORE-01", Vendor.H3C_COMWARE,
                CliMode.SYSTEM_VIEW, 9600, DeviceEnvironment.PRODUCTION, true, "10.0.0.1");
        ChangeTask task = new ChangeTask("id", "CHG", "DC", "SW-CORE-01", "operator", "goal",
                "pre", "change", "verify", "rollback", 1_000, 10_000,
                ChangeTaskStatus.DRAFT, new ArrayList<>()).start(1_100);
        assertFalse(new TargetSafetyPolicy().evaluate(pinned,
                "10.0.0.2", RiskLevel.R3_HIGH, task, 5_000).isAllowed());
    }

    @Test public void blocksAddressPrefixCollision() {
        DeviceProfile pinned = new DeviceProfile("SW-CORE-01", Vendor.H3C_COMWARE,
                CliMode.SYSTEM_VIEW, 9600, DeviceEnvironment.PRODUCTION, true, "10.0.0.1");
        ChangeTask task = new ChangeTask("id", "CHG", "DC", "SW-CORE-01", "operator", "goal",
                "pre", "change", "verify", "rollback", 1_000, 10_000,
                ChangeTaskStatus.DRAFT, new ArrayList<>()).start(1_100);
        assertFalse(new TargetSafetyPolicy().evaluate(pinned,
                "10.0.0.10", RiskLevel.R3_HIGH, task, 5_000).isAllowed());
    }
}
