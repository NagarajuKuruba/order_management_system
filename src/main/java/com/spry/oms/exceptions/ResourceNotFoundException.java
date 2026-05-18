package com.spry.oms.exceptions;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when a requested resource is not found.
 * HTTP Status: 404 NOT_FOUND
 */
public class ResourceNotFoundException extends ApplicationException {

    public ResourceNotFoundException(String resourceName, String identifier) {
        super(
                String.format("%s not found with identifier: %s", resourceName, identifier),
                "RESOURCE_NOT_FOUND",
                HttpStatus.NOT_FOUND
        );
    }

    public ResourceNotFoundException(String resourceName, String fieldName, Object fieldValue) {
        super(
                String.format("%s not found with %s: %s", resourceName, fieldName, fieldValue),
                "RESOURCE_NOT_FOUND",
                HttpStatus.NOT_FOUND
        );
    }

    public ResourceNotFoundException(String message) {
        super(
                message,
                "RESOURCE_NOT_FOUND",
                HttpStatus.NOT_FOUND
        );
    }
}

