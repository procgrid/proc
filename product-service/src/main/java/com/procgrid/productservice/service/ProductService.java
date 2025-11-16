package com.procgrid.productservice.service;

import com.procgrid.productservice.dto.request.ProductCreateRequest;
import com.procgrid.productservice.dto.request.ProductUpdateRequest;
import com.procgrid.productservice.dto.response.ProductDetailResponse;
import com.procgrid.productservice.dto.response.ProductSummaryResponse;
import com.procgrid.productservice.exception.ProductNotFoundException;
import com.procgrid.productservice.exception.UnauthorizedAccessException;
import com.procgrid.productservice.exception.ValidationException;
import com.procgrid.productservice.mapper.ProductMapper;
import com.procgrid.productservice.model.Product;
import com.procgrid.productservice.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service layer for Product operations
 * Provides business logic, validation, and integration with external systems
 */
@Slf4j
@Service
public class ProductService {
    
    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    
    public ProductService(
            ProductRepository productRepository, 
            @Qualifier("productMapperImpl") ProductMapper productMapper, 
            KafkaTemplate<String, Object> kafkaTemplate) {
        this.productRepository = productRepository;
        this.productMapper = productMapper;
        this.kafkaTemplate = kafkaTemplate;
    }
    
    /**
     * Create new product
     */
    @Transactional
    public ProductDetailResponse createProduct(ProductCreateRequest request) {
        log.debug("Creating new product: {}", request.getName());
        
        // Validate request
        validateProductRequest(request);
        
        // Check if producer owns the product
        Long producerId = getCurrentProducerId();
        if (request.getProducerId() != null && !request.getProducerId().equals(producerId)) {
            throw new UnauthorizedAccessException("Cannot create product for another producer");
        }
        
        // Map request to entity
        Product product = productMapper.toEntity(request);
        product.setProducerId(producerId);
        product.setStatus(Product.ProductStatus.DRAFT);
        product.setCreatedBy(getCurrentUsername());
        product.setCreatedAt(LocalDateTime.now());
        
        // Generate SKU if not provided
        if (product.getSku() == null || product.getSku().isEmpty()) {
            product.setSku(generateSku(product));
        }
        
        // Save product
        Product savedProduct = productRepository.save(product);
        
        // Publish product created event
        publishProductEvent("product.created", savedProduct);
        
        log.info("Created product: {} with ID: {}", savedProduct.getName(), savedProduct.getId());
        return productMapper.toDetailResponse(savedProduct);
    }
    
    /**
     * Update existing product
     */
    @Transactional
    public ProductDetailResponse updateProduct(Long id, ProductUpdateRequest request) {
        log.debug("Updating product: {}", id);
        
        // Find existing product
        Product existingProduct = productRepository.findById(id)
            .orElseThrow(() -> new ProductNotFoundException("Product not found: " + id));
        
        // Check ownership
        validateProductOwnership(existingProduct);
        
        // Validate request
        validateProductUpdateRequest(request, existingProduct);
        
        // Update product
        productMapper.updateEntityFromRequest(existingProduct, request);
        existingProduct.setUpdatedBy(getCurrentUsername());
        existingProduct.setUpdatedAt(LocalDateTime.now());
        
        Product updatedProduct = productRepository.update(existingProduct);
        
        // Publish product updated event
        publishProductEvent("product.updated", updatedProduct);
        
        log.info("Updated product: {} with ID: {}", updatedProduct.getName(), updatedProduct.getId());
        return productMapper.toDetailResponse(updatedProduct);
    }
    
    /**
     * Get product by ID
     */
    @Cacheable(value = "productDetails", key = "#id")
    public ProductDetailResponse getProduct(Long id) {
        log.debug("Getting product by ID: {}", id);
        
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new ProductNotFoundException("Product not found: " + id));
        
