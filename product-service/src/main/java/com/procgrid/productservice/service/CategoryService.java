package com.procgrid.productservice.service;

import com.procgrid.productservice.exception.CategoryNotFoundException;
import com.procgrid.productservice.exception.UnauthorizedAccessException;
import com.procgrid.productservice.exception.ValidationException;
import com.procgrid.productservice.mapper.CategoryMapper;
import com.procgrid.productservice.model.Category;
import com.procgrid.productservice.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service layer for Category operations
 * Provides business logic for hierarchical category management
 */
@Slf4j
@Service
public class CategoryService {
    
    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    
    public CategoryService(
            CategoryRepository categoryRepository, 
            @Qualifier("categoryMapperImpl") CategoryMapper categoryMapper, 
            KafkaTemplate<String, Object> kafkaTemplate) {
        this.categoryRepository = categoryRepository;
        this.categoryMapper = categoryMapper;
        this.kafkaTemplate = kafkaTemplate;
    }
    
    /**
     * Create new category
     */
    @Transactional
    public Category createCategory(String name, String description, Long parentId, 
                                 String imageUrl, Map<String, Object> metadata) {
        log.debug("Creating new category: {} with parent: {}", name, parentId);
        
        validateCategoryName(name);
        
        // Validate parent category if provided
        Category parentCategory = null;
        if (parentId != null) {
            parentCategory = categoryRepository.findById(parentId)
                .orElseThrow(() -> new CategoryNotFoundException("Parent category not found: " + parentId));
            
            // Check depth limit (e.g., max 5 levels)
            if (parentCategory.getLevel() >= 4) {
                throw new ValidationException("Maximum category depth exceeded");
            }
        }
        
        // Check for duplicate names at the same level
        if (categoryRepository.existsByNameAndParentId(name, parentId)) {
            throw new ValidationException("Category with name '" + name + 
                "' already exists at this level");
        }
        
        // Create category
        Category category = Category.builder()
            .name(name)
            .description(description)
            .parentId(parentId)
            .imageUrl(imageUrl)
            .metadata(metadata)
            .active(true)
            .createdBy(getCurrentUsername())
            .createdAt(LocalDateTime.now())
            .build();
        
        // Set level and path
        if (parentCategory != null) {
            category.setLevel(parentCategory.getLevel() + 1);
            category.setPath(parentCategory.getPath() + "/" + name.toLowerCase().replace(" ", "-"));
        } else {
            category.setLevel(0);
            category.setPath("/" + name.toLowerCase().replace(" ", "-"));
        }
        
        // Generate slug
        category.setSlug(generateSlug(name, parentId));
        
        Category savedCategory = categoryRepository.save(category);
        
        // Update hierarchy if parent exists
        if (parentCategory != null) {
            categoryRepository.updateChildrenCount(parentId, 1);
        }
        
        // Publish category created event
        publishCategoryEvent("category.created", savedCategory);
        
        log.info("Created category: {} with ID: {}", savedCategory.getName(), savedCategory.getId());
        return savedCategory;
    }
    
    /**
     * Update existing category
     */
    @Transactional
    @CacheEvict(value = {"categories", "categoryHierarchy", "categoryBreadcrumbs"}, allEntries = true)
    public Category updateCategory(Long id, String name, String description, 
                                 String imageUrl, Map<String, Object> metadata) {
        log.debug("Updating category: {}", id);
        
        Category existingCategory = categoryRepository.findById(id)
            .orElseThrow(() -> new CategoryNotFoundException("Category not found: " + id));
        
        // Validate name if changed
        if (name != null && !name.equals(existingCategory.getName())) {
            validateCategoryName(name);
            
            // Check for duplicates at the same level
            if (categoryRepository.existsByNameAndParentId(name, existingCategory.getParentId())) {
                throw new ValidationException("Category with name '" + name + 
                    "' already exists at this level");
            }
        }
        
        // Update fields
        if (name != null) {
            existingCategory.setName(name);
            existingCategory.setSlug(generateSlug(name, existingCategory.getParentId()));
        }
        if (description != null) {
            existingCategory.setDescription(description);
        }
        if (imageUrl != null) {
            existingCategory.setImageUrl(imageUrl);
        }
        if (metadata != null) {
            existingCategory.setMetadata(metadata);
        }
        
        existingCategory.setUpdatedBy(getCurrentUsername());
        existingCategory.setUpdatedAt(LocalDateTime.now());
        
        Category updatedCategory = categoryRepository.update(existingCategory);
        
        // Publish category updated event
        publishCategoryEvent("category.updated", updatedCategory);
        
        log.info("Updated category: {} with ID: {}", updatedCategory.getName(), updatedCategory.getId());
        return updatedCategory;
    }
    
