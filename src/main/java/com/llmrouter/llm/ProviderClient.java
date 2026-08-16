package com.llmrouter.llm;

import java.net.http.HttpTimeoutException;
import java.time.Duration;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import com.llmrouter.config.LlmRouterProperties;
import com.llmrouter.error.ErrorCode;
import com.llmrouter.error.LlmRouterException;
import com.llmrouter.metrics.Usage;
import com.llmrouter.model.ModelDefinition;

@Component
public class ProviderClient {

    private final RestClient restClient;
    private final String apiKey;

    @Autowired
    public ProviderClient(RestClient.Builder restClientBuilder, LlmRouterProperties properties) {
        this.apiKey = properties.getProvider().getApiKey();
        this.restClient = restClientBuilder
                .baseUrl(properties.getProvider().getBaseUrl())
                .build();
    }

    ProviderClient(RestClient restClient, String apiKey) {
        this.restClient = restClient;
        this.apiKey = apiKey;
    }

    public ModelResponse complete(ModelDefinition model, String message) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new LlmRouterException(ErrorCode.PROVIDER_ERROR, "Provider API key is not configured");
        }

        long startNanos = System.nanoTime();
        try {
            OpenAiChatCompletions.Response body = restClient.post()
                    .uri("/chat/completions")
                    .header("Authorization", "Bearer " + apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .body(new OpenAiChatCompletions.Request(
                            model.providerModel(),
                            List.of(new OpenAiChatCompletions.Message("user", message))
                    ))
                    .retrieve()
                    .body(OpenAiChatCompletions.Response.class);
            long latencyMs = Duration.ofNanos(System.nanoTime() - startNanos).toMillis();
            return toModelResponse(body, model, latencyMs);
        } catch (LlmRouterException exception) {
            throw exception;
        } catch (RestClientResponseException exception) {
            throw new LlmRouterException(
                    errorCodeFor(exception.getStatusCode()),
                    "Provider request failed",
                    exception
            );
        } catch (ResourceAccessException exception) {
            ErrorCode errorCode = isTimeout(exception) ? ErrorCode.PROVIDER_TIMEOUT : ErrorCode.PROVIDER_ERROR;
            throw new LlmRouterException(errorCode, "Provider request failed", exception);
        } catch (Exception exception) {
            throw new LlmRouterException(ErrorCode.PROVIDER_ERROR, "Provider request failed", exception);
        }
    }

    ModelResponse toModelResponse(OpenAiChatCompletions.Response body, ModelDefinition model, long latencyMs) {
        if (body == null || body.choices() == null || body.choices().isEmpty()
                || body.choices().getFirst() == null
                || body.choices().getFirst().message() == null) {
            throw new LlmRouterException(ErrorCode.PROVIDER_ERROR, "Provider returned an empty response");
        }

        String content = body.choices().getFirst().message().content();
        Usage usage = toUsage(body.usage());
        return ModelResponse.success(content, model.id(), model.provider(), usage, latencyMs);
    }

    ErrorCode errorCodeFor(HttpStatusCode statusCode) {
        if (statusCode.value() == 429) {
            return ErrorCode.RATE_LIMIT;
        }
        if (statusCode.value() == 408 || statusCode.value() == 504) {
            return ErrorCode.PROVIDER_TIMEOUT;
        }
        return ErrorCode.PROVIDER_ERROR;
    }

    private Usage toUsage(OpenAiChatCompletions.Usage providerUsage) {
        if (providerUsage == null) {
            return Usage.unknown();
        }
        return new Usage(
                providerUsage.promptTokens(),
                providerUsage.completionTokens(),
                providerUsage.totalTokens()
        );
    }

    private boolean isTimeout(ResourceAccessException exception) {
        Throwable current = exception;
        while (current != null) {
            if (current instanceof HttpTimeoutException || current instanceof java.net.SocketTimeoutException) {
                return true;
            }
            current = current.getCause();
        }
        return exception.getMessage() != null && exception.getMessage().toLowerCase().contains("timed out");
    }
}