        return productMapper.toDetailResponse(product);
    }
    
    /**
     * Get product by SKU
     */
    @Cacheable(value = "productDetails", key = "'sku:' + #sku")
    public ProductDetailResponse getProductBySku(String sku) {
        log.debug("Getting product by SKU: {}", sku);
        
        Product product = productRepository.findBySku(sku)
            .orElseThrow(() -> new ProductNotFoundException("Product not found with SKU: " + sku));
        
        return productMapper.toDetailResponse(product);
    }
    
    /**
     * Get products by producer with pagination
     */
    public Page<ProductSummaryResponse> getProductsByProducer(Long producerId, Pageable pageable) {
        log.debug("Getting products for producer: {}, page: {}, size: {}", 
            producerId, pageable.getPageNumber(), pageable.getPageSize());
        
        List<Product> products = productRepository.findByProducerId(
            producerId, pageable.getPageNumber(), pageable.getPageSize());
        
        Long totalCount = productRepository.countByProducerId(producerId);
        
        List<ProductSummaryResponse> responses = products.stream()
            .map(productMapper::toSummaryResponse)
            .toList();
        
        return new PageImpl<>(responses, pageable, totalCount);
    }
    
    /**
     * Get current producer's products
     */
    public Page<ProductSummaryResponse> getMyProducts(Pageable pageable) {
        Long producerId = getCurrentProducerId();
        return getProductsByProducer(producerId, pageable);
    }
    
    /**
     * Search products with filters
     */
    @Cacheable(value = "productSearch", key = "#query + ':' + #categoryId + ':' + #minPrice + ':' + #maxPrice + ':' + #location + ':' + #pageable.pageNumber + ':' + #pageable.pageSize")
    public Page<ProductSummaryResponse> searchProducts(String query, Long categoryId, 
                                                      BigDecimal minPrice, BigDecimal maxPrice,
                                                      String location, Pageable pageable) {
        log.debug("Searching products with query: {}, category: {}, location: {}", 
            query, categoryId, location);
        
        List<Product> products = productRepository.searchProducts(
            query, categoryId, minPrice, maxPrice, location, 
            pageable.getPageNumber(), pageable.getPageSize());
        
        Long totalCount = productRepository.countSearchResults(
            query, categoryId, minPrice, maxPrice, location);
        
        List<ProductSummaryResponse> responses = products.stream()
            .map(productMapper::toSummaryResponse)
            .toList();
        
        return new PageImpl<>(responses, pageable, totalCount);
    }
    
    /**
     * Get products by category with pagination
     */
    @Cacheable(value = "productsByCategory", key = "#categoryId + ':' + #pageable.pageNumber + ':' + #pageable.pageSize")
    public Page<ProductSummaryResponse> getProductsByCategory(Long categoryId, Pageable pageable) {
        log.debug("Getting products for category: {}, page: {}, size: {}", 
            categoryId, pageable.getPageNumber(), pageable.getPageSize());
        
        List<Product> products = productRepository.findByCategoryId(
            categoryId, pageable.getPageNumber(), pageable.getPageSize());
        
        Long totalCount = productRepository.countByCategoryId(categoryId);
        
        List<ProductSummaryResponse> responses = products.stream()
            .map(productMapper::toSummaryResponse)
            .toList();
        
        return new PageImpl<>(responses, pageable, totalCount);
    }
    
    /**
     * Get products by status
     */
    public Page<ProductSummaryResponse> getProductsByStatus(Product.ProductStatus status, Pageable pageable) {
        log.debug("Getting products by status: {}, page: {}, size: {}", 
            status, pageable.getPageNumber(), pageable.getPageSize());
        
        List<Product> products = productRepository.findByStatus(
            status, pageable.getPageNumber(), pageable.getPageSize());
        
        Long totalCount = productRepository.countByStatus(status);
        
        List<ProductSummaryResponse> responses = products.stream()
            .map(productMapper::toSummaryResponse)
            .toList();
        
        return new PageImpl<>(responses, pageable, totalCount);
    }
    
    /**
     * Get featured products
     */
    @Cacheable(value = "featuredProducts", key = "#limit")
    public List<ProductSummaryResponse> getFeaturedProducts(int limit) {
        log.debug("Getting featured products, limit: {}", limit);
        
        List<Product> products = productRepository.findFeaturedProducts(limit);
        
        return products.stream()
            .map(productMapper::toSummaryResponse)
            .toList();
    }
    
    /**
     * Get recently added products
     */
    @Cacheable(value = "recentProducts", key = "#limit")
    public List<ProductSummaryResponse> getRecentProducts(int limit) {
        log.debug("Getting recent products, limit: {}", limit);
        
        List<Product> products = productRepository.findRecentProducts(limit);
        
        return products.stream()
            .map(productMapper::toSummaryResponse)
            .toList();
    }
    
    /**
     * Get products by certification
     */
    public Page<ProductSummaryResponse> getProductsByCertification(List<String> certifications, 
                                                                  Pageable pageable) {
        log.debug("Getting products by certifications: {}, page: {}, size: {}", 
            certifications, pageable.getPageNumber(), pageable.getPageSize());
        
        List<Product> products = productRepository.findByCertification(
            certifications, pageable.getPageNumber(), pageable.getPageSize());
        
        Long totalCount = productRepository.countByCertification(certifications);
        
        List<ProductSummaryResponse> responses = products.stream()
            .map(productMapper::toSummaryResponse)
            .toList();
        
        return new PageImpl<>(responses, pageable, totalCount);
    }
    
    /**
     * Update product status
     */
    @Transactional
    @CacheEvict(value = {"productDetails", "productSearch", "productsByCategory"}, allEntries = true)
    public void updateProductStatus(Long id, Product.ProductStatus status) {
        log.debug("Updating product status: {} to {}", id, status);
        
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new ProductNotFoundException("Product not found: " + id));
        
        validateProductOwnership(product);
        validateStatusTransition(product.getStatus(), status);
        
        boolean updated = productRepository.updateStatus(id, status, getCurrentUsername());
        
        if (updated) {
            // Publish status changed event
            Map<String, Object> eventData = Map.of(
                "productId", id,
                "oldStatus", product.getStatus(),
                "newStatus", status,
                "updatedBy", getCurrentUsername()
            );
            kafkaTemplate.send("product.status.changed", eventData);
            
            log.info("Updated product {} status to {}", id, status);
        }
    }
    
    /**
     * Update product pricing
     */
    @Transactional
    @CacheEvict(value = {"productDetails", "productSearch"}, allEntries = true)
    public void updateProductPricing(Long id, BigDecimal price, BigDecimal minOrderQty, 
                                   BigDecimal maxOrderQty) {
        log.debug("Updating product pricing: {}", id);
        
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new ProductNotFoundException("Product not found: " + id));
        
        validateProductOwnership(product);
        validatePricing(price, minOrderQty, maxOrderQty);
        
        boolean updated = productRepository.updatePricing(id, price, minOrderQty, maxOrderQty, 
            getCurrentUsername());
        
        if (updated) {
            // Publish pricing changed event
            Map<String, Object> eventData = Map.of(
                "productId", id,
                "price", price,
                "minOrderQty", minOrderQty,
                "maxOrderQty", maxOrderQty,
                "updatedBy", getCurrentUsername()
            );
            kafkaTemplate.send("product.pricing.changed", eventData);
            
            log.info("Updated product {} pricing", id);
        }
    }
    
    /**
     * Update product location
     */
    @Transactional
    @CacheEvict(value = {"productDetails", "productSearch"}, allEntries = true)
    public void updateProductLocation(Long id, String city, String state, String country, 
                                    String zipCode, BigDecimal latitude, BigDecimal longitude) {
        log.debug("Updating product location: {}", id);
        
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new ProductNotFoundException("Product not found: " + id));
        
        validateProductOwnership(product);
        
        boolean updated = productRepository.updateLocation(id, city, state, country, 
            zipCode, latitude, longitude, getCurrentUsername());
        
        if (updated) {
            log.info("Updated product {} location", id);
        }
    }
    
    /**
     * Delete product (soft delete)
     */
    @Transactional
    @CacheEvict(value = {"productDetails", "productSearch", "productsByCategory"}, allEntries = true)
    public void deleteProduct(Long id) {
        log.debug("Deleting product: {}", id);
        
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new ProductNotFoundException("Product not found: " + id));
        
        validateProductOwnership(product);
        validateProductDeletion(product);
        
        boolean deleted = productRepository.deleteProduct(id, product.getProducerId(), 
            getCurrentUsername());
        
        if (deleted) {
            // Publish product deleted event
            publishProductEvent("product.deleted", product);
            
            log.info("Deleted product: {} with ID: {}", product.getName(), id);
        }
    }
    
    /**
     * Get product statistics for producer
     */
    @Cacheable(value = "productStats", key = "#producerId")
    public Map<String, Object> getProducerProductStats(Long producerId) {
        log.debug("Getting product statistics for producer: {}", producerId);
        return productRepository.getProducerProductStats(producerId);
    }
    
    /**
     * Get product performance by date range
     */
    public List<Map<String, Object>> getProductPerformanceByDateRange(Long producerId, 
                                                                      String startDate, String endDate) {
        log.debug("Getting product performance for producer: {} from {} to {}", 
            producerId, startDate, endDate);
        return productRepository.getProductPerformanceByDateRange(producerId, startDate, endDate);
    }
    
    /**
     * Bulk update product status
     */
    @Transactional
    @CacheEvict(value = {"productDetails", "productSearch", "productsByCategory"}, allEntries = true)
    public void bulkUpdateProductStatus(List<Long> productIds, Product.ProductStatus status) {
        log.debug("Bulk updating product status for {} items to {}", productIds.size(), status);
        
        // Validate ownership for all products
        for (Long productId : productIds) {
            Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found: " + productId));
            validateProductOwnership(product);
        }
        
        int updated = productRepository.bulkUpdateStatus(productIds, status, getCurrentUsername());
        
        if (updated > 0) {
            // Publish bulk status changed event
            Map<String, Object> eventData = Map.of(
                "productIds", productIds,
                "status", status,
                "updatedBy", getCurrentUsername()
            );
            kafkaTemplate.send("product.bulk.status.changed", eventData);
            
            log.info("Bulk updated {} product statuses to {}", updated, status);
        }
    }
    
    /**
     * Get product view history
     */
    @Cacheable(value = "productViews", key = "#productId + ':' + #days")
    public List<Map<String, Object>> getProductViewHistory(Long productId, int days) {
        log.debug("Getting product view history for: {} over {} days", productId, days);
        return productRepository.getProductViewHistory(productId, days);
    }
    
    /**
     * Track product view
     */
    public void trackProductView(Long productId, String viewerType, String location) {
        log.debug("Tracking product view: {} by {}", productId, viewerType);
        
        // Publish view event
        Map<String, Object> eventData = Map.of(
            "productId", productId,
            "viewerType", viewerType,
            "location", location,
            "timestamp", LocalDateTime.now(),
            "userId", getCurrentUserId()
        );
        kafkaTemplate.send("product.view.tracked", eventData);
    }
    
    // Private helper methods
    
    private void validateProductRequest(ProductCreateRequest request) {
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new ValidationException("Product name is required");
        }
        
        if (request.getCategoryId() == null) {
            throw new ValidationException("Category is required");
        }
        
        if (request.getPrice() == null || request.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Valid price is required");
        }
        
        if (request.getMinOrderQuantity() == null || 
            request.getMinOrderQuantity().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Valid minimum order quantity is required");
        }
        
        if (request.getMaxOrderQuantity() != null && 
            request.getMaxOrderQuantity().compareTo(request.getMinOrderQuantity()) < 0) {
            throw new ValidationException("Maximum order quantity cannot be less than minimum");
        }
    }
    
    private void validateProductUpdateRequest(ProductUpdateRequest request, Product existingProduct) {
        if (request.getName() != null && request.getName().trim().isEmpty()) {
            throw new ValidationException("Product name cannot be empty");
        }
        
        if (request.getPrice() != null && request.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Price must be greater than zero");
        }
        
        if (request.getMinOrderQuantity() != null && 
            request.getMinOrderQuantity().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Minimum order quantity must be greater than zero");
        }
        
        BigDecimal maxQty = request.getMaxOrderQuantity() != null 
            ? request.getMaxOrderQuantity() : existingProduct.getMaxOrderQuantity();
        BigDecimal minQty = request.getMinOrderQuantity() != null 
            ? request.getMinOrderQuantity() : existingProduct.getMinOrderQuantity();
            
        if (maxQty != null && maxQty.compareTo(minQty) < 0) {
            throw new ValidationException("Maximum order quantity cannot be less than minimum");
        }
    }
    
    private void validateProductOwnership(Product product) {
        Long currentProducerId = getCurrentProducerId();
        if (!product.getProducerId().equals(currentProducerId)) {
            throw new UnauthorizedAccessException("Access denied for product: " + product.getId());
        }
    }
    
    private void validateStatusTransition(Product.ProductStatus currentStatus, 
                                        Product.ProductStatus newStatus) {
        // Define allowed status transitions
        switch (currentStatus) {
            case DRAFT -> {
                if (newStatus != Product.ProductStatus.ACTIVE && 
                    newStatus != Product.ProductStatus.INACTIVE) {
                    throw new ValidationException("Cannot transition from DRAFT to " + newStatus);
                }
            }
            case ACTIVE -> {
                if (newStatus != Product.ProductStatus.INACTIVE && 
                    newStatus != Product.ProductStatus.OUT_OF_STOCK) {
                    throw new ValidationException("Cannot transition from ACTIVE to " + newStatus);
                }
            }
            case INACTIVE -> {
                if (newStatus != Product.ProductStatus.ACTIVE) {
                    throw new ValidationException("Cannot transition from INACTIVE to " + newStatus);
                }
            }
            case OUT_OF_STOCK -> {
                if (newStatus != Product.ProductStatus.ACTIVE) {
                    throw new ValidationException("Cannot transition from OUT_OF_STOCK to " + newStatus);
                }
            }
        }
    }
    
    private void validatePricing(BigDecimal price, BigDecimal minOrderQty, BigDecimal maxOrderQty) {
        if (price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Price must be greater than zero");
        }
        
        if (minOrderQty.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Minimum order quantity must be greater than zero");
        }
        
        if (maxOrderQty != null && maxOrderQty.compareTo(minOrderQty) < 0) {
            throw new ValidationException("Maximum order quantity cannot be less than minimum");
        }
    }
    
    private void validateProductDeletion(Product product) {
        if (product.getStatus() == Product.ProductStatus.ACTIVE) {
            throw new ValidationException("Cannot delete active product. Please deactivate first.");
        }
    }
    
    private String generateSku(Product product) {
        // Generate SKU based on product details
        String categoryCode = "CAT" + product.getCategoryId().toString().substring(0, 
            Math.min(3, product.getCategoryId().toString().length()));
        String producerCode = "PRD" + product.getProducerId().toString().substring(0, 
            Math.min(3, product.getProducerId().toString().length()));
        String timestamp = String.valueOf(System.currentTimeMillis()).substring(8);
        
        return categoryCode + "-" + producerCode + "-" + timestamp;
    }
    
    private void publishProductEvent(String eventType, Product product) {
        Map<String, Object> eventData = Map.of(
            "eventType", eventType,
            "productId", product.getId(),
            "producerId", product.getProducerId(),
            "productName", product.getName(),
            "status", product.getStatus(),
            "timestamp", LocalDateTime.now(),
            "updatedBy", getCurrentUsername()
        );
        
        kafkaTemplate.send("product.events", eventData);
    }
    
    private Long getCurrentProducerId() {
        // Extract producer ID from security context
        // This would be populated by your JWT token
        return 1L; // Placeholder - implement based on your security setup
    }
    
    private String getCurrentUsername() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
    
    private String getCurrentUserId() {
        // Extract user ID from security context
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
}