    /**
     * Get category by ID
     */
    @Cacheable(value = "categories", key = "#id")
    public Category getCategory(Long id) {
        log.debug("Getting category by ID: {}", id);
        
        return categoryRepository.findById(id)
            .orElseThrow(() -> new CategoryNotFoundException("Category not found: " + id));
    }
    
    /**
     * Get category by slug
     */
    @Cacheable(value = "categories", key = "'slug:' + #slug")
    public Category getCategoryBySlug(String slug) {
        log.debug("Getting category by slug: {}", slug);
        
        return categoryRepository.findBySlug(slug)
            .orElseThrow(() -> new CategoryNotFoundException("Category not found with slug: " + slug));
    }
    
    /**
     * Get all root categories
     */
    @Cacheable(value = "categoryHierarchy", key = "'roots'")
    public List<Category> getRootCategories() {
        log.debug("Getting root categories");
        return categoryRepository.findRootCategories();
    }
    
    /**
     * Get child categories
     */
    @Cacheable(value = "categoryHierarchy", key = "'children:' + #parentId")
    public List<Category> getChildCategories(Long parentId) {
        log.debug("Getting child categories for parent: {}", parentId);
        return categoryRepository.findChildCategories(parentId);
    }
    
    /**
     * Get category hierarchy starting from a category
     */
    @Cacheable(value = "categoryHierarchy", key = "'hierarchy:' + #categoryId")
    public List<Category> getCategoryHierarchy(Long categoryId) {
        log.debug("Getting category hierarchy for: {}", categoryId);
        return categoryRepository.getCategoryHierarchy(categoryId);
    }
    
    /**
     * Get category breadcrumbs
     */
    @Cacheable(value = "categoryBreadcrumbs", key = "#categoryId")
    public List<String> getCategoryBreadcrumbs(Long categoryId) {
        log.debug("Getting category breadcrumbs for: {}", categoryId);
        return categoryRepository.getCategoryBreadcrumbs(categoryId);
    }
    
    /**
     * Get all categories with pagination
     */
    public List<Category> getAllCategories(int page, int size) {
        log.debug("Getting all categories, page: {}, size: {}", page, size);
        return categoryRepository.findAllCategories(page, size);
    }
    
    /**
     * Count all categories
     */
    public Long countAllCategories() {
        return categoryRepository.countAllCategories();
    }
    
    /**
     * Get active categories with pagination
     */
    @Cacheable(value = "categories", key = "'active:' + #page + ':' + #size")
    public List<Category> getActiveCategories(int page, int size) {
        log.debug("Getting active categories, page: {}, size: {}", page, size);
        return categoryRepository.findActiveCategories(page, size);
    }
    
    /**
     * Search categories by name
     */
    public List<Category> searchCategories(String query, int page, int size) {
        log.debug("Searching categories with query: {}, page: {}, size: {}", query, page, size);
        // Calculate offset and use size as limit
        return categoryRepository.searchCategoriesByName(query, size);
    }
    
    /**
     * Get categories by level
     */
    @Cacheable(value = "categories", key = "'level:' + #level")
    public List<Category> getCategoriesByLevel(int level) {
        log.debug("Getting categories at level: {}", level);
        return categoryRepository.findCategoriesByLevel(level);
    }
    
    /**
     * Get leaf categories (categories without children)
     */
    @Cacheable(value = "categories", key = "'leaves'")
    public List<Category> getLeafCategories() {
        log.debug("Getting leaf categories");
        return categoryRepository.findLeafCategories();
    }
    
    /**
     * Get popular categories by product count
     */
    @Cacheable(value = "categories", key = "'popular:' + #limit")
    public List<Category> getPopularCategories(int limit) {
        log.debug("Getting popular categories, limit: {}", limit);
        return categoryRepository.findPopularCategories(limit);
    }
    
