package com.chenwei666.netserial.guided;

/** One observation, its limits, and a concrete next step. */
public final class DiagnosticFinding {
    public enum Status { PASS, WARNING, FAIL, UNKNOWN }
    private final Status status;
    private final GuideText title;
    private final GuideText explanation;
    private final GuideText nextStep;
    private final String evidence;

    public DiagnosticFinding(Status status, GuideText title, GuideText explanation,
                             GuideText nextStep, String evidence) {
        if (status == null || title == null || explanation == null || nextStep == null) {
            throw new IllegalArgumentException("Finding fields required");
        }
        this.status = status;
        this.title = title;
        this.explanation = explanation;
        this.nextStep = nextStep;
        String value = evidence == null ? "" : evidence;
        this.evidence = value.substring(0, Math.min(value.length(), 8000));
    }

    public Status getStatus() { return status; }
    public GuideText getTitle() { return title; }
    public GuideText getExplanation() { return explanation; }
    public GuideText getNextStep() { return nextStep; }
    public String getEvidence() { return evidence; }
}
