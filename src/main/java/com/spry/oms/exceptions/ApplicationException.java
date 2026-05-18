package com.spry.oms.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Base exception class for all application-specific exceptions.
 * All custom exceptions should extend this class.
 */
@Getter
public abstract class ApplicationException extends RuntimeException {

    private final HttpStatus httpStatus;
    private final String errorCode;

    /**
     * Constructor with message, error code and HTTP status
     * @param message Exception message
     * @param errorCode Error code for identification
     * @param httpStatus HTTP status code
     */
    public ApplicationException(String message, String errorCode, HttpStatus httpStatus) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
    }

    /**
     * Constructor with message, error code, HTTP status and cause
     * @param message Exception message
     * @param errorCode Error code for identification
     * @param httpStatus HTTP status code
     * @param cause Root cause exception
     */
    public ApplicationException(String message, String errorCode, HttpStatus httpStatus, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
    }
}

