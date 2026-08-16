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
                        "REST는 자원 중심이고 RPC는 프로시저 호출 중심이다.",
                        TestModels.small().id(),
                        TestModels.small().provider(),
                        new Usage(10, 20, 30),
                        50
                ));

        EvaluationDataset dataset = evaluationRunner.loadDataset(Path.of("evaluation/dataset.json"));
        assertThat(dataset.cases()).isNotEmpty();

        EvaluationCase firstCase = dataset.cases().getFirst();
        List<EvaluationResult> results = evaluationRunner.run(new EvaluationDataset(List.of(firstCase)));

        assertThat(results).hasSize(1);
        EvaluationResult result = results.getFirst();
        assertThat(result.caseId()).isEqualTo(firstCase.id());
        assertThat(result.selectedModel()).isEqualTo("model-small");
        assertThat(result.strategy()).isEqualTo(RoutingStrategy.BASELINE_DEFAULT.name());
        assertThat(result.reason()).isEqualTo("configured default model");
        assertThat(result.success()).isTrue();
        assertThat(result.inputTokens()).isEqualTo(10);
        assertThat(result.outputTokens()).isEqualTo(20);
        assertThat(result.estimatedCost()).isNotBlank();
    }
}
