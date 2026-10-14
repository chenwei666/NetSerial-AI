package de.kai_morich.simple_usb_terminal;

import android.app.AlertDialog;
import android.content.Context;
import android.text.InputType;
import android.widget.EditText;
import android.widget.Toast;

import com.chenwei666.netserial.safety.RiskLevel;

final class CommandSendConfirmation {
    private CommandSendConfirmation() { }

    static void confirm(Context context, String command, RiskLevel riskLevel, Runnable onConfirmed) {
        confirm(context, command, riskLevel, onConfirmed, () -> { });
    }

    static void confirm(Context context, String command, RiskLevel riskLevel, Runnable onConfirmed,
                        Runnable onCancelled) {
        if (riskLevel.ordinal() < RiskLevel.R3_HIGH.ordinal()) {
            onConfirmed.run();
            return;
        }
        if (riskLevel == RiskLevel.R4_CRITICAL) {
            EditText input = new EditText(context);
            input.setHint("EXECUTE");
            input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);
            new AlertDialog.Builder(context)
                    .setTitle(R.string.command_critical_title)
                    .setMessage(context.getString(R.string.command_critical_message, command))
                    .setView(input)
                    .setNegativeButton(android.R.string.cancel, (dialog, which) -> onCancelled.run())
                    .setOnCancelListener(dialog -> onCancelled.run())
                    .setPositiveButton(R.string.command_confirm_send, (dialog, which) -> {
                        if ("EXECUTE".equals(input.getText().toString().trim())) onConfirmed.run();
                        else Toast.makeText(context, R.string.command_confirmation_mismatch, Toast.LENGTH_LONG).show();
                    }).show();
            return;
        }
        new AlertDialog.Builder(context)
                .setTitle(R.string.command_high_risk_title)
                .setMessage(context.getString(R.string.command_high_risk_message, command))
                .setNegativeButton(android.R.string.cancel, (dialog, which) -> onCancelled.run())
                .setOnCancelListener(dialog -> onCancelled.run())
                .setPositiveButton(R.string.command_confirm_send, (dialog, which) -> onConfirmed.run())
                .show();
    }
}
