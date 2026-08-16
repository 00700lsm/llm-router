package com.llmrouter.evaluation;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.llmrouter.chat.ChatExecutionResult;
import com.llmrouter.chat.ChatService;
import com.llmrouter.error.ErrorCode;
import com.llmrouter.error.LlmRouterException;
import com.llmrouter.routing.BaselineRouter;
import com.llmrouter.routing.RoutingDecision;
import com.llmrouter.routing.RoutingStrategy;

@Component
public class EvaluationRunner {

    static final String QUALITY_METHOD =
            "Deterministic checklist against expectedCondition. Runtime routing does not use this result.";
    static final String ROUTING_POLICY = "BASELINE_DEFAULT: all general requests use the configured default model";

    private static final Path DEFAULT_DATASET_PATH = Path.of("evaluation/dataset.json");

    private final ChatService chatService;
    private final BaselineRouter router;
    private final QualityEvaluator qualityEvaluator;
    private final ObjectMapper objectMapper;

    public EvaluationRunner(
            ChatService chatService,
            BaselineRouter router,
            QualityEvaluator qualityEvaluator,
            ObjectMapper objectMapper
    ) {
        this.chatService = chatService;
        this.router = router;
        this.qualityEvaluator = qualityEvaluator;
        this.objectMapper = objectMapper.copy().enable(SerializationFeature.INDENT_OUTPUT);
    }

    public EvaluationDataset loadDataset() {
        return loadDataset(DEFAULT_DATASET_PATH);
    }

    public EvaluationDataset loadDataset(Path path) {
        if (!Files.exists(path)) {
            throw new LlmRouterException(ErrorCode.UNKNOWN_ERROR, "Evaluation dataset not found: " + path);
        }
        try {
            return objectMapper.readValue(path.toFile(), EvaluationDataset.class);
        } catch (IOException exception) {
            throw new LlmRouterException(ErrorCode.UNKNOWN_ERROR, "Failed to read evaluation dataset", exception);
        }
    }

    public BaselineRoutingEvaluationReport runAll() {
        return run(loadDataset(), DEFAULT_DATASET_PATH, Duration.ofSeconds(5));
    }

    public List<EvaluationResult> run(EvaluationDataset dataset) {
        return run(dataset, DEFAULT_DATASET_PATH, Duration.ZERO).results();
    }

    public BaselineRoutingEvaluationReport run(
            EvaluationDataset dataset,
            Path datasetPath,
            Duration pauseBetweenCalls
    ) {
        if (dataset == null || dataset.cases() == null || dataset.cases().isEmpty()) {
            throw new LlmRouterException(ErrorCode.UNKNOWN_ERROR, "Evaluation dataset has no cases");
        }
        List<EvaluationResult> results = new ArrayList<>();
        boolean first = true;
        for (EvaluationCase evaluationCase : dataset.cases()) {
            if (!first) {
                pause(pauseBetweenCalls);
            }
            first = false;
            results.add(run(evaluationCase));
        }
        List<EvaluationResult> failures = results.stream()
                .filter(result -> !result.success() || !QualityResult.PASS.equals(result.quality()))
                .toList();
        return new BaselineRoutingEvaluationReport(
                "What does the baseline router select, and do results meet expectedCondition?",
                QUALITY_METHOD,
                ROUTING_POLICY,
                datasetPath == null ? null : datasetPath.toString(),
                results,
                failures,
                summarize(results)
        );
    }

    public EvaluationResult run(EvaluationCase evaluationCase) {
        try {
            ChatExecutionResult execution = chatService.chat(evaluationCase.input());
            String answer = execution.response() == null ? null : execution.response().answer();
            QualityResult quality = qualityEvaluator.evaluate(evaluationCase, answer);
            return new EvaluationResult(
                    evaluationCase.id(),
                    evaluationCase.category(),
                    evaluationCase.input(),
                    execution.decision().selectedModel(),
                    execution.decision().provider(),
                    execution.decision().strategy().name(),
                    execution.decision().reason(),
                    execution.success(),
                    quality.verdict(),
                    quality.reason(),
                    answer,
                    execution.modelLatencyMs(),
                    execution.endToEndLatencyMs(),
                    execution.usage() == null ? null : execution.usage().inputTokens(),
                    execution.usage() == null ? null : execution.usage().outputTokens(),
                    execution.cost() == null ? "UNKNOWN" : execution.cost().displayValue(),
                    execution.errorCode()
            );
        } catch (LlmRouterException exception) {
            RoutingDecision decision = routingDecisionOrNull(evaluationCase);
            return new EvaluationResult(
                    evaluationCase.id(),
                    evaluationCase.category(),
                    evaluationCase.input(),
                    decision == null ? null : decision.selectedModel(),
                    decision == null ? null : decision.provider(),
                    decision == null ? null : decision.strategy().name(),
                    decision == null ? null : decision.reason(),
                    false,
                    QualityResult.FAIL,
                    exception.getMessage(),
                    null,
                    null,
                    null,
                    null,
                    null,
                    "UNKNOWN",
                    exception.errorCode().name()
            );
        }
    }

    public Path write(BaselineRoutingEvaluationReport report, Path outputPath) {
        try {
            Files.createDirectories(outputPath.getParent());
            objectMapper.writeValue(outputPath.toFile(), report);
            return outputPath;
        } catch (IOException exception) {
            throw new LlmRouterException(ErrorCode.UNKNOWN_ERROR, "Failed to write evaluation results", exception);
        }
    }

    private RoutingDecision routingDecisionOrNull(EvaluationCase evaluationCase) {
        try {
            return router.route("eval-" + evaluationCase.id(), evaluationCase.input());
        } catch (LlmRouterException ignored) {
            return null;
        }
    }

    private void pause(Duration pauseBetweenCalls) {
        if (pauseBetweenCalls == null || pauseBetweenCalls.isZero() || pauseBetweenCalls.isNegative()) {
            return;
        }
        try {
            Thread.sleep(pauseBetweenCalls.toMillis());
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new LlmRouterException(ErrorCode.UNKNOWN_ERROR, "Evaluation was interrupted", exception);
        }
    }

    private BaselineRoutingSummary summarize(List<EvaluationResult> results) {
        Map<String, Long> selectedModelCounts = new LinkedHashMap<>();
        int successCount = 0;
        int qualityPassCount = 0;
        for (EvaluationResult result : results) {
            if (result.success()) {
                successCount++;
            }
            if (QualityResult.PASS.equals(result.quality())) {
                qualityPassCount++;
            }
            String model = result.selectedModel() == null ? "UNKNOWN" : result.selectedModel();
            selectedModelCounts.merge(model, 1L, Long::sum);
        }
        return new BaselineRoutingSummary(
                results.size(),
                successCount,
                qualityPassCount,
                selectedModelCounts,
                RoutingStrategy.BASELINE_DEFAULT.name()
        );
    }
}
