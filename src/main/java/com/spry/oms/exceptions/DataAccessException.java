package com.spry.oms.exceptions;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when a database or data access operation fails.
 * HTTP Status: 500 INTERNAL_SERVER_ERROR
 */
public class DataAccessException extends ApplicationException {

    public DataAccessException(String message) {
        super(
                message,
                "DATA_ACCESS_ERROR",
                HttpStatus.INTERNAL_SERVER_ERROR
        );
    }

    public DataAccessException(String message, Throwable cause) {
        super(
                message,
                "DATA_ACCESS_ERROR",
                HttpStatus.INTERNAL_SERVER_ERROR,
                cause
        );
    }

    public DataAccessException(String operation, String message, Throwable cause) {
        super(
                String.format("Database operation '%s' failed: %s", operation, message),
                "DATA_ACCESS_ERROR",
                HttpStatus.INTERNAL_SERVER_ERROR,
                cause
        );
    }
}

