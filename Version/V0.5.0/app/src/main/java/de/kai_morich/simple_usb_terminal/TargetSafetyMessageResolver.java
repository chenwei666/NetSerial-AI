package de.kai_morich.simple_usb_terminal;

import android.content.Context;

import com.chenwei666.netserial.safety.TargetSafetyReason;

final class TargetSafetyMessageResolver {
    private TargetSafetyMessageResolver() { }

    static String resolve(Context context, TargetSafetyReason reason) {
        switch (reason) {
            case CHANGE_TASK_REQUIRED: return context.getString(R.string.target_reason_task_required);
            case CHANGE_TASK_MISMATCH: return context.getString(R.string.target_reason_task_mismatch);
            case MANAGEMENT_ADDRESS_REQUIRED: return context.getString(R.string.target_reason_management_required);
            case MANAGEMENT_ADDRESS_MISMATCH: return context.getString(R.string.target_reason_management_mismatch);
            case TARGET_MISMATCH: return context.getString(R.string.target_reason_target_mismatch);
            default: return context.getString(R.string.target_reason_allowed);
        }
    }
}
