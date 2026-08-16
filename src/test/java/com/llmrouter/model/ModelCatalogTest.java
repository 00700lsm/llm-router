package com.llmrouter.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.llmrouter.TestModels;
import com.llmrouter.error.ErrorCode;
import com.llmrouter.error.LlmRouterException;

class ModelCatalogTest {

    @Test
    void listsConfiguredModels() {
        ModelCatalog catalog = new ModelCatalog(List.of(TestModels.small(), TestModels.large()), "model-small");

        assertThat(catalog.list()).hasSize(2);
        assertThat(catalog.findById("model-small")).isPresent();
        assertThat(catalog.findById("model-large")).isPresent();
        assertThat(catalog.findById("model-small").orElseThrow().enabled()).isTrue();
    }

    @Test
    void returnsDefaultModelWhenEnabled() {
        ModelCatalog catalog = new ModelCatalog(List.of(TestModels.small(), TestModels.large()), "model-small");

        assertThat(catalog.defaultModel().id()).isEqualTo("model-small");
    }

    @Test
    void distinguishesDisabledModels() {
        ModelCatalog catalog = new ModelCatalog(List.of(TestModels.disabledSmall(), TestModels.large()), "model-small");

        assertThat(catalog.getRequired("model-small").enabled()).isFalse();
        assertThatThrownBy(catalog::defaultModel)
                .isInstanceOf(LlmRouterException.class)
                .extracting(exception -> ((LlmRouterException) exception).errorCode())
                .isEqualTo(ErrorCode.MODEL_DISABLED);
    }

    @Test
    void failsWhenDefaultModelIsMissing() {
        ModelCatalog catalog = new ModelCatalog(List.of(TestModels.large()), "model-small");

        assertThatThrownBy(catalog::defaultModel)
                .isInstanceOf(LlmRouterException.class)
                .extracting(exception -> ((LlmRouterException) exception).errorCode())
                .isEqualTo(ErrorCode.MODEL_NOT_FOUND);
    }
}
