package com.procgrid.common.utils;

/**
 * Standard error codes for the application
 */
public final class ErrorCodes {
    
    // Authentication & Authorization
    public static final String AUTH_INVALID_CREDENTIALS = "AUTH_001";
    public static final String AUTH_TOKEN_EXPIRED = "AUTH_002";
    public static final String AUTH_TOKEN_INVALID = "AUTH_003";
    public static final String AUTH_INSUFFICIENT_PERMISSIONS = "AUTH_004";
    public static final String AUTH_ACCOUNT_LOCKED = "AUTH_005";
    public static final String AUTH_PASSWORD_EXPIRED = "AUTH_006";
    
    // User Management
    public static final String USER_NOT_FOUND = "USER_001";
    public static final String USER_ALREADY_EXISTS = "USER_002";
    public static final String USER_EMAIL_ALREADY_EXISTS = "USER_003";
    public static final String USER_INVALID_DATA = "USER_004";
    public static final String USER_KYC_INCOMPLETE = "USER_005";
    public static final String USER_PROFILE_INCOMPLETE = "USER_006";
    
    // Product Management
    public static final String PRODUCT_NOT_FOUND = "PROD_001";
    public static final String PRODUCT_INVALID_DATA = "PROD_002";
    public static final String PRODUCT_CATEGORY_NOT_FOUND = "PROD_003";
    public static final String PRODUCT_OUT_OF_STOCK = "PROD_004";
    public static final String PRODUCT_INACTIVE = "PROD_005";
    
    // Quote Management
    public static final String QUOTE_NOT_FOUND = "QUOTE_001";
    public static final String QUOTE_INVALID_STATUS = "QUOTE_002";
    public static final String QUOTE_EXPIRED = "QUOTE_003";
    public static final String QUOTE_ALREADY_ACCEPTED = "QUOTE_004";
    public static final String QUOTE_INVALID_AMOUNT = "QUOTE_005";
    
    // Order Management
    public static final String ORDER_NOT_FOUND = "ORDER_001";
    public static final String ORDER_INVALID_STATUS = "ORDER_002";
    public static final String ORDER_CANNOT_CANCEL = "ORDER_003";
    public static final String ORDER_INVALID_ITEMS = "ORDER_004";
    public static final String ORDER_PAYMENT_FAILED = "ORDER_005";
    
    // Payment & Escrow
    public static final String PAYMENT_NOT_FOUND = "PAY_001";
    public static final String PAYMENT_INSUFFICIENT_FUNDS = "PAY_002";
    public static final String PAYMENT_INVALID_STATUS = "PAY_003";
    public static final String PAYMENT_ESCROW_FAILED = "PAY_004";
    public static final String PAYMENT_RELEASE_FAILED = "PAY_005";
    public static final String PAYMENT_REFUND_FAILED = "PAY_006";
    
    // Notification
    public static final String NOTIFICATION_SEND_FAILED = "NOTIF_001";
    public static final String NOTIFICATION_TEMPLATE_NOT_FOUND = "NOTIF_002";
    public static final String NOTIFICATION_INVALID_RECIPIENT = "NOTIF_003";
    
    // Validation
    public static final String VALIDATION_REQUIRED_FIELD = "VAL_001";
    public static final String VALIDATION_INVALID_FORMAT = "VAL_002";
    public static final String VALIDATION_INVALID_RANGE = "VAL_003";
    public static final String VALIDATION_INVALID_EMAIL = "VAL_004";
    public static final String VALIDATION_INVALID_PHONE = "VAL_005";
    public static final String VALIDATION_INVALID_DATE = "VAL_006";
    
    // System Errors
    public static final String SYSTEM_DATABASE_ERROR = "SYS_001";
    public static final String SYSTEM_CACHE_ERROR = "SYS_002";
    public static final String SYSTEM_EXTERNAL_SERVICE_ERROR = "SYS_003";
    public static final String SYSTEM_FILE_UPLOAD_ERROR = "SYS_004";
    public static final String SYSTEM_CONFIGURATION_ERROR = "SYS_005";
    public static final String SYSTEM_RATE_LIMIT_EXCEEDED = "SYS_006";
    
    // Business Logic
    public static final String BUSINESS_RULE_VIOLATION = "BIZ_001";
    public static final String BUSINESS_WORKFLOW_ERROR = "BIZ_002";
    public static final String BUSINESS_INVALID_STATE = "BIZ_003";
    public static final String BUSINESS_CONCURRENT_UPDATE = "BIZ_004";
    
    private ErrorCodes() {
        // Utility class
    }
}