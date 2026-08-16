package com.llmrouter.llm;

import org.springframework.stereotype.Component;

import com.llmrouter.model.ModelCatalog;
import com.llmrouter.model.ModelDefinition;
import com.llmrouter.routing.RoutingDecision;

@Component
public class LlmGateway {

    private final ProviderClient providerClient;
    private final ModelCatalog modelCatalog;

    public LlmGateway(ProviderClient providerClient, ModelCatalog modelCatalog) {
        this.providerClient = providerClient;
        this.modelCatalog = modelCatalog;
    }

    public ModelResponse complete(RoutingDecision decision, String message) {
        ModelDefinition model = modelCatalog.getRequired(decision.selectedModel());
        return complete(model, message);
    }

    public ModelResponse complete(ModelDefinition model, String message) {
        return providerClient.complete(model, message);
    }
}
