package com.procgrid.common.exception;

import lombok.Getter;

/**
 * Entity not found exception
 */
@Getter
public class EntityNotFoundException extends RuntimeException {
    
    private final String errorCode;
    
    public EntityNotFoundException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }
    
    public EntityNotFoundException(String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
}