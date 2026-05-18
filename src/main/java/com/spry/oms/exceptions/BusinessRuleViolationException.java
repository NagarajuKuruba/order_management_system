package com.spry.oms.exceptions;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when a business rule is violated.
 * HTTP Status: 422 UNPROCESSABLE_ENTITY
 */
public class BusinessRuleViolationException extends ApplicationException {

    public BusinessRuleViolationException(String message) {
        super(
                message,
                "BUSINESS_RULE_VIOLATION",
                HttpStatus.UNPROCESSABLE_ENTITY
        );
    }

    public BusinessRuleViolationException(String message, String errorCode) {
        super(
                message,
                errorCode,
                HttpStatus.UNPROCESSABLE_ENTITY
        );
    }

    public BusinessRuleViolationException(String message, Throwable cause) {
        super(
                message,
                "BUSINESS_RULE_VIOLATION",
                HttpStatus.UNPROCESSABLE_ENTITY,
                cause
        );
    }
}

