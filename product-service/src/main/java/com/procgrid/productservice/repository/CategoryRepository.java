package com.procgrid.productservice.repository;

import com.procgrid.productservice.mapper.mybatis.CategoryMyBatisMapper;
import com.procgrid.productservice.model.Category;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Repository layer for Category operations
 * Provides caching, transaction management, and hierarchical category logic
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class CategoryRepository {
    
    private final CategoryMyBatisMapper categoryMapper;
    
    /**
     * Create new category
     */
    @Transactional
    @CacheEvict(value = {"categories", "categoryTree", "categoryHierarchy"}, allEntries = true)
    public Category save(Category category) {
        log.debug("Creating new category: {}", category.getName());
        
        // Generate slug if not provided
        if (category.getSlug() == null || category.getSlug().isEmpty()) {
            category.setSlug(generateSlug(category.getName()));
        }
        
        // Validate slug uniqueness
        if (existsBySlugAndNotId(category.getSlug(), category.getId())) {
            category.setSlug(generateUniqueSlug(category.getSlug()));
        }
        
        // Calculate level and hierarchy
        calculateCategoryHierarchy(category);
        
        categoryMapper.insertCategory(category);
        log.info("Created category with ID: {} at level: {}", category.getId(), category.getLevel());
        
        return category;
    }
    
    /**
     * Update existing category
     */
    @Transactional
    @CachePut(value = "categories", key = "#category.id")
    @CacheEvict(value = {"categoryTree", "categoryHierarchy"}, allEntries = true)
    public Category update(Category category) {
        log.debug("Updating category: {}", category.getId());
        
        // Validate slug uniqueness if changed
        if (category.getSlug() != null && existsBySlugAndNotId(category.getSlug(), category.getId())) {
            category.setSlug(generateUniqueSlug(category.getSlug()));
        }
        
        // Recalculate hierarchy if needed
        if (category.getParentId() != null) {
            calculateCategoryHierarchy(category);
        }
        
        categoryMapper.updateCategory(category);
        log.info("Updated category: {}", category.getId());
        
        return category;
    }
    
    /**
     * Find category by ID with caching
     */
    @Cacheable(value = "categories", key = "#id")
    public Optional<Category> findById(Long id) {
        log.debug("Finding category by ID: {}", id);
        Category category = categoryMapper.findById(id);
        return Optional.ofNullable(category);
    }
    
    /**
     * Find category by slug with caching
     */
    @Cacheable(value = "categories", key = "'slug:' + #slug")
    public Optional<Category> findBySlug(String slug) {
        log.debug("Finding category by slug: {}", slug);
        Category category = categoryMapper.findBySlug(slug);
        return Optional.ofNullable(category);
    }
    
    /**
     * Find all root categories (cached)
     */
    @Cacheable(value = "categoryTree", key = "'root'")
    public List<Category> findRootCategories() {
        log.debug("Finding root categories");
        return categoryMapper.findRootCategories();
    }
    
    /**
     * Find child categories by parent ID
     */
    @Cacheable(value = "categoryTree", key = "'children:' + #parentId")
    public List<Category> findByParentId(Long parentId) {
        log.debug("Finding child categories for parent: {}", parentId);
        return categoryMapper.findByParentId(parentId);
    }
    
    /**
     * Find categories by level
     */
    @Cacheable(value = "categoryTree", key = "'level:' + #level")
    public List<Category> findByLevel(Integer level) {
        log.debug("Finding categories at level: {}", level);
        return categoryMapper.findByLevel(level);
    }
    
    /**
     * Find all categories (hierarchical)
     */
    @Cacheable(value = "categoryTree", key = "'all'")
    public List<Category> findAllCategories() {
        log.debug("Finding all categories");
        return categoryMapper.findAllCategories();
    }
    
    /**
     * Find featured categories
     */
    @Cacheable(value = "categoryTree", key = "'featured:' + #limit")
    public List<Category> findFeaturedCategories(int limit) {
        log.debug("Finding featured categories, limit: {}", limit);
        return categoryMapper.findFeaturedCategories(limit);
    }
    
    /**
     * Find categories with products
     */
    @Cacheable(value = "categoryTree", key = "'withProducts'")
    public List<Category> findCategoriesWithProducts() {
        log.debug("Finding categories with products");
        return categoryMapper.findCategoriesWithProducts();
    }
    
    /**
     * Search categories by name
     */
    @Cacheable(value = "categorySearch", key = "#query + ':' + #limit")
    public List<Category> searchCategoriesByName(String query, int limit) {
        log.debug("Searching categories by name: {}", query);
        return categoryMapper.searchCategoriesByName(query, limit);
    }
    
    /**
     * Get category hierarchy path
     */
    @Cacheable(value = "categoryHierarchy", key = "#categoryId")
    public List<Category> getCategoryHierarchy(Long categoryId) {
        log.debug("Getting category hierarchy for: {}", categoryId);
        return categoryMapper.getCategoryHierarchy(categoryId);
    }
    
    /**
     * Update product count for category
     */
    @Transactional
    @CacheEvict(value = {"categories", "categoryTree"}, key = "#categoryId")
    public void updateProductCount(Long categoryId, Long count) {
        log.debug("Updating product count for category: {} to {}", categoryId, count);
        categoryMapper.updateProductCount(categoryId, count);
    }
    
    /**
     * Update category hierarchy information
     */
    @Transactional
    @CacheEvict(value = {"categories", "categoryHierarchy"}, allEntries = true)
    public void updateCategoryHierarchy(Long id, Integer level, String categoryPath, 
                                       List<String> breadcrumb, String updatedBy) {
        log.debug("Updating category hierarchy for: {}, level: {}", id, level);
        categoryMapper.updateCategoryHierarchy(id, level, categoryPath, breadcrumb, updatedBy);
    }
    
    /**
     * Check if category has children
     */
    public boolean hasChildren(Long categoryId) {
        return categoryMapper.hasChildren(categoryId);
    }
    
    /**
     * Check if category has products
     */
    public boolean hasProducts(Long categoryId) {
        return categoryMapper.hasProducts(categoryId);
    }
    
    /**
     * Check if slug exists for different category
     */
    public boolean existsBySlugAndNotId(String slug, Long categoryId) {
        Long id = categoryId != null ? categoryId : 0L;
        return categoryMapper.existsBySlugAndNotId(slug, id);
    }
    
    /**
     * Check if category name exists for same parent (sibling check)
     */
    public boolean existsByNameAndParentId(String name, Long parentId) {
        return categoryMapper.existsByNameAndParentId(name, parentId);
    }
    
    /**
     * Update children count for parent category
     */
    @Transactional
    @CacheEvict(value = {"categories", "categoryTree"}, key = "#categoryId")
    public void updateChildrenCount(Long categoryId, int count) {
        log.debug("Updating children count for category: {} to {}", categoryId, count);
        categoryMapper.updateChildrenCount(categoryId, count);
    }
    
    /**
     * Delete category (soft delete) if no children or products
     */
    @Transactional
    @CacheEvict(value = {"categories", "categoryTree", "categoryHierarchy"}, allEntries = true)
    public boolean deleteCategory(Long id, String updatedBy) {
        log.debug("Attempting to delete category: {}", id);
        
        // Check if category has children
        if (hasChildren(id)) {
            log.warn("Cannot delete category {} - has children", id);
            return false;
        }
        
        // Check if category has products
        if (hasProducts(id)) {
            log.warn("Cannot delete category {} - has products", id);
            return false;
        }
        
        int rows = categoryMapper.deleteCategory(id, updatedBy);
        
        if (rows > 0) {
            log.info("Deleted category: {}", id);
            return true;
        }
        
        return false;
    }
    
    /**
     * Update category status (active/inactive)
     */
    @Transactional
    @CacheEvict(value = {"categories", "categoryTree"}, allEntries = true)
    public boolean updateCategoryStatus(Long id, Boolean active, String updatedBy) {
        log.debug("Updating category status: {} to active: {}", id, active);
        int rows = categoryMapper.updateCategoryStatus(id, active, updatedBy);
        
        if (rows > 0) {
            log.info("Updated category {} status to active: {}", id, active);
            return true;
        }
        
        return false;
    }
    
    /**
     * Update featured status
     */
    @Transactional
    @CacheEvict(value = {"categories", "categoryTree"}, allEntries = true)
    public boolean updateFeaturedStatus(Long id, Boolean featured, String updatedBy) {
        log.debug("Updating category featured status: {} to featured: {}", id, featured);
        int rows = categoryMapper.updateFeaturedStatus(id, featured, updatedBy);
        
        if (rows > 0) {
            log.info("Updated category {} featured status to: {}", id, featured);
            return true;
        }
        
        return false;
    }
    
    /**
     * Get category statistics
     */
    @Cacheable(value = "categoryStats", key = "'all'")
    public Map<String, Object> getCategoryStatistics() {
        log.debug("Getting category statistics");
        return categoryMapper.getCategoryStatistics();
    }
    
    /**
     * Find categories for breadcrumb
     */
    @Cacheable(value = "categoryBreadcrumb", key = "#categoryId")
    public List<Category> findCategoryPath(Long categoryId) {
        log.debug("Finding category path for: {}", categoryId);
        return categoryMapper.findCategoryPath(categoryId);
    }
    
    /**
     * Find all subcategory IDs (recursive)
     */
    @Cacheable(value = "categorySubtree", key = "#categoryId")
    public List<Long> findAllSubcategoryIds(Long categoryId) {
        log.debug("Finding all subcategory IDs for: {}", categoryId);
        return categoryMapper.findAllSubcategoryIds(categoryId);
    }
    
    /**
     * Calculate category hierarchy information
     */
    private void calculateCategoryHierarchy(Category category) {
        if (category.getParentId() == null) {
            // Root category
            category.setLevel(0);
            category.setCategoryPath(category.getName());
            category.setBreadcrumb(List.of(category.getName()));
        } else {
            // Get parent category hierarchy
            Optional<Category> parentOpt = findById(category.getParentId());
            if (parentOpt.isPresent()) {
                Category parent = parentOpt.get();
                category.setLevel(parent.getLevel() + 1);
                category.setCategoryPath(parent.getCategoryPath() + " > " + category.getName());
                
                // Build breadcrumb
                List<String> breadcrumb = parent.getBreadcrumb() != null ? 
                    parent.getBreadcrumb() : List.of(parent.getName());
                breadcrumb.add(category.getName());
                category.setBreadcrumb(breadcrumb);
            } else {
                log.warn("Parent category not found: {}", category.getParentId());
                category.setLevel(0);
                category.setCategoryPath(category.getName());
                category.setBreadcrumb(List.of(category.getName()));
            }
        }
    }
    
    /**
     * Generate URL-friendly slug from name
     */
    private String generateSlug(String name) {
        if (name == null) return "";
        
        return name.toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "") // Remove special characters
                .replaceAll("\\s+", "-") // Replace spaces with hyphens
                .replaceAll("-+", "-") // Remove duplicate hyphens
                .replaceAll("^-|-$", ""); // Remove leading/trailing hyphens
    }
    
    /**
     * Generate unique slug by appending number
     */
    private String generateUniqueSlug(String baseSlug) {
        String uniqueSlug = baseSlug;
        int counter = 1;
        
        while (existsBySlugAndNotId(uniqueSlug, null)) {
            uniqueSlug = baseSlug + "-" + counter;
            counter++;
        }
        
        return uniqueSlug;
    }

    /**
     * Find child categories
     */
    public List<Category> findChildCategories(Long parentId) {
        log.debug("Finding child categories for parent: {}", parentId);
        return categoryMapper.findByParentId(parentId);
    }

    /**
     * Get category breadcrumbs
     */
    public List<String> getCategoryBreadcrumbs(Long categoryId) {
        log.debug("Getting breadcrumbs for category: {}", categoryId);
        return categoryMapper.getCategoryBreadcrumbs(categoryId);
    }

    /**
     * Find all categories with pagination
     */
    public List<Category> findAllCategories(int offset, int limit) {
        log.debug("Finding all categories with offset: {} and limit: {}", offset, limit);
        return categoryMapper.findAllWithPagination(offset, limit);
    }

    /**
     * Count all categories
     */
    public long countAllCategories() {
        log.debug("Counting all categories");
        return categoryMapper.countAll();
    }

    /**
     * Find active categories with pagination
     */
    public List<Category> findActiveCategories(int offset, int limit) {
        log.debug("Finding active categories with offset: {} and limit: {}", offset, limit);
        return categoryMapper.findActiveWithPagination(offset, limit);
    }

    /**
     * Find categories by level
     */
    public List<Category> findCategoriesByLevel(int level) {
        log.debug("Finding categories by level: {}", level);
        return categoryMapper.findByLevel(level);
    }

    /**
     * Find leaf categories
     */
    public List<Category> findLeafCategories() {
        log.debug("Finding leaf categories");
        return categoryMapper.findLeafCategories();
    }

    /**
     * Find popular categories
     */
    public List<Category> findPopularCategories(int limit) {
        log.debug("Finding popular categories with limit: {}", limit);
        return categoryMapper.findPopularCategories(limit);
    }

    /**
     * Update category status
     */
    @Transactional
    @CacheEvict(value = "categories", allEntries = true)
    public boolean updateStatus(Long categoryId, boolean active, String updatedBy) {
        log.debug("Updating status for category: {} to active: {}", categoryId, active);
        return categoryMapper.updateStatus(categoryId, active, updatedBy) > 0;
    }

    /**
     * Move category to new parent
     */
    @Transactional
    @CacheEvict(value = "categories", allEntries = true)
    public boolean moveCategory(Long categoryId, Long newParentId, int newLevel, String newPath, String updatedBy) {
        log.debug("Moving category: {} to parent: {}", categoryId, newParentId);
        return categoryMapper.moveCategory(categoryId, newParentId, newLevel, newPath, updatedBy) > 0;
    }

    /**
     * Get category statistics
     */
    public Map<String, Object> getCategoryStats(Long categoryId) {
        log.debug("Getting stats for category: {}", categoryId);
        return categoryMapper.getCategoryStats(categoryId);
    }

    /**
     * Rebuild hierarchy
     */
    @Transactional
    @CacheEvict(value = "categories", allEntries = true)
    public void rebuildHierarchy() {
        log.debug("Rebuilding category hierarchy");
        categoryMapper.rebuildHierarchy();
    }

    /**
     * Find active children
     */
    public List<Category> findActiveChildren(Long parentId) {
        log.debug("Finding active children for parent: {}", parentId);
        return categoryMapper.findActiveChildren(parentId);
    }

    /**
     * Get active product count
     */
    public long getActiveProductCount(Long categoryId) {
        log.debug("Getting active product count for category: {}", categoryId);
        return categoryMapper.getActiveProductCount(categoryId);
    }

    /**
     * Get category descendants
     */
    public List<Category> getCategoryDescendants(Long categoryId) {
        log.debug("Getting descendants for category: {}", categoryId);
        return categoryMapper.getCategoryDescendants(categoryId);
    }

    /**
     * Get children count
     */
    public Long getChildrenCount(Long categoryId) {
        log.debug("Getting children count for category: {}", categoryId);
        return categoryMapper.getChildrenCount(categoryId);
    }

    /**
     * Get product count
     */
    public long getProductCount(Long categoryId) {
        log.debug("Getting product count for category: {}", categoryId);
        return categoryMapper.getProductCount(categoryId);
    }

    /**
     * Deactivate child categories
     */
    @Transactional
    @CacheEvict(value = "categories", allEntries = true)
    public void deactivateChildCategories(Long parentId, String updatedBy) {
        log.debug("Deactivating child categories for parent: {}", parentId);
        categoryMapper.deactivateChildCategories(parentId, updatedBy);
    }

    /**
     * Update category path
     */
    @Transactional
    @CacheEvict(value = "categories", allEntries = true)
    public void updateCategoryPath(Long categoryId, String path, int level, String updatedBy) {
        log.debug("Updating path for category: {} to: {}", categoryId, path);
        categoryMapper.updateCategoryPath(categoryId, path, level, updatedBy);
    }

    /**
     * Check if slug exists for parent
     */
    public boolean existsBySlugAndParentId(String slug, Long parentId) {
        log.debug("Checking if slug exists: {} for parent: {}", slug, parentId);
        return categoryMapper.existsBySlugAndParentId(slug, parentId);
    }
}