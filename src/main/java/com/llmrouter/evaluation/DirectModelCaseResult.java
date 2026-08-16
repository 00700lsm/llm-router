package com.llmrouter.evaluation;

public record DirectModelCaseResult(
        String caseId,
        String category,
        String input,
        String model,
        String provider,
        boolean success,
        String quality,
        String qualityReason,
        String answer,
        Long modelLatencyMs,
        Long endToEndLatencyMs,
        Integer inputTokens,
        Integer outputTokens,
        String estimatedCost,
        String errorCode
) {
}
