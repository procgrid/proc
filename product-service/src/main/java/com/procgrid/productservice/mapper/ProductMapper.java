package com.procgrid.productservice.mapper;

import com.procgrid.productservice.dto.request.*;
import com.procgrid.productservice.dto.response.*;
import com.procgrid.productservice.model.Product;
import org.mapstruct.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * MapStruct mapper for Product entity conversions
 */
@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    imports = {LocalDateTime.class}
)
public interface ProductMapper {
    
    /**
     * Convert Product entity to summary response DTO
     */
    @Mapping(target = "producerName", ignore = true) // Will be set by service
    @Mapping(target = "categoryName", ignore = true) // Will be set by service
    ProductSummaryResponse toSummaryResponse(Product product);
    
    /**
     * Convert list of Product entities to summary response DTOs
     */
    List<ProductSummaryResponse> toSummaryResponseList(List<Product> products);
    
    /**
     * Convert Product entity to detailed response DTO
     */
    @Mapping(target = "producerName", ignore = true)
    @Mapping(target = "producerLocation", ignore = true)
    @Mapping(target = "producerPhone", ignore = true)
    @Mapping(target = "producerEmail", ignore = true)
    @Mapping(target = "categoryName", ignore = true)
    @Mapping(target = "categoryPath", ignore = true)
    @Mapping(target = "isExpired", expression = "java(product.isExpired())")
    @Mapping(target = "isAvailable", expression = "java(product.isAvailable())")
    @Mapping(target = "daysUntilExpiry", ignore = true) // Will be calculated by service
    @Mapping(target = "pricePerKg", ignore = true) // Will be calculated by service
    @Mapping(target = "lots", ignore = true) // Will be set by service
    @Mapping(target = "inventory", ignore = true) // Will be set by service
    ProductDetailResponse toDetailResponse(Product product);
    
    /**
     * Convert ProductCreateRequest DTO to Product entity
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "producerId", ignore = true) // Will be set from security context
    @Mapping(target = "status", constant = "DRAFT")
    @Mapping(target = "visibility", constant = "PRIVATE")
    @Mapping(target = "imageUrls", ignore = true)
    @Mapping(target = "primaryImageUrl", ignore = true)
    @Mapping(target = "viewCount", constant = "0L")
    @Mapping(target = "averageRating", ignore = true)
    @Mapping(target = "ratingCount", constant = "0")
    @Mapping(target = "createdAt", expression = "java(LocalDateTime.now())")
    @Mapping(target = "updatedAt", expression = "java(LocalDateTime.now())")
    @Mapping(target = "createdBy", ignore = true) // Will be set from security context
    @Mapping(target = "updatedBy", ignore = true) // Will be set from security context
    @Mapping(target = "deleted", constant = "false")
    Product toEntity(ProductCreateRequest request);
    
    /**
     * Update Product entity from ProductUpdateRequest DTO
     * Only updates non-null fields from the request
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "producerId", ignore = true)
    @Mapping(target = "status", ignore = true) // Status updated separately
    @Mapping(target = "visibility", ignore = true) // Visibility updated separately
    @Mapping(target = "imageUrls", ignore = true)
    @Mapping(target = "primaryImageUrl", ignore = true)
    @Mapping(target = "viewCount", ignore = true)
    @Mapping(target = "averageRating", ignore = true)
    @Mapping(target = "ratingCount", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", expression = "java(LocalDateTime.now())")
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true) // Will be set from security context
    @Mapping(target = "deleted", ignore = true)
    void updateEntityFromRequest(@MappingTarget Product product, ProductUpdateRequest request);
    
    /**
     * Update Product status and visibility
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "producerId", ignore = true)
    @Mapping(target = "categoryId", ignore = true)
    @Mapping(target = "name", ignore = true)
    @Mapping(target = "description", ignore = true)
    @Mapping(target = "summary", ignore = true)
    @Mapping(target = "variety", ignore = true)
    @Mapping(target = "grade", ignore = true)
    @Mapping(target = "origin", ignore = true)
    @Mapping(target = "harvestDate", ignore = true)
    @Mapping(target = "expiryDate", ignore = true)
    @Mapping(target = "price", ignore = true)
    @Mapping(target = "priceUnit", ignore = true)
    @Mapping(target = "availableQuantity", ignore = true)
    @Mapping(target = "quantityUnit", ignore = true)
    @Mapping(target = "minOrderQuantity", ignore = true)
    @Mapping(target = "certifications", ignore = true)
    @Mapping(target = "tags", ignore = true)
    @Mapping(target = "imageUrls", ignore = true)
    @Mapping(target = "primaryImageUrl", ignore = true)
    @Mapping(target = "packaging", ignore = true)
    @Mapping(target = "storageRequirements", ignore = true)
    @Mapping(target = "transportationRequirements", ignore = true)
    @Mapping(target = "supportsBulkOrders", ignore = true)
    @Mapping(target = "isSeasonal", ignore = true)
    @Mapping(target = "seasonInfo", ignore = true)
    @Mapping(target = "viewCount", ignore = true)
    @Mapping(target = "averageRating", ignore = true)
    @Mapping(target = "ratingCount", ignore = true)
    @Mapping(target = "slug", ignore = true)
    @Mapping(target = "metaKeywords", ignore = true)
    @Mapping(target = "metaDescription", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", expression = "java(LocalDateTime.now())")
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "status", source = "status")
    @Mapping(target = "visibility", source = "visibility")
    void updateStatusAndVisibility(@MappingTarget Product product, ProductStatusUpdate statusUpdate);
    
    /**
     * Convert string status to enum
     */
    default Product.ProductStatus mapStringToProductStatus(String status) {
        if (status == null) return null;
        try {
            return Product.ProductStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
    
    /**
     * Convert string visibility to enum
     */
    default Product.ProductVisibility mapStringToProductVisibility(String visibility) {
        if (visibility == null) return null;
        try {
            return Product.ProductVisibility.valueOf(visibility.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
    
    /**
     * Convert enum to string for responses
     */
    default String mapProductStatusToString(Product.ProductStatus status) {
        return status != null ? status.name() : null;
    }
    
    /**
     * Convert enum to string for responses
     */
    default String mapProductVisibilityToString(Product.ProductVisibility visibility) {
        return visibility != null ? visibility.name() : null;
    }
}