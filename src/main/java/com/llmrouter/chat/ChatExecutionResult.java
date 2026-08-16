package com.llmrouter.chat;

import com.llmrouter.api.ChatResponse;
import com.llmrouter.metrics.EstimatedCost;
import com.llmrouter.metrics.Usage;
import com.llmrouter.routing.RoutingDecision;

public record ChatExecutionResult(
        ChatResponse response,
        RoutingDecision decision,
        Usage usage,
        EstimatedCost cost,
        long modelLatencyMs,
        long endToEndLatencyMs,
        boolean success,
        String errorCode
) {
}
