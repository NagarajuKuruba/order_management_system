package com.spry.oms.exceptions;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when input validation fails.
 * HTTP Status: 400 BAD_REQUEST
 */
public class ValidationException extends ApplicationException {

    public ValidationException(String message) {
        super(
                message,
                "VALIDATION_ERROR",
                HttpStatus.BAD_REQUEST
        );
    }

    public ValidationException(String fieldName, String message) {
        super(
                String.format("Validation failed for field '%s': %s", fieldName, message),
                "VALIDATION_ERROR",
                HttpStatus.BAD_REQUEST
        );
    }

    public ValidationException(String message, Throwable cause) {
        super(
                message,
                "VALIDATION_ERROR",
                HttpStatus.BAD_REQUEST,
                cause
        );
    }
}

