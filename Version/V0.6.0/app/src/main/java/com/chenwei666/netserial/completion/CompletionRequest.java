package com.chenwei666.netserial.completion;

import com.chenwei666.netserial.device.CliMode;
import com.chenwei666.netserial.device.Vendor;

import java.util.Objects;

public final class CompletionRequest {
    private final Vendor vendor;
    private final CliMode cliMode;
    private final String input;
    private final int limit;
    private final String platform;
    private final String context;

    public CompletionRequest(Vendor vendor, CliMode cliMode, String input, int limit) {
        this(vendor, cliMode, input, limit, "", "");
    }

    public CompletionRequest(Vendor vendor, CliMode cliMode, String input, int limit,
                             String platform, String context) {
        this.vendor = Objects.requireNonNull(vendor, "vendor");
        this.cliMode = Objects.requireNonNull(cliMode, "cliMode");
        this.input = Objects.requireNonNull(input, "input").trim();
        if (limit < 1 || limit > 50) {
            throw new IllegalArgumentException("limit must be between 1 and 50");
        }
        this.limit = limit;
        this.platform = normalizeOptional(platform, 128);
        this.context = normalizeOptional(context, 2_000);
    }

    public Vendor getVendor() {
        return vendor;
    }

    public CliMode getCliMode() {
        return cliMode;
    }

    public String getInput() {
        return input;
    }

    public int getLimit() {
        return limit;
    }

    public String getPlatform() { return platform; }

    public String getContext() { return context; }

    private static String normalizeOptional(String value, int maximum) {
        String normalized = value == null ? "" : value.trim();
        if (normalized.length() > maximum) normalized = normalized.substring(normalized.length() - maximum);
        return normalized;
    }
}
