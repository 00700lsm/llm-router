package com.llmrouter.evaluation;

import java.util.Map;

public record BaselineRoutingSummary(
        int totalCases,
        int successCount,
        int qualityPassCount,
        Map<String, Long> selectedModelCounts,
        String routingPolicy
) {
}
