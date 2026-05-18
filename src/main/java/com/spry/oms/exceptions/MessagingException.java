package com.spry.oms.exceptions;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when a messaging/RabbitMQ operation fails.
 * HTTP Status: 503 SERVICE_UNAVAILABLE
 */
public class MessagingException extends ApplicationException {

    public MessagingException(String message) {
        super(
                message,
                "MESSAGING_ERROR",
                HttpStatus.SERVICE_UNAVAILABLE
        );
    }

    public MessagingException(String message, Throwable cause) {
        super(
                message,
                "MESSAGING_ERROR",
                HttpStatus.SERVICE_UNAVAILABLE,
                cause
        );
    }

    public MessagingException(String operation, String message, Throwable cause) {
        super(
                String.format("Messaging operation '%s' failed: %s", operation, message),
                "MESSAGING_ERROR",
                HttpStatus.SERVICE_UNAVAILABLE,
                cause
        );
    }
}