    /**
     * Update category status
     */
    @Transactional
    @CacheEvict(value = {"categories", "categoryHierarchy"}, allEntries = true)
    public void updateCategoryStatus(Long id, boolean isActive) {
        log.debug("Updating category status: {} to {}", id, isActive);
        
        Category category = categoryRepository.findById(id)
            .orElseThrow(() -> new CategoryNotFoundException("Category not found: " + id));
        
        // If deactivating, check if it has active children or products
        if (!isActive) {
            validateCategoryDeactivation(category);
        }
        
        boolean updated = categoryRepository.updateStatus(id, isActive, getCurrentUsername());
        
        if (updated) {
            // If deactivating, also deactivate children
            if (!isActive) {
                deactivateChildCategories(id);
            }
            
            // Publish status changed event
            Map<String, Object> eventData = Map.of(
                "categoryId", id,
                "isActive", isActive,
                "updatedBy", getCurrentUsername()
            );
            kafkaTemplate.send("category.status.changed", eventData);
            
            log.info("Updated category {} status to {}", id, isActive);
        }
    }
    
    /**
     * Move category to different parent
     */
    @Transactional
    @CacheEvict(value = {"categories", "categoryHierarchy", "categoryBreadcrumbs"}, allEntries = true)
    public void moveCategory(Long categoryId, Long newParentId) {
        log.debug("Moving category: {} to new parent: {}", categoryId, newParentId);
        
        Category category = categoryRepository.findById(categoryId)
            .orElseThrow(() -> new CategoryNotFoundException("Category not found: " + categoryId));
        
        Category newParent = null;
        if (newParentId != null) {
            newParent = categoryRepository.findById(newParentId)
                .orElseThrow(() -> new CategoryNotFoundException("New parent category not found: " + newParentId));
            
            // Validate move is allowed
            validateCategoryMove(category, newParent);
        }
        
        Long oldParentId = category.getParentId();
        
        // Update category
        int newLevel = newParent != null ? newParent.getLevel() + 1 : 0;
        String newPath = newParent != null 
            ? newParent.getPath() + "/" + category.getName().toLowerCase().replace(" ", "-")
            : "/" + category.getName().toLowerCase().replace(" ", "-");
        
        boolean moved = categoryRepository.moveCategory(categoryId, newParentId, newLevel, 
            newPath, getCurrentUsername());
        
        if (moved) {
            // Update children counts
            if (oldParentId != null) {
                categoryRepository.updateChildrenCount(oldParentId, -1);
            }
            if (newParentId != null) {
                categoryRepository.updateChildrenCount(newParentId, 1);
            }
            
            // Update descendant paths if needed
            updateDescendantPaths(categoryId, newPath, newLevel);
            
            // Publish category moved event
            Map<String, Object> eventData = Map.of(
                "categoryId", categoryId,
                "oldParentId", oldParentId,
                "newParentId", newParentId,
                "updatedBy", getCurrentUsername()
            );
            kafkaTemplate.send("category.moved", eventData);
            
            log.info("Moved category {} from parent {} to parent {}", 
                categoryId, oldParentId, newParentId);
        }
    }
    
    /**
     * Delete category (soft delete)
     */
    @Transactional
    @CacheEvict(value = {"categories", "categoryHierarchy", "categoryBreadcrumbs"}, allEntries = true)
    public void deleteCategory(Long id) {
        log.debug("Deleting category: {}", id);
        
        Category category = categoryRepository.findById(id)
            .orElseThrow(() -> new CategoryNotFoundException("Category not found: " + id));
        
        validateCategoryDeletion(category);
        
        boolean deleted = categoryRepository.deleteCategory(id, getCurrentUsername());
        
        if (deleted) {
            // Update parent's children count
            if (category.getParentId() != null) {
                categoryRepository.updateChildrenCount(category.getParentId(), -1);
            }
            
            // Publish category deleted event
            publishCategoryEvent("category.deleted", category);
            
            log.info("Deleted category: {} with ID: {}", category.getName(), id);
        }
    }
    
    /**
     * Get category statistics
     */
    @Cacheable(value = "categoryStats", key = "#categoryId")
    public Map<String, Object> getCategoryStats(Long categoryId) {
        log.debug("Getting category statistics for: {}", categoryId);
        return categoryRepository.getCategoryStats(categoryId);
    }
    
