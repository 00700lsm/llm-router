package com.llmrouter.evaluation;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.llmrouter.chat.ChatExecutionResult;
import com.llmrouter.chat.ChatService;
import com.llmrouter.error.ErrorCode;
import com.llmrouter.error.LlmRouterException;

@Component
public class EvaluationRunner {

    private static final Path DEFAULT_DATASET_PATH = Path.of("evaluation/dataset.json");

    private final ChatService chatService;
    private final ObjectMapper objectMapper;

    public EvaluationRunner(ChatService chatService, ObjectMapper objectMapper) {
        this.chatService = chatService;
        this.objectMapper = objectMapper;
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

    public List<EvaluationResult> runAll() {
        return run(loadDataset());
    }

    public List<EvaluationResult> run(EvaluationDataset dataset) {
        if (dataset == null || dataset.cases() == null || dataset.cases().isEmpty()) {
            throw new LlmRouterException(ErrorCode.UNKNOWN_ERROR, "Evaluation dataset has no cases");
        }
        List<EvaluationResult> results = new ArrayList<>();
        for (EvaluationCase evaluationCase : dataset.cases()) {
            results.add(run(evaluationCase));
        }
        return results;
    }

    public EvaluationResult run(EvaluationCase evaluationCase) {
        try {
            ChatExecutionResult execution = chatService.chat(evaluationCase.input());
            return new EvaluationResult(
                    evaluationCase.id(),
                    evaluationCase.category(),
                    evaluationCase.input(),
                    execution.decision().selectedModel(),
                    execution.decision().provider(),
                    execution.decision().strategy().name(),
                    execution.decision().reason(),
                    execution.success(),
                    execution.modelLatencyMs(),
                    execution.endToEndLatencyMs(),
                    execution.usage() == null ? null : execution.usage().inputTokens(),
                    execution.usage() == null ? null : execution.usage().outputTokens(),
                    execution.cost() == null ? "UNKNOWN" : execution.cost().displayValue(),
                    execution.errorCode()
            );
        } catch (LlmRouterException exception) {
            return new EvaluationResult(
                    evaluationCase.id(),
                    evaluationCase.category(),
                    evaluationCase.input(),
                    null,
                    null,
                    null,
                    null,
                    false,
                    null,
                    null,
                    null,
                    null,
                    "UNKNOWN",
                    exception.errorCode().name()
            );
        }
    }
}
