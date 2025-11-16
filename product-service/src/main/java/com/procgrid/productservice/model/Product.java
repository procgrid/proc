package com.procgrid.productservice.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Product entity representing agricultural products in the system
 * This is the core entity for the product catalog
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Product {
    
    /**
     * Unique identifier for the product
     */
    private Long id;
    
    /**
     * Producer who owns this product
     */
    @NotNull(message = "Producer ID is required")
    private Long producerId;
    
    /**
     * Product SKU (Stock Keeping Unit)
     */
    private String sku;
    
    /**
     * Category this product belongs to
     */
    @NotNull(message = "Category ID is required")
    private Long categoryId;
    
    /**
     * Product title/name
     */
    @NotBlank(message = "Product name is required")
    @Size(min = 3, max = 255, message = "Product name must be between 3 and 255 characters")
    private String name;
    
    /**
     * Detailed product description
     */
    @Size(max = 5000, message = "Description cannot exceed 5000 characters")
    private String description;
    
    /**
     * Short summary description
     */
    @Size(max = 500, message = "Summary cannot exceed 500 characters")
    private String summary;
    
    /**
     * Product variety/cultivar
     */
    @Size(max = 100, message = "Variety cannot exceed 100 characters")
    private String variety;
    
    /**
     * Grade/quality of the product
     */
    @Size(max = 50, message = "Grade cannot exceed 50 characters")
    private String grade;
    
    /**
     * Origin/source location
     */
    @Size(max = 255, message = "Origin cannot exceed 255 characters")
    private String origin;
    
    /**
     * Harvest date
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime harvestDate;
    
    /**
     * Best before/expiry date
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime expiryDate;
    
    /**
     * Base price per unit
     */
    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    @Digits(integer = 10, fraction = 2, message = "Price must have at most 10 digits and 2 decimal places")
    private BigDecimal price;
    
    /**
     * Price unit (per kg, per quintal, per ton, etc.)
     */
    @NotBlank(message = "Price unit is required")
    @Size(max = 20, message = "Price unit cannot exceed 20 characters")
    private String priceUnit;
    
    /**
     * Available quantity
     */
    @NotNull(message = "Quantity is required")
    @DecimalMin(value = "0.0", message = "Quantity cannot be negative")
    @Digits(integer = 10, fraction = 3, message = "Quantity must have at most 10 digits and 3 decimal places")
    private BigDecimal availableQuantity;
    
    /**
     * Quantity unit (kg, quintal, ton, pieces, etc.)
     */
    @NotBlank(message = "Quantity unit is required")
    @Size(max = 20, message = "Quantity unit cannot exceed 20 characters")
    private String quantityUnit;
    
    /**
     * Minimum order quantity
     */
    @DecimalMin(value = "0.0", message = "Minimum order quantity cannot be negative")
    @Digits(integer = 10, fraction = 3, message = "Minimum order quantity must have at most 10 digits and 3 decimal places")
    private BigDecimal minOrderQuantity;
    
    /**
     * Maximum order quantity
     */
    @DecimalMin(value = "0.0", message = "Maximum order quantity cannot be negative")
    @Digits(integer = 10, fraction = 3, message = "Maximum order quantity must have at most 10 digits and 3 decimal places")
    private BigDecimal maxOrderQuantity;
    
    /**
     * Product status
     */
    @NotNull(message = "Status is required")
    private ProductStatus status = ProductStatus.DRAFT;
    
    /**
     * Product visibility
     */
    @NotNull(message = "Visibility is required")
    private ProductVisibility visibility = ProductVisibility.PUBLIC;
    
    /**
     * Certifications (organic, fair trade, etc.)
     */
    private List<String> certifications;
    
    /**
     * Product tags for search and categorization
     */
    private List<String> tags;
    
    /**
     * Product images URLs
     */
    private List<String> imageUrls;
    
    /**
     * Primary image URL
     */
    private String primaryImageUrl;
    
    /**
     * Packaging details
     */
    private String packaging;
    
    /**
     * Storage requirements
     */
    private String storageRequirements;
    
    /**
     * Transportation requirements
     */
    private String transportationRequirements;
    
    /**
     * Whether the product supports bulk orders
     */
    @Builder.Default
    private Boolean supportsBulkOrders = false;
    
    /**
     * Whether the product is seasonal
     */
    @Builder.Default
    private Boolean isSeasonal = false;
    
    /**
     * Season information if seasonal
     */
    private String seasonInfo;
    
    /**
     * Product views count for analytics
     */
    @Builder.Default
    private Long viewCount = 0L;
    
    /**
     * Product rating (1-5 stars)
     */
    @DecimalMin(value = "1.0", message = "Rating must be at least 1.0")
    @DecimalMax(value = "5.0", message = "Rating cannot exceed 5.0")
    private BigDecimal averageRating;
    
    /**
     * Number of ratings
     */
    @Builder.Default
    private Integer ratingCount = 0;
    
    /**
     * SEO-friendly URL slug
     */
    @Pattern(regexp = "^[a-z0-9]+(?:-[a-z0-9]+)*$", message = "Invalid URL slug format")
    private String slug;
    
    /**
     * Meta keywords for SEO
     */
    private String metaKeywords;
    
    /**
     * Meta description for SEO
     */
    @Size(max = 160, message = "Meta description cannot exceed 160 characters")
    private String metaDescription;
    
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
     * Product status enumeration
     */
    public enum ProductStatus {
        DRAFT,          // Product is being created/edited
        ACTIVE,         // Product is live and available
        INACTIVE,       // Product is inactive/disabled
        OUT_OF_STOCK,   // Product is temporarily unavailable
        DISCONTINUED,   // Product is permanently unavailable
        SUSPENDED       // Product is temporarily suspended
    }
    
    /**
     * Product visibility enumeration
     */
    public enum ProductVisibility {
        PUBLIC,    // Visible to all users
        PRIVATE,   // Visible only to producer
        RESTRICTED // Visible to specific buyers only
    }
    
    /**
     * Check if product is available for purchase
     */
    public boolean isAvailable() {
        return status == ProductStatus.ACTIVE && 
               availableQuantity != null && 
               availableQuantity.compareTo(BigDecimal.ZERO) > 0 &&
               !Boolean.TRUE.equals(deleted);
    }
    
    /**
     * Check if product is expired
     */
    public boolean isExpired() {
        return expiryDate != null && expiryDate.isBefore(LocalDateTime.now());
    }
    
    /**
     * Check if minimum order quantity is met
     */
    public boolean isMinOrderQuantityMet(BigDecimal orderQuantity) {
        if (minOrderQuantity == null || orderQuantity == null) {
            return true;
        }
        return orderQuantity.compareTo(minOrderQuantity) >= 0;
    }
}