package com.spry.oms.exceptions;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Standardized error response structure for all exceptions.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    /**
     * Unique error code for programmatic handling
     */
    private String errorCode;

    /**
     * HTTP status code
     */
    private int status;

    /**
     * Human-readable error message
     */
    private String message;

    /**
     * Detailed error description
     */
    private String details;

    /**
     * Timestamp when error occurred
     */
    private LocalDateTime timestamp;

    /**
     * Request path that caused the error
     */
    private String path;

    /**
     * Field-level validation errors (for validation exceptions)
     */
    private Map<String, String> validationErrors;

    /**
     * List of error messages (for multiple errors)
     */
    private List<String> errors;

    /**
     * Trace ID for logging and debugging
     */
    private String traceId;
}

