package com.chenwei666.netserial.change;

import com.chenwei666.netserial.terminal.AnsiTextSanitizer;
import com.chenwei666.netserial.terminal.SensitiveTextRedactor;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public final class ChangeEvidenceFormatter {
    private final SensitiveTextRedactor redactor = new SensitiveTextRedactor();
    private final AnsiTextSanitizer ansi = new AnsiTextSanitizer();

    public String toMarkdown(ChangeTask task) {
        StringBuilder report = new StringBuilder();
        report.append("# NetSerial AI Change Evidence\n\n")
                .append("- Ticket: ").append(safe(task.getTicketNumber())).append('\n')
                .append("- Site: ").append(safe(task.getSite())).append('\n')
                .append("- Device: ").append(safe(task.getDeviceName())).append('\n')
                .append("- Operator: ").append(safe(task.getOperatorName())).append('\n')
                .append("- Status: ").append(task.getStatus()).append('\n')
                .append("- Window: ").append(format(task.getWindowStartMillis())).append(" - ")
                .append(format(task.getWindowEndMillis())).append("\n\n")
                .append("## Goal\n\n").append(safe(task.getGoal())).append("\n\n")
                .append("## Pre-check\n\n```text\n").append(safe(task.getPrecheckPlan())).append("\n```\n\n")
                .append("## Planned commands\n\n```text\n").append(safe(task.getCommandPlan())).append("\n```\n\n")
                .append("## Verification\n\n```text\n").append(safe(task.getVerificationPlan())).append("\n```\n\n")
                .append("## Rollback\n\n```text\n").append(safe(task.getRollbackPlan())).append("\n```\n\n")
                .append("## Evidence timeline\n\n");
        for (ChangeEvent event : task.getEvents()) {
            report.append("- ").append(format(event.getTimestampMillis())).append(" | ")
                    .append(event.getType()).append(" | ").append(safe(event.getTarget()))
                    .append(" | ").append(safe(event.getDetail())).append('\n');
        }
        return report.toString();
    }

    private String safe(String value) {
        String input = value == null ? "" : value;
        return redactor.redact(ansi.sanitize(input))
                .replace("```", "''' ").replace('\r', ' ').trim();
    }

    private static String format(long millis) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z", Locale.US);
        format.setTimeZone(TimeZone.getDefault());
        return format.format(new Date(millis));
    }
}
