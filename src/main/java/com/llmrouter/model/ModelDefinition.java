package com.llmrouter.model;

import java.math.BigDecimal;

import com.llmrouter.config.LlmRouterProperties;

public record ModelDefinition(
        String id,
        String provider,
        String displayName,
        String providerModel,
        boolean enabled,
        long contextLimit,
        boolean toolCalling,
        boolean structuredOutput,
        BigDecimal inputCostPerMillion,
        BigDecimal outputCostPerMillion
) {

    public static ModelDefinition from(LlmRouterProperties.Model model) {
        String providerModel = model.getProviderModel() == null || model.getProviderModel().isBlank()
                ? model.getId()
                : model.getProviderModel();
        return new ModelDefinition(
                model.getId(),
                model.getProvider(),
                model.getDisplayName() == null ? model.getId() : model.getDisplayName(),
                providerModel,
                model.isEnabled(),
                model.getContextLimit(),
                model.isToolCalling(),
                model.isStructuredOutput(),
                model.getInputCostPerMillion(),
                model.getOutputCostPerMillion()
        );
    }
}
