package com.chenwei666.netserial.diagnostics;

import java.util.Objects;

public final class HealthFinding {
    private final DiagnosticSeverity severity;
    private final String code;
    private final String evidence;
    private final String recommendation;

    public HealthFinding(DiagnosticSeverity severity, String code, String evidence,
                         String recommendation) {
        this.severity = Objects.requireNonNull(severity, "severity");
        this.code = requireText(code, 64);
        this.evidence = requireText(evidence, 512);
        this.recommendation = requireText(recommendation, 512);
    }

    public DiagnosticSeverity getSeverity() { return severity; }
    public String getCode() { return code; }
    public String getEvidence() { return evidence; }
    public String getRecommendation() { return recommendation; }

    private static String requireText(String value, int max) {
        String result = Objects.requireNonNull(value, "value").trim();
        if (result.isEmpty() || result.length() > max) throw new IllegalArgumentException("Invalid finding text");
        return result;
    }
}
