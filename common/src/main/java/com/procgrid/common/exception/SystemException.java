package com.procgrid.common.exception;

import lombok.Getter;

/**
 * System level exception
 */
@Getter
public class SystemException extends RuntimeException {
    
    private final String errorCode;
    
    public SystemException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }
    
    public SystemException(String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
}