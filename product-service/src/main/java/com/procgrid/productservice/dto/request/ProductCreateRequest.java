package com.procgrid.productservice.dto.request;

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
 * DTO for creating new products
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductCreateRequest {
    
    @NotNull(message = "Producer ID is required")
    private Long producerId;
    
    @NotNull(message = "Category ID is required")
    private Long categoryId;
    
    @NotBlank(message = "Product name is required")
    @Size(min = 3, max = 255, message = "Product name must be between 3 and 255 characters")
    private String name;
    
    @Size(max = 5000, message = "Description cannot exceed 5000 characters")
    private String description;
    
    @Size(max = 500, message = "Summary cannot exceed 500 characters")
    private String summary;
    
    @Size(max = 100, message = "Variety cannot exceed 100 characters")
    private String variety;
    
    @Size(max = 50, message = "Grade cannot exceed 50 characters")
    private String grade;
    
    @Size(max = 255, message = "Origin cannot exceed 255 characters")
    private String origin;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime harvestDate;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime expiryDate;
    
    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    @Digits(integer = 10, fraction = 2, message = "Price must have at most 10 digits and 2 decimal places")
    private BigDecimal price;
    
    @NotBlank(message = "Price unit is required")
    @Size(max = 20, message = "Price unit cannot exceed 20 characters")
    private String priceUnit;
    
    @NotNull(message = "Quantity is required")
    @DecimalMin(value = "0.0", message = "Quantity cannot be negative")
    @Digits(integer = 10, fraction = 3, message = "Quantity must have at most 10 digits and 3 decimal places")
    private BigDecimal availableQuantity;
    
    @NotBlank(message = "Quantity unit is required")
    @Size(max = 20, message = "Quantity unit cannot exceed 20 characters")
    private String quantityUnit;
    
    @DecimalMin(value = "0.0", message = "Minimum order quantity cannot be negative")
    private BigDecimal minOrderQuantity;
    
    @DecimalMin(value = "0.0", message = "Maximum order quantity cannot be negative")
    private BigDecimal maxOrderQuantity;
    
    private List<String> certifications;
    private List<String> tags;
    
    private String packaging;
    private String storageRequirements;
    private String transportationRequirements;
    
    @Builder.Default
    private Boolean supportsBulkOrders = false;
    
    @Builder.Default
    private Boolean isSeasonal = false;
    
    private String seasonInfo;
    
    @Pattern(regexp = "^[a-z0-9]+(?:-[a-z0-9]+)*$", message = "Invalid URL slug format")
    private String slug;
    
    private String metaKeywords;
    
    @Size(max = 160, message = "Meta description cannot exceed 160 characters")
    private String metaDescription;
}