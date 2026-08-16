package com.llmrouter.llm;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.RestClient;

import com.llmrouter.TestModels;
import com.llmrouter.error.ErrorCode;
import com.llmrouter.error.LlmRouterException;
import com.llmrouter.metrics.Usage;

class ProviderClientTest {

    private final ProviderClient client = new ProviderClient(RestClient.create(), "test-key");

    @Test
    void mapsProviderResponseToModelResponse() {
        GeminiGenerateContent.Response body = new GeminiGenerateContent.Response(
                List.of(new GeminiGenerateContent.Candidate(
                        new GeminiGenerateContent.Content("model", List.of(new GeminiGenerateContent.Part("hello")))
                )),
                new GeminiGenerateContent.UsageMetadata(12, 24, 36)
        );

        ModelResponse response = client.toModelResponse(body, TestModels.small(), 820);

        assertThat(response.success()).isTrue();
        assertThat(response.content()).isEqualTo("hello");
        assertThat(response.model()).isEqualTo("model-small");
        assertThat(response.provider()).isEqualTo("GEMINI");
        assertThat(response.latencyMs()).isEqualTo(820);
        assertThat(response.usage()).isEqualTo(new Usage(12, 24, 36));
    }

    @Test
    void mapsMissingUsageToUnknownUsage() {
        GeminiGenerateContent.Response body = new GeminiGenerateContent.Response(
                List.of(new GeminiGenerateContent.Candidate(
                        new GeminiGenerateContent.Content("model", List.of(new GeminiGenerateContent.Part("hello")))
                )),
                null
        );

        ModelResponse response = client.toModelResponse(body, TestModels.small(), 10);

        assertThat(response.usage()).isEqualTo(Usage.unknown());
    }

    @Test
    void failsWhenProviderResponseHasNoCandidates() {
        GeminiGenerateContent.Response body = new GeminiGenerateContent.Response(List.of(), null);

        assertThatThrownBy(() -> client.toModelResponse(body, TestModels.small(), 10))
                .isInstanceOf(LlmRouterException.class)
                .extracting(exception -> ((LlmRouterException) exception).errorCode())
                .isEqualTo(ErrorCode.PROVIDER_ERROR);
    }

    @Test
    void mapsHttpStatusToErrorCode() {
        assertThat(client.errorCodeFor(HttpStatus.TOO_MANY_REQUESTS)).isEqualTo(ErrorCode.RATE_LIMIT);
        assertThat(client.errorCodeFor(HttpStatus.GATEWAY_TIMEOUT)).isEqualTo(ErrorCode.PROVIDER_TIMEOUT);
        assertThat(client.errorCodeFor(HttpStatus.BAD_GATEWAY)).isEqualTo(ErrorCode.PROVIDER_ERROR);
    }
}
