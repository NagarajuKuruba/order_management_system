package com.spry.oms.exceptions;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when a resource conflict occurs.
 * Examples: Duplicate records, concurrent update conflicts, state conflicts
 * HTTP Status: 409 CONFLICT
 */
public class ConflictException extends ApplicationException {

    public ConflictException(String message) {
        super(
                message,
                "CONFLICT",
                HttpStatus.CONFLICT
        );
    }

    public ConflictException(String resourceName, String conflictReason) {
        super(
                String.format("Conflict in %s: %s", resourceName, conflictReason),
                "CONFLICT",
                HttpStatus.CONFLICT
        );
    }


    public ConflictException(String message, Throwable cause) {
        super(
                message,
                "CONFLICT",
                HttpStatus.CONFLICT,
                cause
        );
    }
}

