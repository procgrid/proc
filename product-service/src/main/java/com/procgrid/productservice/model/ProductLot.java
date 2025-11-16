package com.procgrid.productservice.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * ProductLot entity for tracking specific batches/lots of products
 * Used for inventory management and traceability
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductLot {
    
    /**
     * Unique identifier for the product lot
     */
    private Long id;
    
    /**
     * Product this lot belongs to
     */
    @NotNull(message = "Product ID is required")
    private Long productId;
    
    /**
     * Inventory this lot belongs to
     */
    private Long inventoryId;
    
    /**
     * Producer who owns this lot
     */
    private Long producerId;
    
    /**
     * Unique lot/batch number
     */
    @NotBlank(message = "Lot number is required")
    @Size(min = 3, max = 50, message = "Lot number must be between 3 and 50 characters")
    private String lotNumber;
    
    /**
     * Quantity in this lot
     */
    @NotNull(message = "Quantity is required")
    @DecimalMin(value = "0.0", message = "Quantity cannot be negative")
    @Digits(integer = 10, fraction = 3, message = "Quantity must have at most 10 digits and 3 decimal places")
    private BigDecimal quantity;
    
    /**
     * Total quantity in this lot (initial quantity)
     */
    @NotNull(message = "Total quantity is required")
    @DecimalMin(value = "0.0", message = "Total quantity cannot be negative")
    @Digits(integer = 10, fraction = 3, message = "Total quantity must have at most 10 digits and 3 decimal places")
    private BigDecimal totalQuantity;
    
    /**
     * Available quantity (may be less than total due to reservations/sales)
     */
    @NotNull(message = "Available quantity is required")
    @DecimalMin(value = "0.0", message = "Available quantity cannot be negative")
    @Digits(integer = 10, fraction = 3, message = "Available quantity must have at most 10 digits and 3 decimal places")
    private BigDecimal availableQuantity;
    
    /**
     * Reserved quantity (pending orders)
     */
    @Builder.Default
    @DecimalMin(value = "0.0", message = "Reserved quantity cannot be negative")
    private BigDecimal reservedQuantity = BigDecimal.ZERO;
    
    /**
     * Damaged quantity (not sellable)
     */
    @Builder.Default
    @DecimalMin(value = "0.0", message = "Damaged quantity cannot be negative")
    private BigDecimal damagedQuantity = BigDecimal.ZERO;
    
    /**
     * Sold quantity
     */
    @Builder.Default
    @DecimalMin(value = "0.0", message = "Sold quantity cannot be negative")
    private BigDecimal soldQuantity = BigDecimal.ZERO;
    
    /**
     * Quantity unit
     */
    @NotBlank(message = "Quantity unit is required")
    private String quantityUnit;
    
    /**
     * Manufacturing/production date
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime productionDate;
    
    /**
     * Harvest date for this specific lot
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime harvestDate;
    
    /**
     * Expiry date for this lot
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime expiryDate;
    
    /**
     * Storage location
     */
    @Size(max = 255, message = "Storage location cannot exceed 255 characters")
    private String storageLocation;
    
    /**
     * Warehouse or facility where lot is stored
     */
    private String warehouse;
    
    /**
     * Quality grade for this specific lot
     */
    @Size(max = 50, message = "Quality grade cannot exceed 50 characters")
    private String qualityGrade;
    
    /**
     * Quality test results
     */
    private String qualityTestResults;

    /**
     * Quality inspection notes
     */
    private String qualityNotes;
    
    /**
     * Supplier/farm details for traceability
     */
    private String supplierDetails;
    
    /**
     * Field/farm location where product was grown
     */
    private String fieldLocation;
    
    /**
     * GPS coordinates of production location
     */
    private String gpsCoordinates;
    
    /**
     * Lot status
     */
    @NotNull(message = "Status is required")
    @Builder.Default
    private LotStatus status = LotStatus.AVAILABLE;
    
    /**
     * Cost price for this lot
     */
    @DecimalMin(value = "0.0", message = "Cost price cannot be negative")
    @Digits(integer = 10, fraction = 2, message = "Cost price must have at most 10 digits and 2 decimal places")
    private BigDecimal costPrice;
    
    /**
     * Selling price for this lot (may differ from base product price)
     */
    @DecimalMin(value = "0.0", message = "Selling price cannot be negative")
    @Digits(integer = 10, fraction = 2, message = "Selling price must have at most 10 digits and 2 decimal places")
    private BigDecimal sellingPrice;
    
    /**
     * Notes about this lot
     */
    @Size(max = 2000, message = "Notes cannot exceed 2000 characters")
    private String notes;

    /**
     * Processing method used for this lot
     */
    private String processingMethod;

    /**
     * Processing notes for this lot
     */
    private String processingNotes;
    
    /**
     * Certifications specific to this lot
     */
    private String certifications;
    
    /**
     * Storage conditions for this lot
     */
    private String storageConditions;
    
    /**
     * Temperature requirements
     */
    private String temperatureRequirements;
    
    /**
     * Humidity requirements
     */
    private String humidityRequirements;
    
    /**
     * Record creation timestamp
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    
    /**
     * Record last update timestamp
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
    
    /**
     * User who created the record
     */
    private String createdBy;
    
    /**
     * User who last updated the record
     */
    private String updatedBy;
    
    /**
     * Soft delete flag
     */
    @Builder.Default
    private Boolean deleted = false;
    
    /**
     * Lot status enumeration
     */
    public enum LotStatus {
        AVAILABLE,      // Available for sale
        RESERVED,       // Reserved for orders
        SOLD,           // Completely sold
        EXPIRED,        // Past expiry date
        EXPIRING_SOON,  // Close to expiry
        QUARANTINE,     // Under quality inspection
        DAMAGED,        // Damaged/unusable
        QUALITY_ISSUE,  // Quality problems detected
        SOLD_OUT,       // Completely sold out
        RECALLED        // Product recall
    }
    
    /**
     * Check if lot is available for allocation
     */
    public boolean isAvailable() {
        return status == LotStatus.AVAILABLE && 
               availableQuantity != null && 
               availableQuantity.compareTo(BigDecimal.ZERO) > 0 &&
               !isExpired() &&
               !Boolean.TRUE.equals(deleted);
    }
    
    /**
     * Check if lot is expired
     */
    public boolean isExpired() {
        return expiryDate != null && expiryDate.isBefore(LocalDateTime.now());
    }
    
    /**
     * Check if lot is expiring soon
     */
    public boolean isExpiringSoon(int days) {
        return expiryDate != null && 
               expiryDate.isBefore(LocalDateTime.now().plusDays(days)) &&
               !isExpired();
    }
    
    /**
     * Reserve quantity from this lot
     */
    public boolean reserveQuantity(BigDecimal quantityToReserve) {
        if (quantityToReserve == null || quantityToReserve.compareTo(BigDecimal.ZERO) <= 0) {
            return false;
        }
        
        if (availableQuantity.compareTo(quantityToReserve) < 0) {
            return false; // Not enough available quantity
        }
        
        availableQuantity = availableQuantity.subtract(quantityToReserve);
        reservedQuantity = reservedQuantity.add(quantityToReserve);
        
        if (availableQuantity.compareTo(BigDecimal.ZERO) == 0) {
            status = LotStatus.RESERVED;
        }
        
        return true;
    }
    
    /**
     * Release reserved quantity back to available
     */
    public void releaseReservedQuantity(BigDecimal quantityToRelease) {
        if (quantityToRelease == null || quantityToRelease.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }
        
        BigDecimal maxRelease = reservedQuantity.min(quantityToRelease);
        reservedQuantity = reservedQuantity.subtract(maxRelease);
        availableQuantity = availableQuantity.add(maxRelease);
        
        if (status == LotStatus.RESERVED && availableQuantity.compareTo(BigDecimal.ZERO) > 0) {
            status = LotStatus.AVAILABLE;
        }
    }
    
    /**
     * Calculate utilization percentage
     */
    public BigDecimal getUtilizationPercentage() {
        if (quantity == null || quantity.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal used = quantity.subtract(availableQuantity);
        return used.multiply(BigDecimal.valueOf(100)).divide(quantity, 2, BigDecimal.ROUND_HALF_UP);
    }
    
    /**
     * Calculate total quantity accounting for all components
     */
    public BigDecimal calculateTotalQuantity() {
        BigDecimal total = BigDecimal.ZERO;
        
        if (availableQuantity != null) {
            total = total.add(availableQuantity);
        }
        if (reservedQuantity != null) {
            total = total.add(reservedQuantity);
        }
        if (soldQuantity != null) {
            total = total.add(soldQuantity);
        }
        if (damagedQuantity != null) {
            total = total.add(damagedQuantity);
        }
        
        return total;
    }
}