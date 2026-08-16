package com.llmrouter.evaluation;

import java.io.IOException;
import java.math.BigDecimal;
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
import com.llmrouter.error.ErrorCode;
import com.llmrouter.error.LlmRouterException;
import com.llmrouter.llm.LlmGateway;
import com.llmrouter.llm.ModelResponse;
import com.llmrouter.metrics.CostCalculator;
import com.llmrouter.metrics.EstimatedCost;
import com.llmrouter.model.ModelCatalog;
import com.llmrouter.model.ModelDefinition;

@Component
public class DirectModelRunner {

    static final String QUALITY_METHOD =
            "Deterministic checklist against expectedCondition. Runtime routing does not use this result.";

    private final EvaluationRunner evaluationRunner;
    private final ModelCatalog modelCatalog;
    private final LlmGateway llmGateway;
    private final CostCalculator costCalculator;
    private final QualityEvaluator qualityEvaluator;
    private final ObjectMapper objectMapper;

    public DirectModelRunner(
            EvaluationRunner evaluationRunner,
            ModelCatalog modelCatalog,
            LlmGateway llmGateway,
            CostCalculator costCalculator,
            QualityEvaluator qualityEvaluator,
            ObjectMapper objectMapper
    ) {
        this.evaluationRunner = evaluationRunner;
        this.modelCatalog = modelCatalog;
        this.llmGateway = llmGateway;
        this.costCalculator = costCalculator;
        this.qualityEvaluator = qualityEvaluator;
        this.objectMapper = objectMapper.copy().enable(SerializationFeature.INDENT_OUTPUT);
    }

    public DirectModelEvaluationReport run() {
        return run(
                evaluationRunner.loadDataset(),
                Path.of("evaluation/dataset.json"),
                modelCatalog.enabledModels(),
                Duration.ofSeconds(5)
        );
    }

    public DirectModelEvaluationReport run(
            EvaluationDataset dataset,
            Path datasetPath,
            List<ModelDefinition> models
    ) {
        return run(dataset, datasetPath, models, Duration.ZERO);
    }

    public DirectModelEvaluationReport run(
            EvaluationDataset dataset,
            Path datasetPath,
            List<ModelDefinition> models,
            Duration pauseBetweenCalls
    ) {
        if (dataset == null || dataset.cases() == null || dataset.cases().isEmpty()) {
            throw new LlmRouterException(ErrorCode.UNKNOWN_ERROR, "Evaluation dataset has no cases");
        }
        if (models == null || models.isEmpty()) {
            throw new LlmRouterException(ErrorCode.MODEL_NOT_FOUND, "No enabled models to evaluate");
        }

        List<DirectModelCaseResult> results = new ArrayList<>();
        boolean first = true;
        for (EvaluationCase evaluationCase : dataset.cases()) {
            for (ModelDefinition model : models) {
                if (!first) {
                    pause(pauseBetweenCalls);
                }
                first = false;
                results.add(run(evaluationCase, model));
            }
        }
        return new DirectModelEvaluationReport(
                "What characteristics do current models show on the same Simple/General/Reasoning dataset?",
                QUALITY_METHOD,
                datasetPath == null ? null : datasetPath.toString(),
                models.stream().map(ModelDefinition::id).toList(),
                results,
                summarize(results)
        );
    }

