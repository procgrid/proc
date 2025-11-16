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
 * DTO for product summary response (for listings)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductSummaryResponse {
    
    private Long id;
    private Long producerId;
    private String producerName;
    private Long categoryId;
    private String categoryName;
    private String name;
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
    private String primaryImageUrl;
    private Boolean supportsBulkOrders;
    private Boolean isSeasonal;
    private String seasonInfo;
    private Long viewCount;
    private BigDecimal averageRating;
    private Integer ratingCount;
    private String slug;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime harvestDate;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime expiryDate;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
}