package com.procgrid.productservice.repository;

import com.procgrid.productservice.mapper.mybatis.ProductMyBatisMapper;
import com.procgrid.productservice.model.Product;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Repository layer for Product operations
 * Provides caching, transaction management, and business logic
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class ProductRepository {
    
    private final ProductMyBatisMapper productMapper;
    
    /**
     * Create new product
     */
    @Transactional
    @CacheEvict(value = {"products", "productSearch", "categoryProducts"}, allEntries = true)
    public Product save(Product product) {
        log.debug("Creating new product: {}", product.getName());
        
        // Generate slug if not provided
        if (product.getSlug() == null || product.getSlug().isEmpty()) {
            product.setSlug(generateSlug(product.getName()));
        }
        
        // Validate slug uniqueness
        if (existsBySlugAndNotId(product.getSlug(), product.getId())) {
            product.setSlug(generateUniqueSlug(product.getSlug()));
        }
        
        productMapper.insertProduct(product);
        log.info("Created product with ID: {} for producer: {}", product.getId(), product.getProducerId());
        
        return product;
    }
    
    /**
     * Update existing product
     */
    @Transactional
    @CachePut(value = "products", key = "#product.id")
    @CacheEvict(value = {"productSearch", "categoryProducts"}, allEntries = true)
    public Product update(Product product) {
        log.debug("Updating product: {}", product.getId());
        
        // Validate slug uniqueness if changed
        if (product.getSlug() != null && existsBySlugAndNotId(product.getSlug(), product.getId())) {
            product.setSlug(generateUniqueSlug(product.getSlug()));
        }
        
        productMapper.updateProduct(product);
        log.info("Updated product: {} for producer: {}", product.getId(), product.getProducerId());
        
        return product;
    }
    
    /**
     * Find product by ID with caching
     */
    @Cacheable(value = "products", key = "#id")
    public Optional<Product> findById(Long id) {
        log.debug("Finding product by ID: {}", id);
        Product product = productMapper.findById(id);
        return Optional.ofNullable(product);
    }
    
    /**
     * Find product by slug with caching
     */
    @Cacheable(value = "products", key = "'slug:' + #slug")
    public Optional<Product> findBySlug(String slug) {
        log.debug("Finding product by slug: {}", slug);
        Product product = productMapper.findBySlug(slug);
        return Optional.ofNullable(product);
    }
    
    /**
     * Find products by producer ID with pagination
     */
    @Cacheable(value = "producerProducts", key = "#producerId + ':' + #page + ':' + #size")
    public List<Product> findByProducerId(Long producerId, int page, int size) {
        log.debug("Finding products for producer: {}, page: {}, size: {}", producerId, page, size);
        int offset = page * size;
        return productMapper.findByProducerId(producerId, offset, size);
    }
    
    /**
     * Count products by producer ID
     */
    @Cacheable(value = "producerProductCount", key = "#producerId")
    public Long countByProducerId(Long producerId) {
        return productMapper.countByProducerId(producerId);
    }
    
    /**
     * Find products by category ID with pagination
     */
    @Cacheable(value = "categoryProducts", key = "#categoryId + ':' + #page + ':' + #size")
    public List<Product> findByCategoryId(Long categoryId, int page, int size) {
        log.debug("Finding products for category: {}, page: {}, size: {}", categoryId, page, size);
        int offset = page * size;
        return productMapper.findByCategoryId(categoryId, offset, size);
    }
    
    /**
     * Search products with filters
     */
    public List<Product> searchProducts(String query, Long categoryId, BigDecimal minPrice, 
                                       BigDecimal maxPrice, String location, int page, int size) {
        log.debug("Searching products with query: {}, category: {}, location: {}", 
            query, categoryId, location);
        int offset = page * size;
        return productMapper.searchProducts(query, categoryId, minPrice, maxPrice, location, offset, size);
    }
    
    /**
     * Count search results
     */
    public Long countSearchResults(String query, Long categoryId, BigDecimal minPrice, 
                                  BigDecimal maxPrice, String location) {
        return productMapper.countSearchResults(query, categoryId, minPrice, maxPrice, location);
    }
    
    /**
     * Count products by category ID
     */
    @Cacheable(value = "categoryProductCount", key = "#categoryId")
    public Long countByCategoryId(Long categoryId) {
        return productMapper.countByCategoryId(categoryId);
    }
    
    /**
     * Find products by status with pagination
     */
    public List<Product> findByStatus(Product.ProductStatus status, int page, int size) {
        log.debug("Finding products by status: {}, page: {}, size: {}", status, page, size);
        int offset = page * size;
        return productMapper.findByStatus(status, offset, size);
    }
    
    /**
     * Count products by status
     */
    public Long countByStatus(Product.ProductStatus status) {
        return productMapper.countByStatus(status);
    }
    
    /**
     * Find featured products
     */
    public List<Product> findFeaturedProducts(int limit) {
        log.debug("Finding featured products, limit: {}", limit);
        return productMapper.findFeaturedProducts(limit);
    }
    
    /**
     * Find recent products
     */
    public List<Product> findRecentProducts(int limit) {
        log.debug("Finding recent products, limit: {}", limit);
        return productMapper.findRecentProducts(limit);
    }
    
    /**
     * Find products by certification
     */
    public List<Product> findByCertification(List<String> certifications, int page, int size) {
        log.debug("Finding products by certifications: {}, page: {}, size: {}", 
            certifications, page, size);
        int offset = page * size;
        return productMapper.findByCertification(certifications, offset, size);
    }
    
    /**
     * Count products by certification
     */
    public Long countByCertification(List<String> certifications) {
        return productMapper.countByCertification(certifications);
    }
    
    /**
     * Update product status
     */
    public boolean updateStatus(Long id, Product.ProductStatus status, String updatedBy) {
        log.debug("Updating product status: {} to {}", id, status);
        int rows = productMapper.updateProductStatus(id, status, updatedBy);
        
        if (rows > 0) {
            log.info("Updated product {} status to {}", id, status);
            return true;
        }
        
        return false;
    }
    
    /**
     * Update product pricing
     */
    public boolean updatePricing(Long id, BigDecimal price, BigDecimal minOrderQty, 
                                BigDecimal maxOrderQty, String updatedBy) {
        log.debug("Updating product pricing: {}", id);
        int rows = productMapper.updatePricing(id, price, minOrderQty, maxOrderQty, updatedBy);
        
        if (rows > 0) {
            log.info("Updated product {} pricing", id);
            return true;
        }
        
        return false;
    }
    
    /**
     * Update product location
     */
    public boolean updateLocation(Long id, String city, String state, String country, 
                                 String zipCode, BigDecimal latitude, BigDecimal longitude, 
                                 String updatedBy) {
        log.debug("Updating product location: {}", id);
        int rows = productMapper.updateLocation(id, city, state, country, zipCode, 
            latitude, longitude, updatedBy);
        
        if (rows > 0) {
            log.info("Updated product {} location", id);
            return true;
        }
        
        return false;
    }
    
    /**
     * Get product performance by date range
     */
    public List<Map<String, Object>> getProductPerformanceByDateRange(Long producerId, 
                                                                      String startDate, String endDate) {
        log.debug("Getting product performance for producer: {} from {} to {}", 
            producerId, startDate, endDate);
        return productMapper.getProductPerformanceByDateRange(producerId, startDate, endDate);
    }
    
    /**
     * Get product view history
     */
    public List<Map<String, Object>> getProductViewHistory(Long productId, int days) {
        log.debug("Getting product view history for: {} over {} days", productId, days);
        return productMapper.getProductViewHistory(productId, days);
    }
    
    /**
     * Find products expiring soon
     */
    public List<Product> findProductsExpiringSoon(int days) {
        log.debug("Finding products expiring within {} days", days);
        return productMapper.findProductsExpiringSoon(days);
    }
    
    /**
     * Find low stock products for producer
     */
    public List<Product> findLowStockProducts(Long producerId) {
        log.debug("Finding low stock products for producer: {}", producerId);
        return productMapper.findLowStockProducts(producerId);
    }
    
    /**
     * Update product status
     */
    @Transactional
    @CacheEvict(value = {"products", "producerProducts", "categoryProducts"}, allEntries = true)
    public boolean updateProductStatus(Long id, Product.ProductStatus status, String updatedBy) {
        log.debug("Updating product status: {} to {}", id, status);
        int rows = productMapper.updateProductStatus(id, status, updatedBy);
        
        if (rows > 0) {
            log.info("Updated product {} status to {}", id, status);
            return true;
        }
        
        return false;
    }
    
    /**
     * Update product visibility
     */
    @Transactional
    @CacheEvict(value = {"products", "productSearch", "categoryProducts"}, allEntries = true)
    public boolean updateProductVisibility(Long id, Product.ProductVisibility visibility, String updatedBy) {
        log.debug("Updating product visibility: {} to {}", id, visibility);
        int rows = productMapper.updateProductVisibility(id, visibility, updatedBy);
        
        if (rows > 0) {
            log.info("Updated product {} visibility to {}", id, visibility);
            return true;
        }
        
        return false;
    }
    
    /**
     * Increment view count (no caching to avoid contention)
     */
    @Transactional
    public void incrementViewCount(Long id) {
        productMapper.incrementViewCount(id);
    }
    
    /**
     * Update product rating
     */
    @Transactional
    @CacheEvict(value = "products", key = "#id")
    public void updateRating(Long id, BigDecimal rating, Integer count) {
        log.debug("Updating product rating: {} to {} ({} reviews)", id, rating, count);
        productMapper.updateRating(id, rating, count);
    }
    
    /**
     * Update available quantity
     */
    @Transactional
    @CacheEvict(value = {"products", "producerProducts"}, allEntries = true)
    public boolean updateAvailableQuantity(Long id, BigDecimal quantity, String updatedBy) {
        log.debug("Updating product available quantity: {} to {}", id, quantity);
        int rows = productMapper.updateAvailableQuantity(id, quantity, updatedBy);
        
        if (rows > 0) {
            log.info("Updated product {} available quantity to {}", id, quantity);
            return true;
        }
        
        return false;
    }
    
    /**
     * Delete product (soft delete)
     */
    @Transactional
    @CacheEvict(value = {"products", "producerProducts", "categoryProducts", "productSearch"}, allEntries = true)
    public boolean deleteProduct(Long id, Long producerId, String updatedBy) {
        log.debug("Deleting product: {} for producer: {}", id, producerId);
        int rows = productMapper.deleteProduct(id, producerId, updatedBy);
        
        if (rows > 0) {
            log.info("Deleted product: {} for producer: {}", id, producerId);
            return true;
        }
        
        log.warn("Failed to delete product: {} for producer: {}", id, producerId);
        return false;
    }
    
    /**
     * Check if product exists and belongs to producer
     */
    public boolean existsByIdAndProducerId(Long productId, Long producerId) {
        return productMapper.existsByIdAndProducerId(productId, producerId);
    }
    
    /**
     * Check if slug exists for different product
     */
    public boolean existsBySlugAndNotId(String slug, Long productId) {
        Long id = productId != null ? productId : 0L;
        return productMapper.existsBySlugAndNotId(slug, id);
    }
    
    /**
     * Find products by category hierarchy
     */
    @Cacheable(value = "categoryHierarchyProducts", key = "#categoryIds.hashCode() + ':' + #page + ':' + #size")
    public List<Product> findByCategoryHierarchy(List<Long> categoryIds, int page, int size) {
        log.debug("Finding products for category hierarchy: {}", categoryIds);
        int offset = page * size;
        return productMapper.findByCategoryHierarchy(categoryIds, offset, size);
    }
    
    /**
     * Get product statistics for producer
     */
    @Cacheable(value = "producerProductStats", key = "#producerId")
    public Map<String, Object> getProducerProductStats(Long producerId) {
        log.debug("Getting product statistics for producer: {}", producerId);
        return productMapper.getProducerProductStats(producerId);
    }
    
    /**
     * Get product search suggestions
     */
    @Cacheable(value = "productSuggestions", key = "#query + ':' + #limit")
    public List<String> getSearchSuggestions(String query, int limit) {
        log.debug("Getting search suggestions for query: {}", query);
        return productMapper.getSearchSuggestions(query, limit);
    }
    
    /**
     * Bulk update product status
     */
    @Transactional
    @CacheEvict(value = {"products", "producerProducts", "categoryProducts", "productSearch"}, allEntries = true)
    public int bulkUpdateStatus(List<Long> productIds, Product.ProductStatus status, String updatedBy) {
        log.debug("Bulk updating product status for {} products to {}", productIds.size(), status);
        int rows = productMapper.bulkUpdateStatus(productIds, status, updatedBy);
        log.info("Bulk updated {} products to status {}", rows, status);
        return rows;
    }
    
    /**
     * Find similar products
     */
    @Cacheable(value = "similarProducts", key = "#productId + ':' + #categoryId + ':' + #tags.hashCode()")
    public List<Product> findSimilarProducts(Long productId, Long categoryId, List<String> tags, int limit) {
        log.debug("Finding similar products for: {}", productId);
        return productMapper.findSimilarProducts(productId, categoryId, tags, limit);
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
     * Find product by SKU
     */
    @Cacheable(value = "products", key = "'sku:' + #sku")
    public Optional<Product> findBySku(String sku) {
        log.debug("Finding product by SKU: {}", sku);
        return Optional.ofNullable(productMapper.findBySku(sku));
    }
}