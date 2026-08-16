package com.llmrouter.evaluation;

public record EvaluationCase(
        String id,
        String category,
        String input,
        ExpectedCondition expectedCondition
) {
}
