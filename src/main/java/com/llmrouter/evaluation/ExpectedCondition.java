package com.llmrouter.evaluation;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ExpectedCondition(
        String minimumQuality,
        List<String> mustInclude,
        List<String> mustNotInclude,
        Boolean requireNoHangul,
        Integer maxChars,
        Boolean requireJson
) {

    public ExpectedCondition {
        mustInclude = mustInclude == null ? List.of() : List.copyOf(mustInclude);
        mustNotInclude = mustNotInclude == null ? List.of() : List.copyOf(mustNotInclude);
    }

    public boolean requireNoHangulEnabled() {
        return Boolean.TRUE.equals(requireNoHangul);
    }

    public boolean requireJsonEnabled() {
        return Boolean.TRUE.equals(requireJson);
    }
}
