package com.llmrouter.api;

public record ChatResponse(
        String requestId,
        String answer,
        String model,
        String provider
) {
}
