package com.chenwei666.netserial.diagnostics;

import com.chenwei666.netserial.device.Vendor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Produces read-only vendor plans and conservative findings from captured output. */
public final class SwitchHealthEngine {
    private static final int MAX_CAPTURE = 500_000;
    private static final Pattern CPU = Pattern.compile("(?i)(?:cpu[^\\n%]{0,48}?)(\\d{1,3})\\s*%");
    private static final Pattern MEMORY = Pattern.compile("(?i)(?:memory[^\\n%]{0,48}?)(\\d{1,3})\\s*%");
    private static final Pattern TEMPERATURE = Pattern.compile("(?i)(?:temperature|temp)[^\\n\\d-]{0,32}(-?\\d{1,3})(?:\\s*°?c)?");
    private static final Pattern ERROR_COUNT = Pattern.compile("(?i)(\\d+)\\s+(?:input errors|output errors|crc|drops|discard)");

    public HealthCheckPlan plan(Vendor vendor) {
        if (vendor == null) vendor = Vendor.GENERIC;
        switch (vendor) {
            case HUAWEI_VRP:
                return new HealthCheckPlan(vendor, Arrays.asList("display cpu-usage", "display memory-usage",
                        "display temperature all", "display device", "display interface brief",
                        "display interface counters errors", "display transceiver diagnosis interface",
                        "display stp brief", "display eth-trunk", "display poe power"));
            case CISCO_IOS:
                return new HealthCheckPlan(vendor, Arrays.asList("show processes cpu sorted",
                        "show processes memory", "show environment all", "show inventory",
                        "show interfaces status", "show interfaces counters errors",
                        "show interfaces transceiver detail", "show spanning-tree summary",
                        "show etherchannel summary", "show power inline"));
            case RUIJIE_RGOS:
                return new HealthCheckPlan(vendor, Arrays.asList("show cpu", "show memory",
                        "show temperature", "show version", "show interfaces status",
                        "show interfaces counters errors", "show interfaces transceiver",
                        "show spanning-tree summary", "show aggregatePort summary", "show poe status"));
            case H3C_COMWARE:
                return new HealthCheckPlan(vendor, Arrays.asList("display cpu-usage", "display memory",
                        "display environment", "display device", "display interface brief",
                        "display counters inbound interface", "display counters outbound interface",
                        "display transceiver diagnosis interface", "display stp brief",
                        "display link-aggregation verbose", "display poe device"));
            case GENERIC:
            default:
                return new HealthCheckPlan(Vendor.GENERIC, Arrays.asList("show version", "show system",
                        "show interfaces", "show environment", "show spanning-tree"));
        }
    }

    public HealthReport analyze(String capture) {
        String text = capture == null ? "" : capture;
        if (text.length() > MAX_CAPTURE) text = text.substring(text.length() - MAX_CAPTURE);
        List<HealthFinding> findings = new ArrayList<>();
        addPercentFinding(findings, CPU, text, "CPU_HIGH", 80, 95,
                "Verify control-plane load and the processes consuming CPU.");
        addPercentFinding(findings, MEMORY, text, "MEMORY_HIGH", 80, 95,
                "Check memory consumers and recent process or routing-table growth.");
        Matcher temperature = TEMPERATURE.matcher(text);
        while (temperature.find()) {
            int value = parseInt(temperature.group(1));
            if (value >= 70) findings.add(new HealthFinding(value >= 85 ? DiagnosticSeverity.CRITICAL
                    : DiagnosticSeverity.WARNING, "TEMPERATURE_HIGH", temperature.group(),
                    "Check airflow, ambient temperature, fans, and blocked vents."));
        }
        Matcher errors = ERROR_COUNT.matcher(text);
        while (errors.find()) {
            long value = parseLong(errors.group(1));
            if (value > 0) findings.add(new HealthFinding(value > 1000 ? DiagnosticSeverity.CRITICAL
                    : DiagnosticSeverity.WARNING, "INTERFACE_ERRORS", errors.group(),
                    "Compare counter deltas and inspect cabling, optics, duplex, and congestion."));
        }
        String lower = text.toLowerCase(Locale.ROOT);
        if (containsAny(lower, "fan failed", "fan fault", "power failed", "power fault", "over temperature")) {
            findings.add(new HealthFinding(DiagnosticSeverity.CRITICAL, "HARDWARE_ALARM",
                    "A fan, power, or temperature alarm was detected.",
                    "Follow the vendor hardware replacement procedure and preserve redundancy."));
        }
        if (findings.isEmpty()) findings.add(new HealthFinding(DiagnosticSeverity.INFO, "NO_THRESHOLD_ALERT",
                "No configured threshold pattern was found in the supplied capture.",
                "Confirm the complete command set was captured and compare values with the device baseline."));
        return new HealthReport(findings);
    }

    private static void addPercentFinding(List<HealthFinding> findings, Pattern pattern, String text,
                                          String code, int warning, int critical, String recommendation) {
        Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
            int value = parseInt(matcher.group(1));
            if (value >= warning && value <= 100) findings.add(new HealthFinding(
                    value >= critical ? DiagnosticSeverity.CRITICAL : DiagnosticSeverity.WARNING,
                    code, matcher.group(), recommendation));
        }
    }

    private static boolean containsAny(String value, String... needles) {
        for (String needle : needles) if (value.contains(needle)) return true;
        return false;
    }

    private static int parseInt(String value) {
        try { return Integer.parseInt(value); } catch (RuntimeException ignored) { return -1; }
    }

    private static long parseLong(String value) {
        try { return Long.parseLong(value); } catch (RuntimeException ignored) { return -1; }
    }
}
