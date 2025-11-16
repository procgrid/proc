package com.procgrid.productservice.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Category entity for product categorization
 * Supports hierarchical category structure
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Category {
    
    /**
     * Unique identifier for the category
     */
    private Long id;
    
    /**
     * Parent category ID (null for root categories)
     */
    private Long parentId;
    
    /**
     * Category name
     */
    @NotBlank(message = "Category name is required")
    @Size(min = 2, max = 100, message = "Category name must be between 2 and 100 characters")
    private String name;
    
    /**
     * Category description
     */
    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    private String description;
    
    /**
     * SEO-friendly URL slug
     */
    @Pattern(regexp = "^[a-z0-9]+(?:-[a-z0-9]+)*$", message = "Invalid URL slug format")
    private String slug;
    
    /**
     * Category icon URL or class
     */
    private String icon;
    
    /**
     * Category image URL
     */
    private String imageUrl;
    
    /**
     * Display order for sorting
     */
    @Min(value = 0, message = "Sort order cannot be negative")
    @Builder.Default
    private Integer sortOrder = 0;
    
    /**
     * Category level in hierarchy (0 for root)
     */
    @Min(value = 0, message = "Level cannot be negative")
    @Max(value = 10, message = "Maximum category depth is 10")
    @Builder.Default
    private Integer level = 0;
    
    /**
     * Whether category is active
     */
    @Builder.Default
    private Boolean active = true;
    
    /**
     * Whether category is featured
     */
    @Builder.Default
    private Boolean featured = false;
    
    /**
     * Number of products in this category
     */
    @Builder.Default
    private Long productCount = 0L;
    
    /**
     * Number of direct child categories
     */
    @Builder.Default
    private Integer childrenCount = 0;
    
    /**
     * Category tags for search
     */
    private List<String> tags;
    
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
     * Full category path (e.g., "Fruits > Seasonal Fruits > Mangoes")
     */
    private String categoryPath;
    
    /**
     * Category breadcrumb trail (list of parent category names)
     */
    private List<String> breadcrumb;
    
    /**
     * Additional metadata for the category
     */
    private Map<String, Object> metadata;
    
    /**
     * Full hierarchical path (e.g., "/fruits/seasonal/mangoes")
     */
    private String path;
    
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
     * Check if this is a root category
     */
    public boolean isRootCategory() {
        return parentId == null;
    }
    
    /**
     * Check if category can have subcategories
     */
    public boolean canHaveSubcategories() {
        return level != null && level < 10; // Max depth limit
    }
    
    /**
     * Generate category path string
     */
    public String generateCategoryPath(List<String> parentNames) {
        if (parentNames == null || parentNames.isEmpty()) {
            return name;
        }
        return String.join(" > ", parentNames) + " > " + name;
    }
}