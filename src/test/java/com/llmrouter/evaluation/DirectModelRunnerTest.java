package com.llmrouter.evaluation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.llmrouter.TestModels;
import com.llmrouter.llm.ModelResponse;
import com.llmrouter.llm.ProviderClient;
import com.llmrouter.metrics.Usage;
import com.llmrouter.model.ModelDefinition;

@SpringBootTest
class DirectModelRunnerTest {

    @Autowired
    private DirectModelRunner directModelRunner;

    @MockitoBean
    private ProviderClient providerClient;

    @Test
    void runsSameCaseAgainstEachModelWithoutRouter() {
        when(providerClient.complete(any(ModelDefinition.class), anyString()))
                .thenAnswer(invocation -> {
                    ModelDefinition model = invocation.getArgument(0);
                    return ModelResponse.success(
                            "REST and RPC both describe remote communication.",
                            model.id(),
                            model.provider(),
                            new Usage(11, 22, 33),
                            40
                    );
                });

        EvaluationCase evaluationCase = new EvaluationCase(
                "general-001",
                "GENERAL",
                "REST와 RPC 차이를 설명해줘.",
                new ExpectedCondition("PASS", List.of("REST", "RPC"), null, null, null, null)
        );
        DirectModelEvaluationReport report = directModelRunner.run(
                new EvaluationDataset(List.of(evaluationCase)),
                Path.of("evaluation/dataset.json"),
                List.of(TestModels.small(), TestModels.large())
        );

        assertThat(report.results()).hasSize(2);
        assertThat(report.models()).containsExactly("model-small", "model-large");
        assertThat(report.results()).allSatisfy(result -> {
            assertThat(result.success()).isTrue();
            assertThat(result.quality()).isEqualTo(QualityResult.PASS);
            assertThat(result.inputTokens()).isEqualTo(11);
            assertThat(result.outputTokens()).isEqualTo(22);
            assertThat(result.estimatedCost()).isNotEqualTo("UNKNOWN");
        });
        assertThat(report.summaries()).hasSize(2);
        assertThat(report.qualityMethod()).contains("checklist");
    }
}
