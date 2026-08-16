package com.llmrouter.evaluation;

import java.util.List;

public record BaselineRoutingEvaluationReport(
        String question,
        String qualityMethod,
        String routingPolicy,
        String datasetPath,
        List<EvaluationResult> results,
        List<EvaluationResult> failures,
        BaselineRoutingSummary summary
) {
}
