package com.llmrouter.error;

public class LlmRouterException extends RuntimeException {

    private final ErrorCode errorCode;
    private final String requestId;

    public LlmRouterException(ErrorCode errorCode, String message) {
        this(errorCode, message, null, null);
    }

    public LlmRouterException(ErrorCode errorCode, String message, Throwable cause) {
        this(errorCode, message, null, cause);
    }

    public LlmRouterException(ErrorCode errorCode, String message, String requestId) {
        this(errorCode, message, requestId, null);
    }

    public LlmRouterException(ErrorCode errorCode, String message, String requestId, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.requestId = requestId;
    }

    public ErrorCode errorCode() {
        return errorCode;
    }

    public String requestId() {
        return requestId;
    }

    public LlmRouterException withRequestId(String requestId) {
        return new LlmRouterException(errorCode, getMessage(), requestId, getCause());
    }
}
