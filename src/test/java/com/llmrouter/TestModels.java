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
                "GEMINI",
                "Gemini 2.5 Pro",
                "gemini-2.5-pro",
                true,
                1048576,
                true,
                true,
                new BigDecimal("1.25"),
                new BigDecimal("10.00")
        );
    }

    public static ModelDefinition disabledSmall() {
        return model("model-small", false);
    }

    private static ModelDefinition model(String id, boolean enabled) {
        return new ModelDefinition(
                id,
                "GEMINI",
                "Gemini 2.5 Flash",
                "gemini-2.5-flash",
                enabled,
                1048576,
                true,
                true,
                new BigDecimal("0.15"),
                new BigDecimal("0.60")
        );
    }
}
