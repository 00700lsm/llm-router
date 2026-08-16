package com.llmrouter.chat;

import java.time.Duration;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.llmrouter.api.ChatResponse;
import com.llmrouter.error.ErrorCode;
import com.llmrouter.error.LlmRouterException;
import com.llmrouter.llm.LlmGateway;
import com.llmrouter.llm.ModelResponse;
import com.llmrouter.metrics.CostCalculator;
import com.llmrouter.metrics.EstimatedCost;
import com.llmrouter.metrics.Usage;
import com.llmrouter.model.ModelCatalog;
import com.llmrouter.model.ModelDefinition;
import com.llmrouter.routing.BaselineRouter;
import com.llmrouter.routing.RoutingDecision;

@Service
public class ChatService {

    private static final Logger log = LoggerFactory.getLogger(ChatService.class);

    private final BaselineRouter router;
    private final ModelCatalog modelCatalog;
    private final LlmGateway llmGateway;
    private final CostCalculator costCalculator;

    public ChatService(
            BaselineRouter router,
            ModelCatalog modelCatalog,
            LlmGateway llmGateway,
            CostCalculator costCalculator
    ) {
        this.router = router;
        this.modelCatalog = modelCatalog;
        this.llmGateway = llmGateway;
        this.costCalculator = costCalculator;
    }

    public ChatExecutionResult chat(String message) {
        String requestId = UUID.randomUUID().toString();
        long endToEndStart = System.nanoTime();
        RoutingDecision decision = null;
        try {
            decision = router.route(requestId, message);
            ModelDefinition model = modelCatalog.getRequired(decision.selectedModel());
            ModelResponse modelResponse = llmGateway.complete(decision, message);
            EstimatedCost cost = costCalculator.calculate(model, modelResponse.usage());
            long endToEndLatencyMs = elapsedMs(endToEndStart);

            if (!modelResponse.success()) {
                LlmRouterException failure = new LlmRouterException(
                        ErrorCode.PROVIDER_ERROR,
                        modelResponse.error() == null ? "Provider request failed" : modelResponse.error(),
                        requestId
                );
                logObservability(requestId, decision, modelResponse.usage(), cost, modelResponse.latencyMs(),
                        endToEndLatencyMs, false, failure.errorCode().name());
                throw failure;
            }

            ChatExecutionResult result = new ChatExecutionResult(
                    new ChatResponse(requestId, modelResponse.content(), decision.selectedModel(), decision.provider()),
                    decision,
                    modelResponse.usage(),
                    cost,
                    modelResponse.latencyMs(),
                    endToEndLatencyMs,
                    true,
                    null
            );
            logObservability(requestId, decision, result.usage(), result.cost(), result.modelLatencyMs(),
                    result.endToEndLatencyMs(), true, null);
            return result;
        } catch (LlmRouterException exception) {
            LlmRouterException withRequestId = exception.requestId() == null
                    ? exception.withRequestId(requestId)
                    : exception;
            logFailure(requestId, decision, elapsedMs(endToEndStart), withRequestId);
            throw withRequestId;
        }
    }

    private void logFailure(String requestId, RoutingDecision decision, long endToEndLatencyMs, LlmRouterException exception) {
        String selectedModel = decision == null ? null : decision.selectedModel();
        String provider = decision == null ? null : decision.provider();
        String strategy = decision == null ? null : decision.strategy().name();
        String reason = decision == null ? null : decision.reason();
        log.info(
                "requestId={} strategy={} model={} provider={} reason={} modelLatency={}ms endToEndLatency={}ms inputTokens={} outputTokens={} estimatedCost={} success={} error={}",
                requestId,
                strategy,
                selectedModel,
                provider,
                reason,
                null,
                endToEndLatencyMs,
                null,
                null,
                "UNKNOWN",
                false,
                exception.errorCode().name()
        );
    }

    private void logObservability(
            String requestId,
            RoutingDecision decision,
            Usage usage,
            EstimatedCost cost,
            long modelLatencyMs,
            long endToEndLatencyMs,
            boolean success,
            String error
    ) {
        Integer inputTokens = usage == null ? null : usage.inputTokens();
        Integer outputTokens = usage == null ? null : usage.outputTokens();
        String estimatedCost = cost == null ? "UNKNOWN" : cost.displayValue();
        log.info(
                "requestId={} strategy={} model={} provider={} reason={} modelLatency={}ms endToEndLatency={}ms inputTokens={} outputTokens={} estimatedCost={} success={} error={}",
                requestId,
                decision.strategy(),
                decision.selectedModel(),
                decision.provider(),
                decision.reason(),
                modelLatencyMs,
                endToEndLatencyMs,
                inputTokens,
                outputTokens,
                estimatedCost,
                success,
                error
        );
    }

    private long elapsedMs(long startNanos) {
        return Duration.ofNanos(System.nanoTime() - startNanos).toMillis();
    }
}
