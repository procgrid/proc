package com.procgrid.productservice.mapper;

import com.procgrid.productservice.model.Category;
import org.mapstruct.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import java.util.List;

/**
 * MapStruct mapper for Category entity conversions
 */
@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    imports = {LocalDateTime.class}
)
public interface CategoryMapper {
    
    /**
     * Convert Category entity to response DTO
     */
    CategoryResponse toResponse(Category category);
    
    /**
     * Convert list of Category entities to response DTOs
     */
    List<CategoryResponse> toResponseList(List<Category> categories);
    
    /**
     * Convert Category entity to summary response DTO
     */
    CategorySummaryResponse toSummaryResponse(Category category);
    
    /**
     * Convert list of Category entities to summary response DTOs
     */
    List<CategorySummaryResponse> toSummaryResponseList(List<Category> categories);
    
    /**
     * Convert CategoryCreateRequest DTO to Category entity
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "productCount", constant = "0L")
    @Mapping(target = "categoryPath", ignore = true) // Will be calculated by service
    @Mapping(target = "breadcrumb", ignore = true) // Will be calculated by service
    @Mapping(target = "createdAt", expression = "java(LocalDateTime.now())")
    @Mapping(target = "updatedAt", expression = "java(LocalDateTime.now())")
    @Mapping(target = "createdBy", ignore = true) // Will be set from security context
    @Mapping(target = "updatedBy", ignore = true) // Will be set from security context
    @Mapping(target = "deleted", constant = "false")
    Category toEntity(CategoryCreateRequest request);
    
    /**
     * Update Category entity from CategoryUpdateRequest DTO
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "parentId", ignore = true) // Parent cannot be changed via update
    @Mapping(target = "level", ignore = true) // Level is calculated
    @Mapping(target = "productCount", ignore = true)
    @Mapping(target = "categoryPath", ignore = true) // Will be recalculated by service
    @Mapping(target = "breadcrumb", ignore = true) // Will be recalculated by service
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", expression = "java(LocalDateTime.now())")
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true) // Will be set from security context
    @Mapping(target = "deleted", ignore = true)
    void updateEntityFromRequest(@MappingTarget Category category, CategoryUpdateRequest request);
}

/**
 * DTO for category creation requests
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class CategoryCreateRequest {
    
    private Long parentId;
    
    @NotBlank(message = "Category name is required")
    @Size(min = 2, max = 100, message = "Category name must be between 2 and 100 characters")
    private String name;
    
    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    private String description;
    
    @Pattern(regexp = "^[a-z0-9]+(?:-[a-z0-9]+)*$", message = "Invalid URL slug format")
    private String slug;
    
    private String icon;
    private String imageUrl;
    
    @Min(value = 0, message = "Sort order cannot be negative")
    @Builder.Default
    private Integer sortOrder = 0;
    
    @Builder.Default
    private Boolean active = true;
    
    @Builder.Default
    private Boolean featured = false;
    
    private List<String> tags;
    private String metaKeywords;
    
    @Size(max = 160, message = "Meta description cannot exceed 160 characters")
    private String metaDescription;
}

/**
 * DTO for category update requests
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class CategoryUpdateRequest {
    
    @Size(min = 2, max = 100, message = "Category name must be between 2 and 100 characters")
    private String name;
    
    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    private String description;
    
    @Pattern(regexp = "^[a-z0-9]+(?:-[a-z0-9]+)*$", message = "Invalid URL slug format")
    private String slug;
    
    private String icon;
    private String imageUrl;
    
    @Min(value = 0, message = "Sort order cannot be negative")
    private Integer sortOrder;
    
    private Boolean active;
    private Boolean featured;
    
    private List<String> tags;
    private String metaKeywords;
    
    @Size(max = 160, message = "Meta description cannot exceed 160 characters")
    private String metaDescription;
}

/**
 * DTO for category response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class CategoryResponse {
    
    private Long id;
    private Long parentId;
    private String name;
    private String description;
    private String slug;
    private String icon;
    private String imageUrl;
    private Integer sortOrder;
    private Integer level;
    private Boolean active;
    private Boolean featured;
    private Long productCount;
    private List<String> tags;
    private String metaKeywords;
    private String metaDescription;
    private String categoryPath;
    private List<String> breadcrumb;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
    
    private String createdBy;
    private String updatedBy;
    
    // Additional fields for tree structure
    private List<CategoryResponse> children;
    private Boolean hasChildren;
}

/**
 * DTO for category summary response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class CategorySummaryResponse {
    
    private Long id;
    private Long parentId;
    private String name;
    private String slug;
    private String icon;
    private String imageUrl;
    private Integer sortOrder;
    private Integer level;
    private Boolean active;
    private Boolean featured;
    private Long productCount;
    private String categoryPath;
    private Boolean hasChildren;
}