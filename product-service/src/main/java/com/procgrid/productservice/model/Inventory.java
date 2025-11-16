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
 * Inventory entity for tracking product stock levels
 * Aggregates quantities across all lots for a product
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Inventory {
    
    /**
     * Unique identifier for the inventory record
     */
    private Long id;
    
    /**
     * Product this inventory belongs to
     */
    @NotNull(message = "Product ID is required")
    private Long productId;
    
    /**
     * Producer who owns this inventory
     */
    @NotNull(message = "Producer ID is required")
    private Long producerId;
    
    /**
     * Total quantity across all lots
     */
    @NotNull(message = "Total quantity is required")
    @DecimalMin(value = "0.0", message = "Total quantity cannot be negative")
    @Digits(integer = 10, fraction = 3, message = "Total quantity must have at most 10 digits and 3 decimal places")
    private BigDecimal totalQuantity;
    
    /**
     * Available quantity (not reserved or sold)
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
     * Sold quantity (completed orders)
     */
    @Builder.Default
    @DecimalMin(value = "0.0", message = "Sold quantity cannot be negative")
    private BigDecimal soldQuantity = BigDecimal.ZERO;
    
    /**
     * Damaged/waste quantity
     */
    @Builder.Default
    @DecimalMin(value = "0.0", message = "Damaged quantity cannot be negative")
    private BigDecimal damagedQuantity = BigDecimal.ZERO;
    
    /**
     * Quantity unit
     */
    @NotBlank(message = "Quantity unit is required")
    private String quantityUnit;
    
    /**
     * Minimum stock level (reorder point)
     */
    @DecimalMin(value = "0.0", message = "Minimum stock level cannot be negative")
    private BigDecimal minStockLevel;
    
    /**
     * Maximum stock level
     */
    @DecimalMin(value = "0.0", message = "Maximum stock level cannot be negative")
    private BigDecimal maxStockLevel;
    
    /**
     * Reorder quantity
     */
    @DecimalMin(value = "0.0", message = "Reorder quantity cannot be negative")
    private BigDecimal reorderQuantity;
    
    /**
     * Average cost per unit
     */
    @DecimalMin(value = "0.0", message = "Average cost cannot be negative")
    @Digits(integer = 10, fraction = 2, message = "Average cost must have at most 10 digits and 2 decimal places")
    private BigDecimal averageCost;
    
    /**
     * Current cost per unit
     */
    @DecimalMin(value = "0.0", message = "Cost per unit cannot be negative")
    @Digits(integer = 10, fraction = 2, message = "Cost per unit must have at most 10 digits and 2 decimal places")
    private BigDecimal costPerUnit;
    
    /**
     * Total inventory value
     */
    @DecimalMin(value = "0.0", message = "Total value cannot be negative")
    @Digits(integer = 15, fraction = 2, message = "Total value must have at most 15 digits and 2 decimal places")
    private BigDecimal totalValue;
    
    /**
     * Inventory status
     */
    @NotNull(message = "Status is required")
    @Builder.Default
    private InventoryStatus status = InventoryStatus.IN_STOCK;
    
    /**
     * Location where inventory is stored
     */
    @Size(max = 255, message = "Location cannot exceed 255 characters")
    private String location;
    
    /**
     * Warehouse identifier
     */
    private String warehouseId;
    
    /**
     * Last stock update timestamp
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastUpdated;
    
    /**
     * Last stock count date
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastStockCount;
    
    /**
     * Next stock count due date
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime nextStockCountDue;
    
    /**
     * Stock turnover rate (annual)
     */
    private BigDecimal turnoverRate;
    
    /**
     * Days of supply remaining
     */
    private Integer daysOfSupply;
    
    /**
     * Lead time in days for restocking
     */
    private Integer leadTimeDays;
    
    /**
     * Seasonal adjustment factor
     */
    private BigDecimal seasonalFactor;
    
    /**
     * Notes about inventory
     */
    @Size(max = 1000, message = "Notes cannot exceed 1000 characters")
    private String notes;
    
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
     * Inventory status enumeration
     */
    public enum InventoryStatus {
        IN_STOCK,       // Normal stock levels
        LOW_STOCK,      // Below minimum threshold
        OUT_OF_STOCK,   // No stock available
        OVERSTOCK,      // Above maximum threshold
        PENDING,        // Stock update pending
        DISCONTINUED    // Product discontinued
    }
    
    /**
     * Check if inventory is available
     */
    public boolean isAvailable() {
        return status != InventoryStatus.OUT_OF_STOCK && 
               status != InventoryStatus.DISCONTINUED &&
               availableQuantity != null && 
               availableQuantity.compareTo(BigDecimal.ZERO) > 0 &&
               !Boolean.TRUE.equals(deleted);
    }
    
    /**
     * Check if stock is low
     */
    public boolean isLowStock() {
        if (minStockLevel == null || availableQuantity == null) {
            return false;
        }
        return availableQuantity.compareTo(minStockLevel) <= 0;
    }
    
    /**
     * Check if overstocked
     */
    public boolean isOverstocked() {
        if (maxStockLevel == null || totalQuantity == null) {
            return false;
        }
        return totalQuantity.compareTo(maxStockLevel) > 0;
    }
    
    /**
     * Reserve quantity for orders
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
        updateStatus();
        return true;
    }
    
    /**
     * Release reserved quantity
     */
    public void releaseReservedQuantity(BigDecimal quantityToRelease) {
        if (quantityToRelease == null || quantityToRelease.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }
        
        BigDecimal maxRelease = reservedQuantity.min(quantityToRelease);
        reservedQuantity = reservedQuantity.subtract(maxRelease);
        availableQuantity = availableQuantity.add(maxRelease);
        updateStatus();
    }
    
    /**
     * Complete sale by moving quantity from reserved to sold
     */
    public void completeSale(BigDecimal quantitySold) {
        if (quantitySold == null || quantitySold.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }
        
        BigDecimal maxSale = reservedQuantity.min(quantitySold);
        reservedQuantity = reservedQuantity.subtract(maxSale);
        soldQuantity = soldQuantity.add(maxSale);
        totalQuantity = totalQuantity.subtract(maxSale);
        updateStatus();
    }
    
    /**
     * Add new stock
     */
    public void addStock(BigDecimal quantityToAdd) {
        if (quantityToAdd == null || quantityToAdd.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }
        
        totalQuantity = totalQuantity.add(quantityToAdd);
        availableQuantity = availableQuantity.add(quantityToAdd);
        updateStatus();
    }
    
    /**
     * Remove damaged/waste stock
     */
    public void removeDamagedStock(BigDecimal quantityDamaged) {
        if (quantityDamaged == null || quantityDamaged.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }
        
        BigDecimal maxRemove = availableQuantity.min(quantityDamaged);
        availableQuantity = availableQuantity.subtract(maxRemove);
        totalQuantity = totalQuantity.subtract(maxRemove);
        damagedQuantity = damagedQuantity.add(maxRemove);
        updateStatus();
    }
    
    /**
     * Update inventory status based on current quantities
     */
    private void updateStatus() {
        if (availableQuantity.compareTo(BigDecimal.ZERO) == 0) {
            status = InventoryStatus.OUT_OF_STOCK;
        } else if (isLowStock()) {
            status = InventoryStatus.LOW_STOCK;
        } else if (isOverstocked()) {
            status = InventoryStatus.OVERSTOCK;
        } else {
            status = InventoryStatus.IN_STOCK;
        }
    }
    
    /**
     * Calculate stock utilization percentage
     */
    public BigDecimal getStockUtilization() {
        if (totalQuantity == null || totalQuantity.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal used = soldQuantity.add(reservedQuantity);
        return used.multiply(BigDecimal.valueOf(100)).divide(totalQuantity, 2, BigDecimal.ROUND_HALF_UP);
    }
    
    /**
     * Calculate inventory value
     */
    public void calculateTotalValue() {
        if (totalQuantity != null && averageCost != null) {
            totalValue = totalQuantity.multiply(averageCost);
        }
    }
}