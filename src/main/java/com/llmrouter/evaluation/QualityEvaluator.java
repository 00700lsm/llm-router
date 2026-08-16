package com.llmrouter.evaluation;

import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class QualityEvaluator {

    private static final Pattern HANGUL = Pattern.compile("[\\uAC00-\\uD7A3]");

    private final ObjectMapper objectMapper;

    public QualityEvaluator(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public QualityResult evaluate(EvaluationCase evaluationCase, String answer) {
        if (answer == null || answer.isBlank()) {
            return QualityResult.fail("answer is blank");
        }

        ExpectedCondition expected = evaluationCase == null ? null : evaluationCase.expectedCondition();
        if (expected == null) {
            return QualityResult.fail("expectedCondition is missing");
        }

        String normalizedAnswer = answer.trim();
        for (String required : expected.mustInclude()) {
            if (!containsIgnoreCase(normalizedAnswer, required)) {
                return QualityResult.fail("missing required text: " + required);
            }
        }
        for (String forbidden : expected.mustNotInclude()) {
            if (containsIgnoreCase(normalizedAnswer, forbidden)) {
                return QualityResult.fail("contains forbidden text: " + forbidden);
            }
        }
        if (expected.requireNoHangulEnabled() && HANGUL.matcher(normalizedAnswer).find()) {
            return QualityResult.fail("answer contains Hangul");
        }
        if (expected.maxChars() != null && normalizedAnswer.length() > expected.maxChars()) {
            return QualityResult.fail("answer exceeds maxChars: " + expected.maxChars());
        }
        if (expected.requireJsonEnabled() && !isJsonObject(normalizedAnswer)) {
            return QualityResult.fail("answer is not a JSON object");
        }
        return QualityResult.pass("checklist requirements satisfied");
    }

    private boolean containsIgnoreCase(String answer, String required) {
        return answer.toLowerCase().contains(required.toLowerCase());
    }

    private boolean isJsonObject(String answer) {
        try {
            JsonNode node = objectMapper.readTree(answer);
            return node != null && node.isObject();
        } catch (JsonProcessingException exception) {
            return false;
        }
    }
}
