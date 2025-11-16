package com.procgrid.productservice.exception;

/**
 * Exception thrown when category deletion fails due to business constraints
 */
public class CategoryDeletionException extends RuntimeException {
    
    public CategoryDeletionException(String message) {
        super(message);
    }
    
    public CategoryDeletionException(String message, Throwable cause) {
        super(message, cause);
    }
}