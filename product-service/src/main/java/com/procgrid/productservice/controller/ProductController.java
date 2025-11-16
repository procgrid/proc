package com.procgrid.productservice.controller;

import com.procgrid.productservice.dto.request.ProductCreateRequest;
import com.procgrid.productservice.dto.request.ProductUpdateRequest;
import com.procgrid.productservice.dto.response.ProductDetailResponse;
import com.procgrid.productservice.dto.response.ProductSummaryResponse;
import com.procgrid.productservice.model.Product;
import com.procgrid.productservice.service.ProductService;
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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * REST Controller for Product operations
 * Provides API endpoints for product management, search, and analytics
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@Tag(name = "Product Management", description = "APIs for managing agricultural products")
@SecurityRequirement(name = "bearerAuth")
public class ProductController {
    
    private final ProductService productService;
    
    /**
     * Create new product
     */
    @PostMapping
    @PreAuthorize("hasRole('PRODUCER')")
    @Operation(summary = "Create new product", description = "Create a new agricultural product listing")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Product created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request data"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Producer role required")
    })
    public ResponseEntity<ProductDetailResponse> createProduct(
            @Valid @RequestBody ProductCreateRequest request) {
        log.debug("Creating new product: {}", request.getName());
        
        ProductDetailResponse response = productService.createProduct(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
    
    /**
     * Update existing product
     */
    @PutMapping("/{productId}")
    @PreAuthorize("hasRole('PRODUCER')")
    @Operation(summary = "Update product", description = "Update an existing product")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Product updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request data"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Producer role required"),
        @ApiResponse(responseCode = "404", description = "Product not found")
    })
    public ResponseEntity<ProductDetailResponse> updateProduct(
            @Parameter(description = "Product ID") @PathVariable Long productId,
            @Valid @RequestBody ProductUpdateRequest request) {
        log.debug("Updating product: {}", productId);
        
        ProductDetailResponse response = productService.updateProduct(productId, request);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get product by ID
     */
    @GetMapping("/{productId}")
    @PreAuthorize("hasRole('BUYER') or hasRole('PRODUCER') or hasRole('ADMIN')")
    @Operation(summary = "Get product by ID", description = "Retrieve product details by ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Product found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "404", description = "Product not found")
    })
    public ResponseEntity<ProductDetailResponse> getProduct(
            @Parameter(description = "Product ID") @PathVariable Long productId,
            @Parameter(description = "Viewer type for analytics") @RequestParam(required = false) String viewerType,
            @Parameter(description = "Viewer location") @RequestParam(required = false) String location) {
        log.debug("Getting product: {}", productId);
        
        ProductDetailResponse response = productService.getProduct(productId);
        
        // Track view if parameters provided
        if (viewerType != null) {
            productService.trackProductView(productId, viewerType, location);
        }
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get product by SKU
     */
    @GetMapping("/sku/{sku}")
    @PreAuthorize("hasRole('BUYER') or hasRole('PRODUCER') or hasRole('ADMIN')")
    @Operation(summary = "Get product by SKU", description = "Retrieve product details by SKU")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Product found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "404", description = "Product not found")
    })
    public ResponseEntity<ProductDetailResponse> getProductBySku(
            @Parameter(description = "Product SKU") @PathVariable String sku) {
        log.debug("Getting product by SKU: {}", sku);
        
        ProductDetailResponse response = productService.getProductBySku(sku);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Search products
     */
    @GetMapping("/search")
    @PreAuthorize("hasRole('BUYER') or hasRole('PRODUCER') or hasRole('ADMIN')")
    @Operation(summary = "Search products", description = "Search products with filters")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Search results returned"),
        @ApiResponse(responseCode = "400", description = "Invalid search parameters"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<Page<ProductSummaryResponse>> searchProducts(
            @Parameter(description = "Search query") @RequestParam(required = false) String query,
            @Parameter(description = "Category ID") @RequestParam(required = false) Long categoryId,
            @Parameter(description = "Minimum price") @RequestParam(required = false) BigDecimal minPrice,
            @Parameter(description = "Maximum price") @RequestParam(required = false) BigDecimal maxPrice,
            @Parameter(description = "Location filter") @RequestParam(required = false) String location,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "DESC") String sortDir) {
        
        log.debug("Searching products with query: {}", query);
        
        Sort.Direction direction = sortDir.equalsIgnoreCase("ASC") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        Page<ProductSummaryResponse> response = productService.searchProducts(
            query, categoryId, minPrice, maxPrice, location, pageable);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get products by category
     */
    @GetMapping("/category/{categoryId}")
    @PreAuthorize("hasRole('BUYER') or hasRole('PRODUCER') or hasRole('ADMIN')")
    @Operation(summary = "Get products by category", description = "Retrieve products in a specific category")
    public ResponseEntity<Page<ProductSummaryResponse>> getProductsByCategory(
            @Parameter(description = "Category ID") @PathVariable Long categoryId,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "DESC") String sortDir) {
        
        Sort.Direction direction = sortDir.equalsIgnoreCase("ASC") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        Page<ProductSummaryResponse> response = productService.getProductsByCategory(categoryId, pageable);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get featured products
     */
    @GetMapping("/featured")
    @Operation(summary = "Get featured products", description = "Retrieve featured products")
    public ResponseEntity<List<ProductSummaryResponse>> getFeaturedProducts(
            @Parameter(description = "Limit") @RequestParam(defaultValue = "10") int limit) {
        
        List<ProductSummaryResponse> response = productService.getFeaturedProducts(limit);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get recent products
     */
    @GetMapping("/recent")
    @Operation(summary = "Get recent products", description = "Retrieve recently added products")
    public ResponseEntity<List<ProductSummaryResponse>> getRecentProducts(
            @Parameter(description = "Limit") @RequestParam(defaultValue = "10") int limit) {
        
        List<ProductSummaryResponse> response = productService.getRecentProducts(limit);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get products by certification
     */
    @GetMapping("/certified")
    @PreAuthorize("hasRole('BUYER') or hasRole('PRODUCER') or hasRole('ADMIN')")
    @Operation(summary = "Get certified products", description = "Retrieve products with specific certifications")
    public ResponseEntity<Page<ProductSummaryResponse>> getProductsByCertification(
            @Parameter(description = "Certification types") @RequestParam List<String> certifications,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<ProductSummaryResponse> response = productService.getProductsByCertification(certifications, pageable);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get my products (current producer)
     */
    @GetMapping("/my")
    @PreAuthorize("hasRole('PRODUCER')")
    @Operation(summary = "Get my products", description = "Retrieve products belonging to current producer")
    public ResponseEntity<Page<ProductSummaryResponse>> getMyProducts(
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "DESC") String sortDir) {
        
        Sort.Direction direction = sortDir.equalsIgnoreCase("ASC") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        Page<ProductSummaryResponse> response = productService.getMyProducts(pageable);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get products by producer
     */
    @GetMapping("/producer/{producerId}")
    @PreAuthorize("hasRole('BUYER') or hasRole('PRODUCER') or hasRole('ADMIN')")
    @Operation(summary = "Get products by producer", description = "Retrieve products from a specific producer")
    public ResponseEntity<Page<ProductSummaryResponse>> getProductsByProducer(
            @Parameter(description = "Producer ID") @PathVariable Long producerId,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<ProductSummaryResponse> response = productService.getProductsByProducer(producerId, pageable);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Update product status
     */
    @PatchMapping("/{productId}/status")
    @PreAuthorize("hasRole('PRODUCER')")
    @Operation(summary = "Update product status", description = "Update the status of a product")
    public ResponseEntity<Void> updateProductStatus(
            @Parameter(description = "Product ID") @PathVariable Long productId,
            @Parameter(description = "New status") @RequestParam Product.ProductStatus status) {
        
        log.debug("Updating product {} status to {}", productId, status);
        productService.updateProductStatus(productId, status);
        return ResponseEntity.ok().build();
    }
    
    /**
     * Update product pricing
     */
    @PatchMapping("/{productId}/pricing")
    @PreAuthorize("hasRole('PRODUCER')")
    @Operation(summary = "Update product pricing", description = "Update product price and order quantities")
    public ResponseEntity<Void> updateProductPricing(
            @Parameter(description = "Product ID") @PathVariable Long productId,
            @Parameter(description = "New price") @RequestParam BigDecimal price,
            @Parameter(description = "Minimum order quantity") @RequestParam(required = false) BigDecimal minOrderQty,
            @Parameter(description = "Maximum order quantity") @RequestParam(required = false) BigDecimal maxOrderQty) {
        
        log.debug("Updating product {} pricing", productId);
        productService.updateProductPricing(productId, price, minOrderQty, maxOrderQty);
        return ResponseEntity.ok().build();
    }
    
    /**
     * Update product location
     */
    @PatchMapping("/{productId}/location")
    @PreAuthorize("hasRole('PRODUCER')")
    @Operation(summary = "Update product location", description = "Update product location information")
    public ResponseEntity<Void> updateProductLocation(
            @Parameter(description = "Product ID") @PathVariable Long productId,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String state,
            @RequestParam(required = false) String country,
            @RequestParam(required = false) String zipCode,
            @RequestParam(required = false) BigDecimal latitude,
            @RequestParam(required = false) BigDecimal longitude) {
        
        log.debug("Updating product {} location", productId);
        productService.updateProductLocation(productId, city, state, country, zipCode, latitude, longitude);
        return ResponseEntity.ok().build();
    }
    
    /**
     * Delete product
     */
    @DeleteMapping("/{productId}")
    @PreAuthorize("hasRole('PRODUCER')")
    @Operation(summary = "Delete product", description = "Soft delete a product")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Product deleted successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "404", description = "Product not found")
    })
    public ResponseEntity<Void> deleteProduct(
            @Parameter(description = "Product ID") @PathVariable Long productId) {
        
        log.debug("Deleting product: {}", productId);
        productService.deleteProduct(productId);
        return ResponseEntity.noContent().build();
    }
    
    /**
     * Bulk update product status
     */
    @PatchMapping("/bulk/status")
    @PreAuthorize("hasRole('PRODUCER')")
    @Operation(summary = "Bulk update product status", description = "Update status for multiple products")
    public ResponseEntity<Void> bulkUpdateProductStatus(
            @Parameter(description = "Product IDs") @RequestParam List<Long> productIds,
            @Parameter(description = "New status") @RequestParam Product.ProductStatus status) {
        
        log.debug("Bulk updating {} products to status {}", productIds.size(), status);
        productService.bulkUpdateProductStatus(productIds, status);
        return ResponseEntity.ok().build();
    }
    
    /**
     * Get product statistics
     */
    @GetMapping("/stats/producer/{producerId}")
    @PreAuthorize("hasRole('PRODUCER') or hasRole('ADMIN')")
    @Operation(summary = "Get product statistics", description = "Retrieve product statistics for a producer")
    public ResponseEntity<Map<String, Object>> getProductStats(
            @Parameter(description = "Producer ID") @PathVariable Long producerId) {
        
        Map<String, Object> stats = productService.getProducerProductStats(producerId);
        return ResponseEntity.ok(stats);
    }
    
    /**
     * Get product performance analytics
     */
    @GetMapping("/analytics/performance")
    @PreAuthorize("hasRole('PRODUCER') or hasRole('ADMIN')")
    @Operation(summary = "Get product performance", description = "Retrieve product performance analytics")
    public ResponseEntity<List<Map<String, Object>>> getProductPerformance(
            @Parameter(description = "Producer ID") @RequestParam Long producerId,
            @Parameter(description = "Start date (YYYY-MM-DD)") @RequestParam String startDate,
            @Parameter(description = "End date (YYYY-MM-DD)") @RequestParam String endDate) {
        
        List<Map<String, Object>> performance = productService.getProductPerformanceByDateRange(
            producerId, startDate, endDate);
        return ResponseEntity.ok(performance);
    }
    
    /**
     * Get product view history
     */
    @GetMapping("/{productId}/views")
    @PreAuthorize("hasRole('PRODUCER') or hasRole('ADMIN')")
    @Operation(summary = "Get product view history", description = "Retrieve product view analytics")
    public ResponseEntity<List<Map<String, Object>>> getProductViewHistory(
            @Parameter(description = "Product ID") @PathVariable Long productId,
            @Parameter(description = "Number of days") @RequestParam(defaultValue = "30") int days) {
        
        List<Map<String, Object>> views = productService.getProductViewHistory(productId, days);
        return ResponseEntity.ok(views);
    }
}