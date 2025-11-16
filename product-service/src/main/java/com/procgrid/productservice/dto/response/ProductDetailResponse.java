package com.procgrid.productservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for detailed product response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductDetailResponse {
    
    private Long id;
    private Long producerId;
    private String producerName;
    private String producerLocation;
    private String producerPhone;
    private String producerEmail;
    private Long categoryId;
    private String categoryName;
    private String categoryPath;
    private String name;
    private String description;
    private String summary;
    private String variety;
    private String grade;
    private String origin;
    private BigDecimal price;
    private String priceUnit;
    private BigDecimal availableQuantity;
    private String quantityUnit;
    private BigDecimal minOrderQuantity;
    private String status;
    private String visibility;
    private List<String> certifications;
    private List<String> tags;
    private List<String> imageUrls;
    private String primaryImageUrl;
    private String packaging;
    private String storageRequirements;
    private String transportationRequirements;
    private Boolean supportsBulkOrders;
    private Boolean isSeasonal;
    private String seasonInfo;
    private Long viewCount;
    private BigDecimal averageRating;
    private Integer ratingCount;
    private String slug;
    private String metaKeywords;
    private String metaDescription;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime harvestDate;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime expiryDate;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
    
    private String createdBy;
    private String updatedBy;
    
    // Additional computed fields
    private Boolean isExpired;
    private Boolean isAvailable;
    private Integer daysUntilExpiry;
    private BigDecimal pricePerKg; // Normalized price for comparison
    
    // Related data
    private List<ProductLotSummary> lots;
    private InventorySummary inventory;
}

/**
 * DTO for product lot summary
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class ProductLotSummary {
    
    private Long id;
    private String lotNumber;
    private BigDecimal quantity;
    private BigDecimal availableQuantity;
    private String quantityUnit;
    private String qualityGrade;
    private String status;
    private String storageLocation;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime harvestDate;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime expiryDate;
}

/**
 * DTO for inventory summary
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class InventorySummary {
    
    private Long id;
    private BigDecimal totalQuantity;
    private BigDecimal availableQuantity;
    private BigDecimal reservedQuantity;
    private BigDecimal soldQuantity;
    private String quantityUnit;
    private String status;
    private BigDecimal averageCost;
    private BigDecimal totalValue;
    private Boolean isLowStock;
    private Boolean isOverstocked;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastUpdated;
}