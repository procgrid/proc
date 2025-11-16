package com.procgrid.productservice.controller;

import com.procgrid.productservice.model.ProductLot;
import com.procgrid.productservice.service.ProductLotService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * REST Controller for ProductLot operations
 * Provides API endpoints for product lot/batch management and traceability
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/product-lots")
@RequiredArgsConstructor
@Tag(name = "Product Lot Management", description = "APIs for managing product lots/batches and traceability")
@SecurityRequirement(name = "bearerAuth")
public class ProductLotController {
    
    private final ProductLotService productLotService;
    
    /**
     * Create new product lot
     */
    @PostMapping
    @PreAuthorize("hasRole('PRODUCER')")
    @Operation(summary = "Create new product lot", description = "Create a new product lot/batch with traceability information")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Product lot created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request data"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Producer role required")
    })
    public ResponseEntity<ProductLot> createProductLot(
            @Parameter(description = "Product ID") @RequestParam Long productId,
            @Parameter(description = "Lot number") @RequestParam String lotNumber,
            @Parameter(description = "Total quantity") @RequestParam BigDecimal totalQuantity,
            @Parameter(description = "Production date") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate productionDate,
            @Parameter(description = "Expiry date") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate expiryDate,
            @Parameter(description = "Harvest date") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate harvestDate,
            @Parameter(description = "Field location") @RequestParam(required = false) String fieldLocation,
            @Parameter(description = "Growing conditions") @RequestParam(required = false) String growingConditions,
            @Parameter(description = "Processing method") @RequestParam(required = false) String processingMethod,
            @Parameter(description = "Quality grade") @RequestParam(required = false) String qualityGrade) {
        
        log.debug("Creating product lot for product: {}", productId);
        
        ProductLot response = productLotService.createProductLot(productId, lotNumber, totalQuantity,
            productionDate, expiryDate, harvestDate, fieldLocation, growingConditions,
            processingMethod, qualityGrade);
        
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
    
    /**
     * Get product lot by ID
     */
    @GetMapping("/{lotId}")
    @PreAuthorize("hasRole('PRODUCER') or hasRole('BUYER') or hasRole('ADMIN')")
    @Operation(summary = "Get product lot by ID", description = "Retrieve product lot details by ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Product lot found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "404", description = "Product lot not found")
    })
    public ResponseEntity<ProductLot> getProductLot(
            @Parameter(description = "Product lot ID") @PathVariable Long lotId) {
        
        log.debug("Getting product lot: {}", lotId);
        
        ProductLot response = productLotService.getProductLot(lotId);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get product lot by lot number
     */
    @GetMapping("/lot-number/{lotNumber}")
    @PreAuthorize("hasRole('PRODUCER') or hasRole('BUYER') or hasRole('ADMIN')")
    @Operation(summary = "Get product lot by lot number", description = "Retrieve product lot by lot number")
    public ResponseEntity<ProductLot> getProductLotByLotNumber(
            @Parameter(description = "Lot number") @PathVariable String lotNumber) {
        
        log.debug("Getting product lot by lot number: {}", lotNumber);
        
        ProductLot response = productLotService.getProductLotByLotNumber(lotNumber);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get product lots by product ID
     */
    @GetMapping("/product/{productId}")
    @PreAuthorize("hasRole('PRODUCER') or hasRole('BUYER') or hasRole('ADMIN')")
    @Operation(summary = "Get product lots by product", description = "Retrieve all lots for a specific product")
    public ResponseEntity<Page<ProductLot>> getProductLotsByProduct(
            @Parameter(description = "Product ID") @PathVariable Long productId,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "DESC") String sortDir) {
        
        Sort.Direction direction = sortDir.equalsIgnoreCase("ASC") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        Page<ProductLot> response = productLotService.getProductLotsByProductId(productId, pageable);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get my product lots (current producer)
     */
    @GetMapping("/my")
    @PreAuthorize("hasRole('PRODUCER')")
    @Operation(summary = "Get my product lots", description = "Retrieve product lots belonging to current producer")
    public ResponseEntity<Page<ProductLot>> getMyProductLots(
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "DESC") String sortDir) {
        
        Sort.Direction direction = sortDir.equalsIgnoreCase("ASC") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        Page<ProductLot> response = productLotService.getMyProductLots(pageable);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get product lots by producer
     */
    @GetMapping("/producer/{producerId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get product lots by producer", description = "Retrieve product lots for a specific producer")
    public ResponseEntity<Page<ProductLot>> getProductLotsByProducer(
            @Parameter(description = "Producer ID") @PathVariable Long producerId,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<ProductLot> response = productLotService.getProductLotsByProducerId(producerId, pageable);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get available product lots
     */
    @GetMapping("/available")
    @PreAuthorize("hasRole('PRODUCER') or hasRole('BUYER')")
    @Operation(summary = "Get available product lots", description = "Retrieve product lots with available quantity")
    public ResponseEntity<List<ProductLot>> getAvailableProductLots(
            @Parameter(description = "Product ID") @RequestParam(required = false) Long productId,
            @Parameter(description = "Producer ID") @RequestParam(required = false) Long producerId) {
        
        List<ProductLot> response = productLotService.getAvailableProductLots(productId, producerId);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get sold out product lots
     */
    @GetMapping("/sold-out")
    @PreAuthorize("hasRole('PRODUCER') or hasRole('ADMIN')")
    @Operation(summary = "Get sold out product lots", description = "Retrieve product lots that are sold out")
    public ResponseEntity<List<ProductLot>> getSoldOutProductLots(
            @Parameter(description = "Product ID") @RequestParam(required = false) Long productId,
            @Parameter(description = "Producer ID") @RequestParam(required = false) Long producerId) {
        
        List<ProductLot> response = productLotService.getSoldOutProductLots(productId, producerId);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get expiring product lots
     */
    @GetMapping("/expiring")
    @PreAuthorize("hasRole('PRODUCER')")
    @Operation(summary = "Get expiring product lots", description = "Retrieve product lots expiring within specified days")
    public ResponseEntity<List<ProductLot>> getExpiringProductLots(
            @Parameter(description = "Number of days") @RequestParam(defaultValue = "30") int days,
            @Parameter(description = "Producer ID") @RequestParam(required = false) Long producerId) {
        
        List<ProductLot> response = productLotService.getExpiringProductLots(days, producerId);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get expired product lots
     */
    @GetMapping("/expired")
    @PreAuthorize("hasRole('PRODUCER')")
    @Operation(summary = "Get expired product lots", description = "Retrieve product lots that have expired")
    public ResponseEntity<List<ProductLot>> getExpiredProductLots(
            @Parameter(description = "Producer ID") @RequestParam(required = false) Long producerId) {
        
        List<ProductLot> response = productLotService.getExpiredProductLots(producerId);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get product lots by quality grade
     */
    @GetMapping("/quality/{qualityGrade}")
    @PreAuthorize("hasRole('PRODUCER') or hasRole('BUYER') or hasRole('ADMIN')")
    @Operation(summary = "Get product lots by quality grade", description = "Retrieve product lots by quality grade")
    public ResponseEntity<List<ProductLot>> getProductLotsByQualityGrade(
            @Parameter(description = "Quality grade") @PathVariable String qualityGrade,
            @Parameter(description = "Product ID") @RequestParam(required = false) Long productId,
            @Parameter(description = "Producer ID") @RequestParam(required = false) Long producerId) {
        
        List<ProductLot> response = productLotService.getProductLotsByQualityGrade(qualityGrade, productId, producerId);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get product lots by production date range
     */
    @GetMapping("/production-date-range")
    @PreAuthorize("hasRole('PRODUCER') or hasRole('BUYER') or hasRole('ADMIN')")
    @Operation(summary = "Get product lots by production date range", description = "Retrieve product lots by production date range")
    public ResponseEntity<List<ProductLot>> getProductLotsByProductionDateRange(
            @Parameter(description = "Start date") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @Parameter(description = "Product ID") @RequestParam(required = false) Long productId,
            @Parameter(description = "Producer ID") @RequestParam(required = false) Long producerId) {
        
        List<ProductLot> response = productLotService.getProductLotsByProductionDateRange(
            startDate, endDate, productId, producerId);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get product lots by harvest date range
     */
    @GetMapping("/harvest-date-range")
    @PreAuthorize("hasRole('PRODUCER') or hasRole('BUYER') or hasRole('ADMIN')")
    @Operation(summary = "Get product lots by harvest date range", description = "Retrieve product lots by harvest date range")
    public ResponseEntity<List<ProductLot>> getProductLotsByHarvestDateRange(
            @Parameter(description = "Start date") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @Parameter(description = "Product ID") @RequestParam(required = false) Long productId,
            @Parameter(description = "Producer ID") @RequestParam(required = false) Long producerId) {
        
        List<ProductLot> response = productLotService.getProductLotsByHarvestDateRange(
            startDate, endDate, productId, producerId);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get product lots by field location
     */
    @GetMapping("/field/{fieldLocation}")
    @PreAuthorize("hasRole('PRODUCER') or hasRole('ADMIN')")
    @Operation(summary = "Get product lots by field location", description = "Retrieve product lots from specific field location")
    public ResponseEntity<List<ProductLot>> getProductLotsByFieldLocation(
            @Parameter(description = "Field location") @PathVariable String fieldLocation,
            @Parameter(description = "Producer ID") @RequestParam(required = false) Long producerId) {
        
        List<ProductLot> response = productLotService.getProductLotsByFieldLocation(fieldLocation, producerId);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Search product lots
     */
    @GetMapping("/search")
    @PreAuthorize("hasRole('PRODUCER') or hasRole('BUYER') or hasRole('ADMIN')")
    @Operation(summary = "Search product lots", description = "Search product lots by multiple criteria")
    public ResponseEntity<Page<ProductLot>> searchProductLots(
            @Parameter(description = "Search term") @RequestParam(required = false) String search,
            @Parameter(description = "Product ID") @RequestParam(required = false) Long productId,
            @Parameter(description = "Producer ID") @RequestParam(required = false) Long producerId,
            @Parameter(description = "Quality grade") @RequestParam(required = false) String qualityGrade,
            @Parameter(description = "Field location") @RequestParam(required = false) String fieldLocation,
            @Parameter(description = "Production start date") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate productionStartDate,
            @Parameter(description = "Production end date") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate productionEndDate,
            @Parameter(description = "Expiry start date") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate expiryStartDate,
            @Parameter(description = "Expiry end date") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate expiryEndDate,
            @Parameter(description = "Minimum available quantity") @RequestParam(required = false) BigDecimal minAvailableQty,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "DESC") String sortDir) {
        
        Sort.Direction direction = sortDir.equalsIgnoreCase("ASC") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        Page<ProductLot> response = productLotService.searchProductLots(search, productId, producerId,
            qualityGrade, fieldLocation, productionStartDate, productionEndDate, 
            expiryStartDate, expiryEndDate, minAvailableQty, pageable);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Reserve quantity from product lot
     */
    @PostMapping("/{lotId}/reserve")
    @PreAuthorize("hasRole('PRODUCER') or hasRole('BUYER')")
    @Operation(summary = "Reserve quantity", description = "Reserve quantity from product lot for an order")
    public ResponseEntity<Void> reserveQuantity(
            @Parameter(description = "Lot ID") @PathVariable Long lotId,
            @Parameter(description = "Quantity to reserve") @RequestParam BigDecimal quantity,
            @Parameter(description = "Order ID") @RequestParam String orderId) {
        
        log.debug("Reserving quantity: {} from lot: {}", quantity, lotId);
        productLotService.reserveQuantity(lotId, quantity, orderId);
        return ResponseEntity.ok().build();
    }
    
    /**
     * Release reserved quantity
     */
    @PostMapping("/{lotId}/release")
    @PreAuthorize("hasRole('PRODUCER') or hasRole('BUYER')")
    @Operation(summary = "Release reserved quantity", description = "Release previously reserved quantity")
    public ResponseEntity<Void> releaseReservedQuantity(
            @Parameter(description = "Lot ID") @PathVariable Long lotId,
            @Parameter(description = "Quantity to release") @RequestParam BigDecimal quantity,
            @Parameter(description = "Reason for release") @RequestParam(required = false) String reason) {
        
        log.debug("Releasing reserved quantity: {} from lot: {}", quantity, lotId);
        productLotService.releaseReservedQuantity(lotId, quantity, reason);
        return ResponseEntity.ok().build();
    }
    
    /**
     * Complete sale
     */
    @PostMapping("/{lotId}/complete-sale")
    @PreAuthorize("hasRole('PRODUCER') or hasRole('BUYER')")
    @Operation(summary = "Complete sale", description = "Complete sale by moving reserved quantity to sold")
    public ResponseEntity<Void> completeSale(
            @Parameter(description = "Lot ID") @PathVariable Long lotId,
            @Parameter(description = "Sale quantity") @RequestParam BigDecimal quantity,
            @Parameter(description = "Order ID") @RequestParam String orderId) {
        
        log.debug("Completing sale: {} from lot: {}", quantity, lotId);
        productLotService.completeSale(lotId, quantity, orderId);
        return ResponseEntity.ok().build();
    }
    
    /**
     * Update production information
     */
    @PatchMapping("/{lotId}/production-info")
    @PreAuthorize("hasRole('PRODUCER')")
    @Operation(summary = "Update production information", description = "Update production and traceability information")
    public ResponseEntity<Void> updateProductionInfo(
            @Parameter(description = "Lot ID") @PathVariable Long lotId,
            @Parameter(description = "Field location") @RequestParam(required = false) String fieldLocation,
            @Parameter(description = "Growing conditions") @RequestParam(required = false) String growingConditions,
            @Parameter(description = "Processing method") @RequestParam(required = false) String processingMethod,
            @Parameter(description = "Quality grade") @RequestParam(required = false) String qualityGrade,
            @Parameter(description = "Harvest date") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate harvestDate) {
        
        log.debug("Updating production info for lot: {}", lotId);
        productLotService.updateProductionInfo(lotId, fieldLocation, growingConditions,
            processingMethod, qualityGrade, harvestDate);
        return ResponseEntity.ok().build();
    }
    
    /**
     * Update expiry date
     */
    @PatchMapping("/{lotId}/expiry-date")
    @PreAuthorize("hasRole('PRODUCER')")
    @Operation(summary = "Update expiry date", description = "Update the expiry date of a product lot")
    public ResponseEntity<Void> updateExpiryDate(
            @Parameter(description = "Lot ID") @PathVariable Long lotId,
            @Parameter(description = "New expiry date") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate expiryDate) {
        
        log.debug("Updating expiry date for lot: {}", lotId);
        productLotService.updateExpiryDate(lotId, expiryDate);
        return ResponseEntity.ok().build();
    }
    
    /**
     * Update lot as expired
     */
    @PostMapping("/{lotId}/mark-expired")
    @PreAuthorize("hasRole('PRODUCER')")
    @Operation(summary = "Mark lot as expired", description = "Mark a product lot as expired")
    public ResponseEntity<Void> markAsExpired(
            @Parameter(description = "Lot ID") @PathVariable Long lotId) {
        
        log.debug("Marking lot as expired: {}", lotId);
        productLotService.markAsExpired(lotId);
        return ResponseEntity.ok().build();
    }
    
    /**
     * Get traceability chain
     */
    @GetMapping("/{lotId}/traceability")
    @PreAuthorize("hasRole('PRODUCER') or hasRole('BUYER') or hasRole('ADMIN')")
    @Operation(summary = "Get traceability chain", description = "Get full traceability information for a product lot")
    public ResponseEntity<ProductLot> getTraceabilityInfo(
            @Parameter(description = "Lot ID") @PathVariable Long lotId) {
        
        ProductLot response = productLotService.getProductLot(lotId);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Delete product lot
     */
    @DeleteMapping("/{lotId}")
    @PreAuthorize("hasRole('PRODUCER')")
    @Operation(summary = "Delete product lot", description = "Soft delete a product lot")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Product lot deleted successfully"),
        @ApiResponse(responseCode = "400", description = "Cannot delete lot with sold quantity"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "404", description = "Product lot not found")
    })
    public ResponseEntity<Void> deleteProductLot(
            @Parameter(description = "Lot ID") @PathVariable Long lotId) {
        
        log.debug("Deleting product lot: {}", lotId);
        productLotService.deleteProductLot(lotId);
        return ResponseEntity.noContent().build();
    }
}