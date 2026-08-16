package com.llmrouter.metrics;

import java.math.BigDecimal;

public record EstimatedCost(BigDecimal amount, boolean unknown) {

    public static EstimatedCost unavailable() {
        return new EstimatedCost(null, true);
    }

    public static EstimatedCost of(BigDecimal amount) {
        return new EstimatedCost(amount, false);
    }

    public String displayValue() {
        if (unknown || amount == null) {
            return "UNKNOWN";
        }
        return amount.toPlainString();
    }
}
