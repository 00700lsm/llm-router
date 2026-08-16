package com.llmrouter.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.llmrouter.error.ErrorCode;
import com.llmrouter.error.LlmRouterException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException exception) {
        String message = exception.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(error -> error.getDefaultMessage())
                .orElse("Invalid request");
        return error(HttpStatus.BAD_REQUEST, ErrorCode.INVALID_REQUEST, null, message);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleUnreadable(HttpMessageNotReadableException exception) {
        return error(HttpStatus.BAD_REQUEST, ErrorCode.INVALID_REQUEST, null, "Invalid request");
    }

    @ExceptionHandler(LlmRouterException.class)
    public ResponseEntity<ErrorResponse> handleRouterException(LlmRouterException exception) {
        return error(httpStatus(exception.errorCode()), exception.errorCode(), exception.requestId(), exception.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnknown(Exception exception) {
        log.error("Unhandled error", exception);
        return error(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.UNKNOWN_ERROR, null, "Unexpected error");
    }

    private ResponseEntity<ErrorResponse> error(
            HttpStatus status,
            ErrorCode errorCode,
            String requestId,
            String message
    ) {
        return ResponseEntity.status(status).body(new ErrorResponse(requestId, errorCode.name(), message));
    }

    private HttpStatus httpStatus(ErrorCode errorCode) {
        return switch (errorCode) {
            case INVALID_REQUEST -> HttpStatus.BAD_REQUEST;
            case RATE_LIMIT -> HttpStatus.TOO_MANY_REQUESTS;
            case PROVIDER_ERROR -> HttpStatus.BAD_GATEWAY;
            case PROVIDER_TIMEOUT -> HttpStatus.GATEWAY_TIMEOUT;
            case MODEL_NOT_FOUND, MODEL_DISABLED, UNKNOWN_ERROR -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
    }
}
