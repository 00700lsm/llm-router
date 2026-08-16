package com.llmrouter.evaluation;

public record QualityResult(String verdict, String reason) {

    public static final String PASS = "PASS";
    public static final String FAIL = "FAIL";

    public static QualityResult pass(String reason) {
        return new QualityResult(PASS, reason);
    }

    public static QualityResult fail(String reason) {
        return new QualityResult(FAIL, reason);
    }

    public boolean passed() {
        return PASS.equals(verdict);
    }
}