    /**
     * Rebuild category paths and levels
     */
    @Transactional
    @CacheEvict(value = {"categories", "categoryHierarchy", "categoryBreadcrumbs"}, allEntries = true)
    public void rebuildCategoryHierarchy() {
        log.debug("Rebuilding category hierarchy");
        categoryRepository.rebuildHierarchy();
        log.info("Rebuilt category hierarchy");
    }
    
    // Private helper methods
    
    private void validateCategoryName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new ValidationException("Category name is required");
        }
        
        if (name.length() > 100) {
            throw new ValidationException("Category name cannot exceed 100 characters");
        }
        
        // Check for invalid characters
        if (!name.matches("^[a-zA-Z0-9\\s\\-_&()]+$")) {
            throw new ValidationException("Category name contains invalid characters");
        }
    }
    
    private void validateCategoryDeactivation(Category category) {
        // Check if category has active children
        List<Category> activeChildren = categoryRepository.findActiveChildren(category.getId());
        if (!activeChildren.isEmpty()) {
            throw new ValidationException("Cannot deactivate category with active children");
        }
        
        // Check if category has active products
        Long activeProductCount = categoryRepository.getActiveProductCount(category.getId());
        if (activeProductCount > 0) {
            throw new ValidationException("Cannot deactivate category with active products");
        }
    }
    
    private void validateCategoryMove(Category category, Category newParent) {
        // Cannot move to itself
        if (category.getId().equals(newParent.getId())) {
            throw new ValidationException("Cannot move category to itself");
        }
        
        // Cannot move to its own descendant
        List<Category> descendants = categoryRepository.getCategoryDescendants(category.getId());
        if (descendants.stream().anyMatch(desc -> desc.getId().equals(newParent.getId()))) {
            throw new ValidationException("Cannot move category to its own descendant");
        }
        
        // Check depth limit
        if (newParent.getLevel() >= 4) {
            throw new ValidationException("Maximum category depth exceeded");
        }
        
        // Check for name conflicts at new location
        if (categoryRepository.existsByNameAndParentId(category.getName(), newParent.getId())) {
            throw new ValidationException("Category with same name already exists at destination");
        }
    }
    
    private void validateCategoryDeletion(Category category) {
        // Check if category has children
        Long childrenCount = categoryRepository.getChildrenCount(category.getId());
        if (childrenCount > 0L) {
            throw new ValidationException("Cannot delete category with children");
        }
        
        // Check if category has products
        Long productCount = categoryRepository.getProductCount(category.getId());
        if (productCount > 0L) {
            throw new ValidationException("Cannot delete category with products");
        }
    }
    
    private void deactivateChildCategories(Long parentId) {
        log.debug("Deactivating child categories for parent: {}", parentId);
        categoryRepository.deactivateChildCategories(parentId, getCurrentUsername());
    }
    
    private void updateDescendantPaths(Long categoryId, String newBasePath, int newBaseLevel) {
        log.debug("Updating descendant paths for category: {}", categoryId);
        
        List<Category> descendants = categoryRepository.getCategoryDescendants(categoryId);
        for (Category descendant : descendants) {
            int levelDiff = descendant.getLevel() - newBaseLevel - 1;
            String newPath = newBasePath + descendant.getPath().substring(
                descendant.getPath().lastIndexOf('/'));
            
            categoryRepository.updateCategoryPath(descendant.getId(), newPath, 
                newBaseLevel + levelDiff + 1, getCurrentUsername());
        }
    }
    
    private String generateSlug(String name, Long parentId) {
        String baseSlug = name.toLowerCase()
            .replaceAll("[^a-z0-9\\s-]", "")
            .replaceAll("\\s+", "-")
            .replaceAll("-+", "-")
            .replaceAll("^-|-$", "");
        
        String slug = baseSlug;
        int counter = 1;
        
        while (categoryRepository.existsBySlugAndParentId(slug, parentId)) {
            slug = baseSlug + "-" + counter;
            counter++;
        }
        
        return slug;
    }
    
    private void publishCategoryEvent(String eventType, Category category) {
        Map<String, Object> eventData = Map.of(
            "eventType", eventType,
            "categoryId", category.getId(),
            "categoryName", category.getName(),
            "parentId", category.getParentId(),
            "level", category.getLevel(),
            "timestamp", LocalDateTime.now(),
            "updatedBy", getCurrentUsername()
        );
        
        kafkaTemplate.send("category.events", eventData);
    }
    
    private String getCurrentUsername() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
}