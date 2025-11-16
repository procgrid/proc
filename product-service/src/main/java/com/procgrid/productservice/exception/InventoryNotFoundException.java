package com.procgrid.productservice.exception;

/**
 * Exception thrown when an inventory record is not found
 */
public class InventoryNotFoundException extends RuntimeException {
    
    public InventoryNotFoundException(String message) {
        super(message);
    }
    
    public InventoryNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}