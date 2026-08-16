package com.llmrouter.evaluation;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

class QualityEvaluatorTest {

    private final QualityEvaluator evaluator = new QualityEvaluator(new ObjectMapper());

    @Test
    void failsWhenAnswerIsBlank() {
        QualityResult result = evaluator.evaluate(caseWith(condition()), "   ");

        assertThat(result.passed()).isFalse();
        assertThat(result.reason()).contains("blank");
    }

    @Test
    void passesWhenChecklistIsSatisfied() {
        ExpectedCondition condition = new ExpectedCondition("PASS", java.util.List.of("REST", "RPC"), null, null, null, null);

        QualityResult result = evaluator.evaluate(
                caseWith(condition),
                "REST는 자원 중심이고 RPC는 프로시저 호출 중심이다."
        );

        assertThat(result.passed()).isTrue();
        assertThat(result.verdict()).isEqualTo(QualityResult.PASS);
    }

    @Test
    void failsWhenRequiredTextIsMissing() {
        ExpectedCondition condition = new ExpectedCondition("PASS", java.util.List.of("RPC"), null, null, null, null);

        QualityResult result = evaluator.evaluate(caseWith(condition), "REST only");

        assertThat(result.passed()).isFalse();
        assertThat(result.reason()).contains("RPC");
    }

    @Test
    void failsWhenHangulIsPresent() {
        ExpectedCondition condition = new ExpectedCondition("PASS", null, null, true, null, null);

        QualityResult result = evaluator.evaluate(caseWith(condition), "오늘 날씨가 좋다. The weather is nice.");

        assertThat(result.passed()).isFalse();
        assertThat(result.reason()).contains("Hangul");
    }

    @Test
    void failsWhenAnswerExceedsMaxChars() {
        ExpectedCondition condition = new ExpectedCondition("PASS", null, null, null, 5, null);

        QualityResult result = evaluator.evaluate(caseWith(condition), "too long");

        assertThat(result.passed()).isFalse();
        assertThat(result.reason()).contains("maxChars");
    }

    @Test
    void passesJsonObjectAndFailsMarkdownFence() {
        ExpectedCondition condition = new ExpectedCondition("PASS", java.util.List.of("30"), null, null, null, true);
        EvaluationCase evaluationCase = caseWith(condition);

        assertThat(evaluator.evaluate(evaluationCase, "{\"name\":\"민수\",\"age\":30}").passed()).isTrue();
        assertThat(evaluator.evaluate(evaluationCase, "```json\n{\"name\":\"민수\",\"age\":30}\n```").passed()).isFalse();
    }

    private EvaluationCase caseWith(ExpectedCondition condition) {
        return new EvaluationCase("case-1", "SIMPLE", "input", condition);
    }

    private ExpectedCondition condition() {
        return new ExpectedCondition("PASS", null, null, null, null, null);
    }
}
