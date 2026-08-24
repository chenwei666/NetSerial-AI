package com.chenwei666.netserial.safety;

import com.chenwei666.netserial.change.ChangeTask;
import com.chenwei666.netserial.device.DeviceProfile;

import java.util.Objects;

public final class TargetSafetyPolicy {
    public TargetSafetyDecision evaluate(DeviceProfile device, String target, RiskLevel risk,
                                         ChangeTask task, long nowMillis) {
        Objects.requireNonNull(device, "device");
        Objects.requireNonNull(risk, "risk");
        String normalizedTarget = target == null || target.trim().isEmpty()
                ? device.getName() : target.trim();
        if (!device.isProtectedDevice() || risk.ordinal() < RiskLevel.R3_HIGH.ordinal()) {
            return new TargetSafetyDecision(true, TargetSafetyReason.ALLOWED);
        }
        if (task == null) {
            return new TargetSafetyDecision(false, TargetSafetyReason.CHANGE_TASK_REQUIRED);
        }
        if (!task.isAuthorizedAt(nowMillis, device.getName())) {
            return new TargetSafetyDecision(false, TargetSafetyReason.CHANGE_TASK_MISMATCH);
        }
        if (normalizedTarget.equalsIgnoreCase(device.getName())) {
            return new TargetSafetyDecision(true, TargetSafetyReason.ALLOWED);
        }
        if (device.getManagementAddress().isEmpty()) {
            return new TargetSafetyDecision(false, TargetSafetyReason.MANAGEMENT_ADDRESS_REQUIRED);
        }
        if (!normalizedTarget.equalsIgnoreCase(device.getManagementAddress())) {
            return new TargetSafetyDecision(false, TargetSafetyReason.MANAGEMENT_ADDRESS_MISMATCH);
        }
        return new TargetSafetyDecision(true, TargetSafetyReason.ALLOWED);
    }
}
