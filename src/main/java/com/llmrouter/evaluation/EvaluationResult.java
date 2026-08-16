package com.llmrouter.evaluation;

public record EvaluationResult(
        String caseId,
        String category,
        String input,
        String selectedModel,
        String provider,
        String strategy,
        String reason,
        boolean success,
        Long modelLatencyMs,
        Long endToEndLatencyMs,
        Integer inputTokens,
        Integer outputTokens,
        String estimatedCost,
        String errorCode
) {
}
