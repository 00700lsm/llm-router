package com.llmrouter.api;

public record ErrorResponse(
        String requestId,
        String error,
        String message
) {
}
