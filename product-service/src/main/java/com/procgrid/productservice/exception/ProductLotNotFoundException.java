package com.procgrid.productservice.exception;

/**
 * Exception thrown when a product lot is not found
 */
public class ProductLotNotFoundException extends RuntimeException {
    
    public ProductLotNotFoundException(String message) {
        super(message);
    }
    
    public ProductLotNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}