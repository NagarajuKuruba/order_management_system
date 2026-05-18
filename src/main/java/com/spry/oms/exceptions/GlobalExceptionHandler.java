package com.spry.oms.exceptions;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * Handle custom ApplicationException and its subclasses
     */
    @ExceptionHandler(ApplicationException.class)
    public ResponseEntity<ErrorResponse> handleApplicationException(
            ApplicationException ex,
            WebRequest request) {

        String traceId = generateTraceId();
        String path = request.getDescription(false).replace("uri=", "");

        log.error("Application exception occurred [TraceId: {}] - ErrorCode: {}, Message: {}",
                traceId, ex.getErrorCode(), ex.getMessage(), ex);

        ErrorResponse errorResponse = ErrorResponse.builder()
                .errorCode(ex.getErrorCode())
                .status(ex.getHttpStatus().value())
                .message(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .path(path)
                .traceId(traceId)
                .build();

        return new ResponseEntity<>(errorResponse, ex.getHttpStatus());
    }

    /**
     * Handle ResourceNotFoundException
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(
            ResourceNotFoundException ex,
            WebRequest request) {

        String traceId = generateTraceId();
        String path = request.getDescription(false).replace("uri=", "");

        log.warn("Resource not found [TraceId: {}] - Message: {}", traceId, ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.builder()
                .errorCode(ex.getErrorCode())
                .status(HttpStatus.NOT_FOUND.value())
                .message(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .path(path)
                .traceId(traceId)
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    /**
     * Handle ValidationException
     */
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            ValidationException ex,
            WebRequest request) {

        String traceId = generateTraceId();
        String path = request.getDescription(false).replace("uri=", "");

        log.warn("Validation exception [TraceId: {}] - Message: {}", traceId, ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.builder()
                .errorCode(ex.getErrorCode())
                .status(HttpStatus.BAD_REQUEST.value())
                .message(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .path(path)
                .traceId(traceId)
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle InvalidOperationException
     */
    @ExceptionHandler(InvalidOperationException.class)
    public ResponseEntity<ErrorResponse> handleInvalidOperation(
            InvalidOperationException ex,
            WebRequest request) {

        String traceId = generateTraceId();
        String path = request.getDescription(false).replace("uri=", "");

        log.warn("Invalid operation [TraceId: {}] - ErrorCode: {}, Message: {}",
                traceId, ex.getErrorCode(), ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.builder()
                .errorCode(ex.getErrorCode())
                .status(HttpStatus.BAD_REQUEST.value())
                .message(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .path(path)
                .traceId(traceId)
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle BusinessRuleViolationException
     */
    @ExceptionHandler(BusinessRuleViolationException.class)
    public ResponseEntity<ErrorResponse> handleBusinessRuleViolation(
            BusinessRuleViolationException ex,
            WebRequest request) {

        String traceId = generateTraceId();
        String path = request.getDescription(false).replace("uri=", "");

        log.warn("Business rule violation [TraceId: {}] - ErrorCode: {}, Message: {}",
                traceId, ex.getErrorCode(), ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.builder()
                .errorCode(ex.getErrorCode())
                .status(HttpStatus.UNPROCESSABLE_ENTITY.value())
                .message(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .path(path)
                .traceId(traceId)
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.UNPROCESSABLE_ENTITY);
    }

    /**
     * Handle ConflictException
     */
    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ErrorResponse> handleConflict(
            ConflictException ex,
            WebRequest request) {

        String traceId = generateTraceId();
        String path = request.getDescription(false).replace("uri=", "");

        log.warn("Conflict exception [TraceId: {}] - ErrorCode: {}, Message: {}",
                traceId, ex.getErrorCode(), ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.builder()
                .errorCode(ex.getErrorCode())
                .status(HttpStatus.CONFLICT.value())
                .message(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .path(path)
                .traceId(traceId)
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }

    /**
     * Handle MessagingException
     */
    @ExceptionHandler(MessagingException.class)
    public ResponseEntity<ErrorResponse> handleMessagingException(
            MessagingException ex,
            WebRequest request) {

        String traceId = generateTraceId();
        String path = request.getDescription(false).replace("uri=", "");

        log.error("Messaging exception [TraceId: {}] - ErrorCode: {}, Message: {}",
                traceId, ex.getErrorCode(), ex.getMessage(), ex);

        ErrorResponse errorResponse = ErrorResponse.builder()
                .errorCode(ex.getErrorCode())
                .status(HttpStatus.SERVICE_UNAVAILABLE.value())
                .message(ex.getMessage())
                .details("A messaging service error occurred. Please try again later.")
                .timestamp(LocalDateTime.now())
                .path(path)
                .traceId(traceId)
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.SERVICE_UNAVAILABLE);
    }

    /**
     * Handle DataAccessException
     */
    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ErrorResponse> handleDataAccessException(
            DataAccessException ex,
            WebRequest request) {

        String traceId = generateTraceId();
        String path = request.getDescription(false).replace("uri=", "");

        log.error("Data access exception [TraceId: {}] - ErrorCode: {}, Message: {}",
                traceId, ex.getErrorCode(), ex.getMessage(), ex);

        ErrorResponse errorResponse = ErrorResponse.builder()
                .errorCode(ex.getErrorCode())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .message(ex.getMessage())
                .details("A database error occurred. Please try again later.")
                .timestamp(LocalDateTime.now())
                .path(path)
                .traceId(traceId)
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Handle Spring validation errors (MethodArgumentNotValidException)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(
            MethodArgumentNotValidException ex,
            WebRequest request) {

        String traceId = generateTraceId();
        String path = request.getDescription(false).replace("uri=", "");
        Map<String, String> validationErrors = new HashMap<>();

        log.warn("Validation error occurred [TraceId: {}] - Total errors: {}",
                traceId, ex.getBindingResult().getErrorCount());

        ex.getBindingResult()
                .getFieldErrors()
                .forEach(error -> {
                    log.debug("Field validation error - Field: {}, Message: {}, TraceId: {}",
                            error.getField(), error.getDefaultMessage(), traceId);
                    validationErrors.put(
                            error.getField(),
                            error.getDefaultMessage()
                    );
                });

        log.debug("Returning validation errors response [TraceId: {}]", traceId);

        ErrorResponse errorResponse = ErrorResponse.builder()
                .errorCode("VALIDATION_ERROR")
                .status(HttpStatus.BAD_REQUEST.value())
                .message("Input validation failed")
                .validationErrors(validationErrors)
                .timestamp(LocalDateTime.now())
                .path(path)
                .traceId(traceId)
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle optimistic locking failures (concurrent updates)
     */
    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<ErrorResponse> handleOptimisticLock(
            ObjectOptimisticLockingFailureException ex,
            WebRequest request) {

        String traceId = generateTraceId();
        String path = request.getDescription(false).replace("uri=", "");

        log.error("Optimistic locking failure detected [TraceId: {}]", traceId, ex);

        ErrorResponse errorResponse = ErrorResponse.builder()
                .errorCode("CONFLICT")
                .status(HttpStatus.CONFLICT.value())
                .message("Concurrent update detected. Another user has modified this resource.")
                .details("Please refresh and try again.")
                .timestamp(LocalDateTime.now())
                .path(path)
                .traceId(traceId)
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }

    /**
     * Handle generic RuntimeException
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntime(
            RuntimeException ex,
            WebRequest request) {

        String traceId = generateTraceId();
        String path = request.getDescription(false).replace("uri=", "");

        log.error("Runtime exception occurred [TraceId: {}] - Message: {}",
                traceId, ex.getMessage(), ex);

        ErrorResponse errorResponse = ErrorResponse.builder()
                .errorCode("RUNTIME_ERROR")
                .status(HttpStatus.BAD_REQUEST.value())
                .message(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .path(path)
                .traceId(traceId)
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle all other exceptions
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex,
            WebRequest request) {

        String traceId = generateTraceId();
        String path = request.getDescription(false).replace("uri=", "");

        log.error("Unexpected error occurred [TraceId: {}] - Exception: {}, Message: {}",
                traceId, ex.getClass().getName(), ex.getMessage(), ex);

        ErrorResponse errorResponse = ErrorResponse.builder()
                .errorCode("INTERNAL_SERVER_ERROR")
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .message("An unexpected error occurred")
                .details(String.format("Please contact support with trace ID: %s", traceId))
                .timestamp(LocalDateTime.now())
                .path(path)
                .traceId(traceId)
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Generate a unique trace ID for debugging
     */
    private String generateTraceId() {
        return UUID.randomUUID().toString();
    }
}