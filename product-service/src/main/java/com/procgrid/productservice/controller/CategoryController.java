package com.procgrid.productservice.controller;

import com.procgrid.productservice.model.Category;
import com.procgrid.productservice.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST Controller for Category operations
 * Provides API endpoints for hierarchical category management
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
@Tag(name = "Category Management", description = "APIs for managing product categories")
@SecurityRequirement(name = "bearerAuth")
public class CategoryController {
    
    private final CategoryService categoryService;
    
    /**
     * Create new category
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create new category", description = "Create a new product category")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Category created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request data"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Admin role required")
    })
    public ResponseEntity<Category> createCategory(
            @Parameter(description = "Category name") @RequestParam String name,
            @Parameter(description = "Category description") @RequestParam(required = false) String description,
            @Parameter(description = "Parent category ID") @RequestParam(required = false) Long parentId,
            @Parameter(description = "Category image URL") @RequestParam(required = false) String imageUrl,
            @RequestBody(required = false) Map<String, Object> metadata) {
        
        log.debug("Creating new category: {} with parent: {}", name, parentId);
        
        Category response = categoryService.createCategory(name, description, parentId, imageUrl, metadata);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
    
    /**
     * Update existing category
     */
    @PutMapping("/{categoryId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update category", description = "Update an existing category")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Category updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request data"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Admin role required"),
        @ApiResponse(responseCode = "404", description = "Category not found")
    })
    public ResponseEntity<Category> updateCategory(
            @Parameter(description = "Category ID") @PathVariable Long categoryId,
            @Parameter(description = "Category name") @RequestParam(required = false) String name,
            @Parameter(description = "Category description") @RequestParam(required = false) String description,
            @Parameter(description = "Category image URL") @RequestParam(required = false) String imageUrl,
            @RequestBody(required = false) Map<String, Object> metadata) {
        
        log.debug("Updating category: {}", categoryId);
        
        Category response = categoryService.updateCategory(categoryId, name, description, imageUrl, metadata);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get category by ID
     */
    @GetMapping("/{categoryId}")
    @Operation(summary = "Get category by ID", description = "Retrieve category details by ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Category found"),
        @ApiResponse(responseCode = "404", description = "Category not found")
    })
    public ResponseEntity<Category> getCategory(
            @Parameter(description = "Category ID") @PathVariable Long categoryId) {
        
        log.debug("Getting category: {}", categoryId);
        
        Category response = categoryService.getCategory(categoryId);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get category by slug
     */
    @GetMapping("/slug/{slug}")
    @Operation(summary = "Get category by slug", description = "Retrieve category details by slug")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Category found"),
        @ApiResponse(responseCode = "404", description = "Category not found")
    })
    public ResponseEntity<Category> getCategoryBySlug(
            @Parameter(description = "Category slug") @PathVariable String slug) {
        
        log.debug("Getting category by slug: {}", slug);
        
        Category response = categoryService.getCategoryBySlug(slug);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get root categories
     */
    @GetMapping("/roots")
    @Operation(summary = "Get root categories", description = "Retrieve all root categories")
    public ResponseEntity<List<Category>> getRootCategories() {
        
        log.debug("Getting root categories");
        
        List<Category> response = categoryService.getRootCategories();
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get child categories
     */
    @GetMapping("/{categoryId}/children")
    @Operation(summary = "Get child categories", description = "Retrieve child categories of a parent category")
    public ResponseEntity<List<Category>> getChildCategories(
            @Parameter(description = "Parent category ID") @PathVariable Long categoryId) {
        
        log.debug("Getting child categories for: {}", categoryId);
        
        List<Category> response = categoryService.getChildCategories(categoryId);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get category hierarchy
     */
    @GetMapping("/{categoryId}/hierarchy")
    @Operation(summary = "Get category hierarchy", description = "Retrieve the full hierarchy starting from a category")
    public ResponseEntity<List<Category>> getCategoryHierarchy(
            @Parameter(description = "Category ID") @PathVariable Long categoryId) {
        
        log.debug("Getting category hierarchy for: {}", categoryId);
        
        List<Category> response = categoryService.getCategoryHierarchy(categoryId);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get category breadcrumbs
     */
    @GetMapping("/{categoryId}/breadcrumbs")
    @Operation(summary = "Get category breadcrumbs", description = "Retrieve breadcrumb navigation for a category")
    public ResponseEntity<List<String>> getCategoryBreadcrumbs(
            @Parameter(description = "Category ID") @PathVariable Long categoryId) {
        
        log.debug("Getting breadcrumbs for category: {}", categoryId);
        
        List<String> response = categoryService.getCategoryBreadcrumbs(categoryId);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get all categories with pagination
     */
    @GetMapping
    @Operation(summary = "Get all categories", description = "Retrieve all categories with pagination")
    public ResponseEntity<Page<Category>> getAllCategories(
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
        
        log.debug("Getting all categories, page: {}, size: {}", page, size);
        
        List<Category> categories = categoryService.getAllCategories(page, size);
        Long totalCount = categoryService.countAllCategories();
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Category> response = new PageImpl<>(categories, pageable, totalCount);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get active categories
     */
    @GetMapping("/active")
    @Operation(summary = "Get active categories", description = "Retrieve only active categories")
    public ResponseEntity<Page<Category>> getActiveCategories(
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
        
        log.debug("Getting active categories, page: {}, size: {}", page, size);
        
        List<Category> categories = categoryService.getActiveCategories(page, size);
        Pageable pageable = PageRequest.of(page, size);
        Page<Category> response = new PageImpl<>(categories, pageable, categories.size());
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Search categories
     */
    @GetMapping("/search")
    @Operation(summary = "Search categories", description = "Search categories by name")
    public ResponseEntity<Page<Category>> searchCategories(
            @Parameter(description = "Search query") @RequestParam String query,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
        
        log.debug("Searching categories with query: {}", query);
        
        List<Category> categories = categoryService.searchCategories(query, page, size);
        Pageable pageable = PageRequest.of(page, size);
        Page<Category> response = new PageImpl<>(categories, pageable, categories.size());
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get categories by level
     */
    @GetMapping("/level/{level}")
    @Operation(summary = "Get categories by level", description = "Retrieve categories at a specific hierarchy level")
    public ResponseEntity<List<Category>> getCategoriesByLevel(
            @Parameter(description = "Hierarchy level") @PathVariable int level) {
        
        log.debug("Getting categories at level: {}", level);
        
        List<Category> response = categoryService.getCategoriesByLevel(level);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get leaf categories
     */
    @GetMapping("/leaves")
    @Operation(summary = "Get leaf categories", description = "Retrieve categories without children")
    public ResponseEntity<List<Category>> getLeafCategories() {
        
        log.debug("Getting leaf categories");
        
        List<Category> response = categoryService.getLeafCategories();
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get popular categories
     */
    @GetMapping("/popular")
    @Operation(summary = "Get popular categories", description = "Retrieve popular categories by product count")
    public ResponseEntity<List<Category>> getPopularCategories(
            @Parameter(description = "Limit") @RequestParam(defaultValue = "10") int limit) {
        
        log.debug("Getting popular categories, limit: {}", limit);
        
        List<Category> response = categoryService.getPopularCategories(limit);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Update category status
     */
    @PatchMapping("/{categoryId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update category status", description = "Activate or deactivate a category")
    public ResponseEntity<Void> updateCategoryStatus(
            @Parameter(description = "Category ID") @PathVariable Long categoryId,
            @Parameter(description = "Is active") @RequestParam boolean isActive) {
        
        log.debug("Updating category {} status to {}", categoryId, isActive);
        categoryService.updateCategoryStatus(categoryId, isActive);
        return ResponseEntity.ok().build();
    }
    
    /**
     * Move category
     */
    @PatchMapping("/{categoryId}/move")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Move category", description = "Move category to a different parent")
    public ResponseEntity<Void> moveCategory(
            @Parameter(description = "Category ID") @PathVariable Long categoryId,
            @Parameter(description = "New parent category ID") @RequestParam(required = false) Long newParentId) {
        
        log.debug("Moving category {} to new parent: {}", categoryId, newParentId);
        categoryService.moveCategory(categoryId, newParentId);
        return ResponseEntity.ok().build();
    }
    
    /**
     * Delete category
     */
    @DeleteMapping("/{categoryId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete category", description = "Soft delete a category")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Category deleted successfully"),
        @ApiResponse(responseCode = "400", description = "Cannot delete category with children or products"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Admin role required"),
        @ApiResponse(responseCode = "404", description = "Category not found")
    })
    public ResponseEntity<Void> deleteCategory(
            @Parameter(description = "Category ID") @PathVariable Long categoryId) {
        
        log.debug("Deleting category: {}", categoryId);
        categoryService.deleteCategory(categoryId);
        return ResponseEntity.noContent().build();
    }
    
    /**
     * Get category statistics
     */
    @GetMapping("/{categoryId}/stats")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get category statistics", description = "Retrieve category statistics")
    public ResponseEntity<Map<String, Object>> getCategoryStats(
            @Parameter(description = "Category ID") @PathVariable Long categoryId) {
        
        log.debug("Getting statistics for category: {}", categoryId);
        
        Map<String, Object> stats = categoryService.getCategoryStats(categoryId);
        return ResponseEntity.ok(stats);
    }
    
    /**
     * Rebuild category hierarchy
     */
    @PostMapping("/rebuild-hierarchy")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Rebuild category hierarchy", description = "Rebuild category paths and levels")
    public ResponseEntity<Void> rebuildCategoryHierarchy() {
        
        log.debug("Rebuilding category hierarchy");
        categoryService.rebuildCategoryHierarchy();
        return ResponseEntity.ok().build();
    }
}