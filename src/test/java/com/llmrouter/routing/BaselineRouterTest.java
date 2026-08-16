package com.llmrouter.routing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.llmrouter.TestModels;
import com.llmrouter.error.ErrorCode;
import com.llmrouter.error.LlmRouterException;
import com.llmrouter.model.ModelCatalog;

class BaselineRouterTest {

    @Test
    void selectsConfiguredDefaultModel() {
        ModelCatalog catalog = new ModelCatalog(List.of(TestModels.small(), TestModels.large()), "model-small");
        BaselineRouter router = new BaselineRouter(catalog);

        RoutingDecision decision = router.route("req-1", "Java에서 volatile이 필요한 이유를 설명해줘.");

        assertThat(decision.requestId()).isEqualTo("req-1");
        assertThat(decision.selectedModel()).isEqualTo("model-small");
        assertThat(decision.provider()).isEqualTo("OPENAI");
        assertThat(decision.strategy()).isEqualTo(RoutingStrategy.BASELINE_DEFAULT);
        assertThat(decision.reason()).isEqualTo("configured default model");
    }

    @Test
    void failsWhenDefaultModelIsDisabled() {
        ModelCatalog catalog = new ModelCatalog(List.of(TestModels.disabledSmall(), TestModels.large()), "model-small");
        BaselineRouter router = new BaselineRouter(catalog);

        assertThatThrownBy(() -> router.route("req-1", "hello"))
                .isInstanceOf(LlmRouterException.class)
                .extracting(exception -> ((LlmRouterException) exception).errorCode())
                .isEqualTo(ErrorCode.MODEL_DISABLED);
    }

    @Test
    void failsWhenDefaultModelIsNotInCatalog() {
        ModelCatalog catalog = new ModelCatalog(List.of(TestModels.large()), "model-small");
        BaselineRouter router = new BaselineRouter(catalog);

        assertThatThrownBy(() -> router.route("req-1", "hello"))
                .isInstanceOf(LlmRouterException.class)
                .extracting(exception -> ((LlmRouterException) exception).errorCode())
                .isEqualTo(ErrorCode.MODEL_NOT_FOUND);
    }

    @Test
    void failsWhenDefaultModelIsNotConfigured() {
        ModelCatalog catalog = new ModelCatalog(List.of(TestModels.small()), null);
        BaselineRouter router = new BaselineRouter(catalog);

        assertThatThrownBy(() -> router.route("req-1", "hello"))
                .isInstanceOf(LlmRouterException.class)
                .extracting(exception -> ((LlmRouterException) exception).errorCode())
                .isEqualTo(ErrorCode.MODEL_NOT_FOUND);
    }
}
