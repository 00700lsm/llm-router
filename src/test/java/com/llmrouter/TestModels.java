package com.llmrouter;

import java.math.BigDecimal;

import com.llmrouter.model.ModelDefinition;

public final class TestModels {

    private TestModels() {
    }

    public static ModelDefinition small() {
        return model("model-small", true);
    }

    public static ModelDefinition large() {
        return new ModelDefinition(
                "model-large",
                "OPENAI",
                "GPT-4o",
                "gpt-4o",
                true,
                128000,
                true,
                true,
                new BigDecimal("2.50"),
                new BigDecimal("10.00")
        );
    }

    public static ModelDefinition disabledSmall() {
        return model("model-small", false);
    }

    private static ModelDefinition model(String id, boolean enabled) {
        return new ModelDefinition(
                id,
                "OPENAI",
                "GPT-4o mini",
                "gpt-4o-mini",
                enabled,
                128000,
                true,
                true,
                new BigDecimal("0.15"),
                new BigDecimal("0.60")
        );
    }
}
