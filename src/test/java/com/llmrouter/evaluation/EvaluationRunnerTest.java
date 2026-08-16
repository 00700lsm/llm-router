package com.llmrouter.evaluation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.nio.file.Path;
import java.time.Duration;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.llmrouter.TestModels;
import com.llmrouter.llm.ModelResponse;
import com.llmrouter.llm.ProviderClient;
import com.llmrouter.metrics.Usage;
import com.llmrouter.routing.RoutingStrategy;

@SpringBootTest
class EvaluationRunnerTest {

    @Autowired
    private EvaluationRunner evaluationRunner;

    @MockitoBean
    private ProviderClient providerClient;

    @Test
    void loadsDatasetAndRunsOneCaseThroughRouter() {
        when(providerClient.complete(any(), anyString()))
                .thenReturn(ModelResponse.success(
                        "REST is resource oriented and RPC is procedure oriented.",
                        TestModels.small().id(),
                        TestModels.small().provider(),
                        new Usage(10, 20, 30),
                        50
                ));

        EvaluationCase evaluationCase = new EvaluationCase(
                "general-001",
                "GENERAL",
                "REST와 RPC 차이를 설명해줘.",
                new ExpectedCondition("PASS", List.of("REST", "RPC"), null, null, null, null)
        );
        BaselineRoutingEvaluationReport report = evaluationRunner.run(
                new EvaluationDataset(List.of(evaluationCase)),
                Path.of("evaluation/dataset.json"),
                Duration.ZERO
        );

        assertThat(report.results()).hasSize(1);
        EvaluationResult result = report.results().getFirst();
        assertThat(result.caseId()).isEqualTo("general-001");
        assertThat(result.selectedModel()).isEqualTo("model-small");
        assertThat(result.strategy()).isEqualTo(RoutingStrategy.BASELINE_DEFAULT.name());
        assertThat(result.reason()).isEqualTo("configured default model");
        assertThat(result.success()).isTrue();
        assertThat(result.quality()).isEqualTo(QualityResult.PASS);
        assertThat(result.inputTokens()).isEqualTo(10);
        assertThat(result.outputTokens()).isEqualTo(20);
        assertThat(result.estimatedCost()).isNotBlank();
        assertThat(report.failures()).isEmpty();
        assertThat(report.summary().selectedModelCounts()).containsEntry("model-small", 1L);
    }

    @Test
    void recordsQualityFailureWithoutChangingRoute() {
        when(providerClient.complete(any(), anyString()))
                .thenReturn(ModelResponse.success(
                        "```json\n{\"이름\":\"민수\",\"나이\":30}\n```",
                        TestModels.small().id(),
                        TestModels.small().provider(),
                        new Usage(8, 12, 20),
                        40
                ));

        EvaluationCase evaluationCase = new EvaluationCase(
                "simple-003",
                "SIMPLE",
                "JSON만 출력해",
                new ExpectedCondition("PASS", List.of("30"), null, null, null, true)
        );
        BaselineRoutingEvaluationReport report = evaluationRunner.run(
                new EvaluationDataset(List.of(evaluationCase)),
                Path.of("evaluation/dataset.json"),
                Duration.ZERO
        );

        EvaluationResult result = report.results().getFirst();
        assertThat(result.selectedModel()).isEqualTo("model-small");
        assertThat(result.strategy()).isEqualTo(RoutingStrategy.BASELINE_DEFAULT.name());
        assertThat(result.success()).isTrue();
        assertThat(result.quality()).isEqualTo(QualityResult.FAIL);
        assertThat(result.qualityReason()).contains("JSON");
        assertThat(report.failures()).extracting(EvaluationResult::caseId).containsExactly("simple-003");
    }
}
