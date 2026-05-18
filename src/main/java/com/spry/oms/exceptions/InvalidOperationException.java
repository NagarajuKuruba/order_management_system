package com.spry.oms.exceptions;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when an invalid business operation is attempted.
 * Examples: Invalid order status transitions, cancelled orders being modified, etc.
 * HTTP Status: 400 BAD_REQUEST
 */
public class InvalidOperationException extends ApplicationException {

    public InvalidOperationException(String message) {
        super(
                message,
                "INVALID_OPERATION",
                HttpStatus.BAD_REQUEST
        );
    }

    public InvalidOperationException(String message, String errorCode) {
        super(
                message,
                errorCode,
                HttpStatus.BAD_REQUEST
        );
    }

    public InvalidOperationException(String message, Throwable cause) {
        super(
                message,
                "INVALID_OPERATION",
                HttpStatus.BAD_REQUEST,
                cause
        );
    }
}

