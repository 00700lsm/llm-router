package com.llmrouter.evaluation;

import java.util.List;

public record DirectModelEvaluationReport(
        String question,
        String qualityMethod,
        String datasetPath,
        List<String> models,
        List<DirectModelCaseResult> results,
        List<ModelBaselineSummary> summaries
) {
}
