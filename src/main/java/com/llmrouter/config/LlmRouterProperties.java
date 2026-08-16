package com.llmrouter.config;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "llm-router")
public class LlmRouterProperties {

    private Routing routing = new Routing();
    private Provider provider = new Provider();
    private List<Model> models = new ArrayList<>();

    public Routing getRouting() {
        return routing;
    }

    public void setRouting(Routing routing) {
        this.routing = routing == null ? new Routing() : routing;
    }

    public Provider getProvider() {
        return provider;
    }

    public void setProvider(Provider provider) {
        this.provider = provider == null ? new Provider() : provider;
    }

    public List<Model> getModels() {
        return models;
    }

    public void setModels(List<Model> models) {
        this.models = models == null ? new ArrayList<>() : models;
    }

    public static class Routing {
        private String defaultModel;

        public String getDefaultModel() {
            return defaultModel;
        }

        public void setDefaultModel(String defaultModel) {
            this.defaultModel = defaultModel;
        }
    }

    public static class Provider {
        private String baseUrl = "https://generativelanguage.googleapis.com/v1beta";
        private String apiKey = "";

        public String getBaseUrl() {
            return baseUrl;
        }

        public void setBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl == null || baseUrl.isBlank()
                    ? "https://generativelanguage.googleapis.com/v1beta"
                    : baseUrl;
        }

        public String getApiKey() {
            return apiKey;
        }

        public void setApiKey(String apiKey) {
            this.apiKey = apiKey == null ? "" : apiKey;
        }
    }

    public static class Model {
        private String id;
        private String provider;
        private String displayName;
        private String providerModel;
        private boolean enabled;
        private long contextLimit;
        private boolean toolCalling;
        private boolean structuredOutput;
        private BigDecimal inputCostPerMillion;
        private BigDecimal outputCostPerMillion;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getProvider() {
            return provider;
        }

        public void setProvider(String provider) {
            this.provider = provider;
        }

        public String getDisplayName() {
            return displayName;
        }

        public void setDisplayName(String displayName) {
            this.displayName = displayName;
        }

        public String getProviderModel() {
            return providerModel;
        }

        public void setProviderModel(String providerModel) {
            this.providerModel = providerModel;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public long getContextLimit() {
            return contextLimit;
        }

        public void setContextLimit(long contextLimit) {
            this.contextLimit = contextLimit;
        }

        public boolean isToolCalling() {
            return toolCalling;
        }

        public void setToolCalling(boolean toolCalling) {
            this.toolCalling = toolCalling;
        }

        public boolean isStructuredOutput() {
            return structuredOutput;
        }

        public void setStructuredOutput(boolean structuredOutput) {
            this.structuredOutput = structuredOutput;
        }

        public BigDecimal getInputCostPerMillion() {
            return inputCostPerMillion;
        }

        public void setInputCostPerMillion(BigDecimal inputCostPerMillion) {
            this.inputCostPerMillion = inputCostPerMillion;
        }

        public BigDecimal getOutputCostPerMillion() {
            return outputCostPerMillion;
        }

        public void setOutputCostPerMillion(BigDecimal outputCostPerMillion) {
            this.outputCostPerMillion = outputCostPerMillion;
        }
    }
}