    public DirectModelCaseResult run(EvaluationCase evaluationCase, ModelDefinition model) {
        long endToEndStart = System.nanoTime();
        try {
            ModelResponse response = llmGateway.complete(model, evaluationCase.input());
            long endToEndLatencyMs = Duration.ofNanos(System.nanoTime() - endToEndStart).toMillis();
            EstimatedCost cost = costCalculator.calculate(model, response.usage());
            QualityResult quality = qualityEvaluator.evaluate(evaluationCase, response.content());
            return new DirectModelCaseResult(
                    evaluationCase.id(),
                    evaluationCase.category(),
                    evaluationCase.input(),
                    model.id(),
                    model.provider(),
                    response.success(),
                    quality.verdict(),
                    quality.reason(),
                    response.content(),
                    response.latencyMs(),
                    endToEndLatencyMs,
                    response.usage() == null ? null : response.usage().inputTokens(),
                    response.usage() == null ? null : response.usage().outputTokens(),
                    cost.displayValue(),
                    response.success() ? null : ErrorCode.PROVIDER_ERROR.name()
            );
        } catch (LlmRouterException exception) {
            long endToEndLatencyMs = Duration.ofNanos(System.nanoTime() - endToEndStart).toMillis();
            return new DirectModelCaseResult(
                    evaluationCase.id(),
                    evaluationCase.category(),
                    evaluationCase.input(),
                    model.id(),
                    model.provider(),
                    false,
                    QualityResult.FAIL,
                    exception.getMessage(),
                    null,
                    null,
                    endToEndLatencyMs,
                    null,
                    null,
                    "UNKNOWN",
                    exception.errorCode().name()
            );
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

    public Path write(DirectModelEvaluationReport report, Path outputPath) {
        try {
            Files.createDirectories(outputPath.getParent());
            objectMapper.writeValue(outputPath.toFile(), report);
            return outputPath;
        } catch (IOException exception) {
            throw new LlmRouterException(ErrorCode.UNKNOWN_ERROR, "Failed to write evaluation results", exception);
        }
    }

    private List<ModelBaselineSummary> summarize(List<DirectModelCaseResult> results) {
        Map<String, List<DirectModelCaseResult>> byModel = new LinkedHashMap<>();
        for (DirectModelCaseResult result : results) {
            byModel.computeIfAbsent(result.model(), key -> new ArrayList<>()).add(result);
        }
        List<ModelBaselineSummary> summaries = new ArrayList<>();
        for (Map.Entry<String, List<DirectModelCaseResult>> entry : byModel.entrySet()) {
            summaries.add(summarizeModel(entry.getValue()));
        }
        return summaries;
    }

    private ModelBaselineSummary summarizeModel(List<DirectModelCaseResult> results) {
        DirectModelCaseResult first = results.getFirst();
        int successCount = 0;
        int qualityPassCount = 0;
        long modelLatencyTotal = 0;
        int modelLatencyCount = 0;
        long endToEndTotal = 0;
        int endToEndCount = 0;
        int inputTokens = 0;
        int outputTokens = 0;
        boolean inputKnown = true;
        boolean outputKnown = true;
        BigDecimal totalCost = BigDecimal.ZERO;
        boolean costUnknown = false;

        for (DirectModelCaseResult result : results) {
            if (result.success()) {
                successCount++;
            }
            if (QualityResult.PASS.equals(result.quality())) {
                qualityPassCount++;
            }
            if (result.modelLatencyMs() != null) {
                modelLatencyTotal += result.modelLatencyMs();
                modelLatencyCount++;
            }
            if (result.endToEndLatencyMs() != null) {
                endToEndTotal += result.endToEndLatencyMs();
                endToEndCount++;
            }
            if (result.inputTokens() == null) {
                inputKnown = false;
            } else {
                inputTokens += result.inputTokens();
            }
            if (result.outputTokens() == null) {
                outputKnown = false;
            } else {
                outputTokens += result.outputTokens();
            }
            if (result.estimatedCost() == null || "UNKNOWN".equals(result.estimatedCost())) {
                costUnknown = true;
            } else {
                totalCost = totalCost.add(new BigDecimal(result.estimatedCost()));
            }
        }

        return new ModelBaselineSummary(
                first.model(),
                first.provider(),
                results.size(),
                successCount,
                qualityPassCount,
                results.isEmpty() ? 0 : (double) qualityPassCount / results.size(),
                modelLatencyCount == 0 ? null : (double) modelLatencyTotal / modelLatencyCount,
                endToEndCount == 0 ? null : (double) endToEndTotal / endToEndCount,
                inputKnown ? inputTokens : null,
                outputKnown ? outputTokens : null,
                costUnknown ? "UNKNOWN" : totalCost.toPlainString()
        );
    }
}
