package com.llmrouter.metrics;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.springframework.stereotype.Component;

import com.llmrouter.model.ModelDefinition;

@Component
public class CostCalculator {

    private static final BigDecimal MILLION = new BigDecimal("1000000");
    private static final int SCALE = 8;

    public EstimatedCost calculate(ModelDefinition model, Usage usage) {
        if (model == null
                || usage == null
                || !usage.hasTokenCounts()
                || model.inputCostPerMillion() == null
                || model.outputCostPerMillion() == null) {
            return EstimatedCost.unavailable();
        }

        BigDecimal inputCost = costForTokens(usage.inputTokens(), model.inputCostPerMillion());
        BigDecimal outputCost = costForTokens(usage.outputTokens(), model.outputCostPerMillion());
        return EstimatedCost.of(inputCost.add(outputCost));
    }

    private BigDecimal costForTokens(int tokens, BigDecimal costPerMillion) {
        return BigDecimal.valueOf(tokens)
                .multiply(costPerMillion)
                .divide(MILLION, SCALE, RoundingMode.HALF_UP);
    }
}
