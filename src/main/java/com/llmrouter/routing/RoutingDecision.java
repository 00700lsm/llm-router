package com.llmrouter.routing;

public record RoutingDecision(
        String requestId,
        String selectedModel,
        String provider,
        RoutingStrategy strategy,
        String reason
) {
}
