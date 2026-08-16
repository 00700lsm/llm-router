package com.llmrouter.llm;

import com.llmrouter.metrics.Usage;

public record ModelResponse(
        String content,
        String model,
        String provider,
        Usage usage,
        long latencyMs,
        boolean success,
        String error
) {

    public static ModelResponse success(
            String content,
            String model,
            String provider,
            Usage usage,
            long latencyMs
    ) {
        return new ModelResponse(content, model, provider, usage, latencyMs, true, null);
    }

    public static ModelResponse failure(
            String model,
            String provider,
            Usage usage,
            long latencyMs,
            String error
    ) {
        return new ModelResponse(null, model, provider, usage, latencyMs, false, error);
    }
}
