package com.llmrouter.evaluation;

public record ModelBaselineSummary(
        String model,
        String provider,
        int totalCases,
        int successCount,
        int qualityPassCount,
        double qualityPassRate,
        Double averageModelLatencyMs,
        Double averageEndToEndLatencyMs,
        Integer totalInputTokens,
        Integer totalOutputTokens,
        String totalEstimatedCost
) {
}
