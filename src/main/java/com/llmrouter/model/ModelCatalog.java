package com.llmrouter.model;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.llmrouter.config.LlmRouterProperties;
import com.llmrouter.error.ErrorCode;
import com.llmrouter.error.LlmRouterException;

@Component
public class ModelCatalog {

    private final Map<String, ModelDefinition> modelsById;
    private final String defaultModelId;

    @Autowired
    public ModelCatalog(LlmRouterProperties properties) {
        this(properties.getModels().stream().map(ModelDefinition::from).toList(),
                properties.getRouting().getDefaultModel());
    }

    public ModelCatalog(List<ModelDefinition> models, String defaultModelId) {
        this.modelsById = new LinkedHashMap<>();
        for (ModelDefinition model : models) {
            this.modelsById.put(model.id(), model);
        }
        this.defaultModelId = defaultModelId;
    }

    public List<ModelDefinition> list() {
        return List.copyOf(modelsById.values());
    }

    public List<ModelDefinition> enabledModels() {
        return list().stream().filter(ModelDefinition::enabled).toList();
    }

    public Optional<ModelDefinition> findById(String id) {
        return Optional.ofNullable(modelsById.get(id));
    }

    public ModelDefinition getRequired(String id) {
        return findById(id).orElseThrow(() ->
                new LlmRouterException(ErrorCode.MODEL_NOT_FOUND, "Model not found: " + id));
    }

    public String defaultModelId() {
        return defaultModelId;
    }

    public ModelDefinition defaultModel() {
        if (defaultModelId == null || defaultModelId.isBlank()) {
            throw new LlmRouterException(ErrorCode.MODEL_NOT_FOUND, "Default model is not configured");
        }
        ModelDefinition model = getRequired(defaultModelId);
        if (!model.enabled()) {
            throw new LlmRouterException(
                    ErrorCode.MODEL_DISABLED,
                    "Default model is disabled: " + defaultModelId
            );
        }
        return model;
    }
}
