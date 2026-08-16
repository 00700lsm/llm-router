package com.llmrouter.llm;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

public final class GeminiGenerateContent {

    private GeminiGenerateContent() {
    }

    public record Request(List<Content> contents) {
        public static Request fromUserMessage(String message) {
            return new Request(List.of(new Content("user", List.of(new Part(message)))));
        }
    }

    public record Content(String role, List<Part> parts) {
    }

    public record Part(String text) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Response(List<Candidate> candidates, UsageMetadata usageMetadata) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Candidate(Content content) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record UsageMetadata(
            Integer promptTokenCount,
            Integer candidatesTokenCount,
            Integer totalTokenCount
    ) {
    }
}
