package com.llmrouter.metrics;

public record Usage(
        Integer inputTokens,
        Integer outputTokens,
        Integer totalTokens
) {

    public static Usage unknown() {
        return new Usage(null, null, null);
    }

    public boolean hasTokenCounts() {
        return inputTokens != null && outputTokens != null;
    }
}
