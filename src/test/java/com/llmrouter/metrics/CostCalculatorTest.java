package com.llmrouter.metrics;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import com.llmrouter.TestModels;

class CostCalculatorTest {

    private final CostCalculator calculator = new CostCalculator();

    @Test
    void calculatesEstimatedCostFromTokenUsage() {
        Usage usage = new Usage(1000, 500, 1500);

        EstimatedCost cost = calculator.calculate(TestModels.small(), usage);

        assertThat(cost.unknown()).isFalse();
        assertThat(cost.amount()).isEqualByComparingTo(new BigDecimal("0.00045000"));
        assertThat(cost.displayValue()).isEqualTo("0.00045000");
    }

    @Test
    void returnsUnknownWhenUsageIsMissing() {
        EstimatedCost cost = calculator.calculate(TestModels.small(), Usage.unknown());

        assertThat(cost.unknown()).isTrue();
        assertThat(cost.displayValue()).isEqualTo("UNKNOWN");
    }

    @Test
    void returnsUnknownWhenTokenCountsAreIncomplete() {
        EstimatedCost cost = calculator.calculate(TestModels.small(), new Usage(100, null, null));

        assertThat(cost.unknown()).isTrue();
    }
}
