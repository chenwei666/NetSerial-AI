package com.chenwei666.netserial.compliance;

public final class ComplianceFinding {
    private final String ruleId;
    private final ComplianceSeverity severity;
    private final String message;
    private final String recommendation;

    ComplianceFinding(String ruleId, ComplianceSeverity severity, String message, String recommendation) {
        this.ruleId = ruleId;
        this.severity = severity;
        this.message = message;
        this.recommendation = recommendation;
    }

    public String getRuleId() { return ruleId; }
    public ComplianceSeverity getSeverity() { return severity; }
    public String getMessage() { return message; }
    public String getRecommendation() { return recommendation; }
}
