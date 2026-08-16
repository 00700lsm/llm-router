package com.llmrouter.routing;

import org.springframework.stereotype.Component;

import com.llmrouter.model.ModelCatalog;
import com.llmrouter.model.ModelDefinition;

@Component
public class BaselineRouter {

    private final ModelCatalog modelCatalog;

    public BaselineRouter(ModelCatalog modelCatalog) {
        this.modelCatalog = modelCatalog;
    }

    public RoutingDecision route(String requestId, String message) {
        if (message == null) {
            throw new IllegalArgumentException("message must not be null");
        }
        ModelDefinition selected = modelCatalog.defaultModel();
        return new RoutingDecision(
                requestId,
                selected.id(),
                selected.provider(),
                RoutingStrategy.BASELINE_DEFAULT,
                "configured default model"
        );
    }
}
