package com.procgrid.productservice.controller;

import com.procgrid.productservice.model.Inventory;
import com.procgrid.productservice.service.InventoryService;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * REST Controller for Inventory operations
 * Provides API endpoints for inventory management, stock tracking, and analytics
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/inventory")
@RequiredArgsConstructor
@Tag(name = "Inventory Management", description = "APIs for managing product inventory and stock")
@SecurityRequirement(name = "bearerAuth")
public class InventoryController {
    
    private final InventoryService inventoryService;
    
    /**
     * Create new inventory
     */
    @PostMapping
    @PreAuthorize("hasRole('PRODUCER')")
    @Operation(summary = "Create new inventory", description = "Create inventory record for a product")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Inventory created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request data"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Producer role required")
    })
    public ResponseEntity<Inventory> createInventory(
            @Parameter(description = "Product ID") @RequestParam Long productId,
            @Parameter(description = "Initial quantity") @RequestParam BigDecimal initialQuantity,
            @Parameter(description = "Cost per unit") @RequestParam BigDecimal costPerUnit,
            @Parameter(description = "Minimum stock level") @RequestParam(required = false) BigDecimal minStockLevel,
            @Parameter(description = "Maximum stock level") @RequestParam(required = false) BigDecimal maxStockLevel,
            @Parameter(description = "Reorder quantity") @RequestParam(required = false) BigDecimal reorderQuantity) {
        
        log.debug("Creating inventory for product: {}", productId);
        
        Inventory response = inventoryService.createInventory(productId, initialQuantity, costPerUnit,
            minStockLevel, maxStockLevel, reorderQuantity);
        
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
    
    /**
     * Get inventory by ID
     */
    @GetMapping("/{inventoryId}")
    @PreAuthorize("hasRole('PRODUCER') or hasRole('ADMIN')")
    @Operation(summary = "Get inventory by ID", description = "Retrieve inventory details by ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Inventory found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "404", description = "Inventory not found")
    })
    public ResponseEntity<Inventory> getInventory(
            @Parameter(description = "Inventory ID") @PathVariable Long inventoryId) {
        
        log.debug("Getting inventory: {}", inventoryId);
        
        Inventory response = inventoryService.getInventory(inventoryId);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get inventory by product ID
     */
    @GetMapping("/product/{productId}")
    @PreAuthorize("hasRole('PRODUCER') or hasRole('ADMIN')")
    @Operation(summary = "Get inventory by product", description = "Retrieve inventory for a specific product")
    public ResponseEntity<Inventory> getInventoryByProduct(
            @Parameter(description = "Product ID") @PathVariable Long productId) {
        
        log.debug("Getting inventory for product: {}", productId);
        
        Inventory response = inventoryService.getInventoryByProductId(productId);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get my inventory (current producer)
     */
    @GetMapping("/my")
    @PreAuthorize("hasRole('PRODUCER')")
    @Operation(summary = "Get my inventory", description = "Retrieve inventory belonging to current producer")
    public ResponseEntity<Page<Inventory>> getMyInventory(
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "DESC") String sortDir) {
        
        Sort.Direction direction = sortDir.equalsIgnoreCase("ASC") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        Page<Inventory> response = inventoryService.getMyInventory(pageable);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get inventory by producer
     */
    @GetMapping("/producer/{producerId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get inventory by producer", description = "Retrieve inventory for a specific producer")
    public ResponseEntity<Page<Inventory>> getProducerInventory(
            @Parameter(description = "Producer ID") @PathVariable Long producerId,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Inventory> response = inventoryService.getProducerInventory(producerId, pageable);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get low stock inventory
     */
    @GetMapping("/low-stock")
    @PreAuthorize("hasRole('PRODUCER')")
    @Operation(summary = "Get low stock inventory", description = "Retrieve inventory items with low stock")
    public ResponseEntity<List<Inventory>> getLowStockInventory(
            @Parameter(description = "Producer ID") @RequestParam(required = false) Long producerId) {
        
        List<Inventory> response;
        if (producerId != null) {
            response = inventoryService.getLowStockInventory(producerId);
        } else {
            // Get for current producer
            response = inventoryService.getLowStockInventory(null);
        }
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get out of stock inventory
     */
    @GetMapping("/out-of-stock")
    @PreAuthorize("hasRole('PRODUCER')")
    @Operation(summary = "Get out of stock inventory", description = "Retrieve inventory items that are out of stock")
    public ResponseEntity<List<Inventory>> getOutOfStockInventory(
            @Parameter(description = "Producer ID") @RequestParam(required = false) Long producerId) {
        
        List<Inventory> response;
        if (producerId != null) {
            response = inventoryService.getOutOfStockInventory(producerId);
        } else {
            response = inventoryService.getOutOfStockInventory(null);
        }
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get overstocked inventory
     */
    @GetMapping("/overstocked")
    @PreAuthorize("hasRole('PRODUCER')")
    @Operation(summary = "Get overstocked inventory", description = "Retrieve inventory items that are overstocked")
    public ResponseEntity<List<Inventory>> getOverstockedInventory(
            @Parameter(description = "Producer ID") @RequestParam(required = false) Long producerId) {
        
        List<Inventory> response;
        if (producerId != null) {
            response = inventoryService.getOverstockedInventory(producerId);
        } else {
            response = inventoryService.getOverstockedInventory(null);
        }
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get inventory requiring stock count
     */
    @GetMapping("/requiring-count")
    @PreAuthorize("hasRole('PRODUCER')")
    @Operation(summary = "Get inventory requiring stock count", description = "Retrieve inventory items requiring stock count")
    public ResponseEntity<List<Inventory>> getInventoryRequiringStockCount() {
        
        List<Inventory> response = inventoryService.getInventoryRequiringStockCount();
        return ResponseEntity.ok(response);
    }
    
    /**
     * Add stock to inventory
     */
    @PostMapping("/{inventoryId}/add-stock")
    @PreAuthorize("hasRole('PRODUCER')")
    @Operation(summary = "Add stock", description = "Add stock quantity to inventory")
    public ResponseEntity<Void> addStock(
            @Parameter(description = "Inventory ID") @PathVariable Long inventoryId,
            @Parameter(description = "Quantity to add") @RequestParam BigDecimal quantity,
            @Parameter(description = "Reason for adding stock") @RequestParam(required = false) String reason) {
        
        log.debug("Adding stock: {} to inventory: {}", quantity, inventoryId);
        inventoryService.addStock(inventoryId, quantity, reason);
        return ResponseEntity.ok().build();
    }
    
    /**
     * Reserve quantity from inventory
     */
    @PostMapping("/{inventoryId}/reserve")
    @PreAuthorize("hasRole('PRODUCER') or hasRole('BUYER')")
    @Operation(summary = "Reserve quantity", description = "Reserve quantity from inventory for an order")
    public ResponseEntity<Void> reserveQuantity(
            @Parameter(description = "Inventory ID") @PathVariable Long inventoryId,
            @Parameter(description = "Quantity to reserve") @RequestParam BigDecimal quantity,
            @Parameter(description = "Order ID") @RequestParam String orderId) {
        
        log.debug("Reserving quantity: {} from inventory: {}", quantity, inventoryId);
        inventoryService.reserveQuantity(inventoryId, quantity, orderId);
        return ResponseEntity.ok().build();
    }
    
    /**
     * Release reserved quantity
     */
    @PostMapping("/{inventoryId}/release")
    @PreAuthorize("hasRole('PRODUCER') or hasRole('BUYER')")
    @Operation(summary = "Release reserved quantity", description = "Release previously reserved quantity")
    public ResponseEntity<Void> releaseReservedQuantity(
            @Parameter(description = "Inventory ID") @PathVariable Long inventoryId,
            @Parameter(description = "Quantity to release") @RequestParam BigDecimal quantity,
            @Parameter(description = "Reason for release") @RequestParam(required = false) String reason) {
        
        log.debug("Releasing reserved quantity: {} from inventory: {}", quantity, inventoryId);
        inventoryService.releaseReservedQuantity(inventoryId, quantity, reason);
        return ResponseEntity.ok().build();
    }
    
    /**
     * Complete sale
     */
    @PostMapping("/{inventoryId}/complete-sale")
    @PreAuthorize("hasRole('PRODUCER') or hasRole('BUYER')")
    @Operation(summary = "Complete sale", description = "Complete sale by moving reserved quantity to sold")
    public ResponseEntity<Void> completeSale(
            @Parameter(description = "Inventory ID") @PathVariable Long inventoryId,
            @Parameter(description = "Sale quantity") @RequestParam BigDecimal quantity,
            @Parameter(description = "Order ID") @RequestParam String orderId) {
        
        log.debug("Completing sale: {} from inventory: {}", quantity, inventoryId);
        inventoryService.completeSale(inventoryId, quantity, orderId);
        return ResponseEntity.ok().build();
    }
    
    /**
     * Remove damaged stock
     */
    @PostMapping("/{inventoryId}/remove-damaged")
    @PreAuthorize("hasRole('PRODUCER')")
    @Operation(summary = "Remove damaged stock", description = "Remove damaged stock from inventory")
    public ResponseEntity<Void> removeDamagedStock(
            @Parameter(description = "Inventory ID") @PathVariable Long inventoryId,
            @Parameter(description = "Damaged quantity") @RequestParam BigDecimal quantity,
            @Parameter(description = "Damage reason") @RequestParam(required = false) String reason) {
        
        log.debug("Removing damaged stock: {} from inventory: {}", quantity, inventoryId);
        inventoryService.removeDamagedStock(inventoryId, quantity, reason);
        return ResponseEntity.ok().build();
    }
    
    /**
     * Update stock levels
     */
    @PatchMapping("/{inventoryId}/stock-levels")
    @PreAuthorize("hasRole('PRODUCER')")
    @Operation(summary = "Update stock levels", description = "Update minimum, maximum, and reorder stock levels")
    public ResponseEntity<Void> updateStockLevels(
            @Parameter(description = "Inventory ID") @PathVariable Long inventoryId,
            @Parameter(description = "Minimum stock level") @RequestParam(required = false) BigDecimal minLevel,
            @Parameter(description = "Maximum stock level") @RequestParam(required = false) BigDecimal maxLevel,
            @Parameter(description = "Reorder quantity") @RequestParam(required = false) BigDecimal reorderQty) {
        
        log.debug("Updating stock levels for inventory: {}", inventoryId);
        inventoryService.updateStockLevels(inventoryId, minLevel, maxLevel, reorderQty);
        return ResponseEntity.ok().build();
    }
    
    /**
     * Update cost and value
     */
    @PatchMapping("/{inventoryId}/cost-value")
    @PreAuthorize("hasRole('PRODUCER')")
    @Operation(summary = "Update cost and value", description = "Update inventory cost and value per unit")
    public ResponseEntity<Void> updateCostAndValue(
            @Parameter(description = "Inventory ID") @PathVariable Long inventoryId,
            @Parameter(description = "Cost per unit") @RequestParam BigDecimal costPerUnit,
            @Parameter(description = "Value per unit") @RequestParam BigDecimal valuePerUnit) {
        
        log.debug("Updating cost and value for inventory: {}", inventoryId);
        inventoryService.updateCostAndValue(inventoryId, costPerUnit, valuePerUnit);
        return ResponseEntity.ok().build();
    }
    
    /**
     * Update stock count date
     */
    @PostMapping("/{inventoryId}/stock-count")
    @PreAuthorize("hasRole('PRODUCER')")
    @Operation(summary = "Update stock count date", description = "Update the last stock count date to current date")
    public ResponseEntity<Void> updateStockCountDate(
            @Parameter(description = "Inventory ID") @PathVariable Long inventoryId) {
        
        log.debug("Updating stock count date for inventory: {}", inventoryId);
        inventoryService.updateStockCountDate(inventoryId);
        return ResponseEntity.ok().build();
    }
    
    /**
     * Get inventory statistics
     */
    @GetMapping("/stats/producer/{producerId}")
    @PreAuthorize("hasRole('PRODUCER') or hasRole('ADMIN')")
    @Operation(summary = "Get inventory statistics", description = "Retrieve inventory statistics for a producer")
    public ResponseEntity<Map<String, Object>> getInventoryStats(
            @Parameter(description = "Producer ID") @PathVariable Long producerId) {
        
        Map<String, Object> stats = inventoryService.getInventoryStats(producerId);
        return ResponseEntity.ok(stats);
    }
    
    /**
     * Get inventory value analytics
     */
    @GetMapping("/analytics/value")
    @PreAuthorize("hasRole('PRODUCER') or hasRole('ADMIN')")
    @Operation(summary = "Get inventory value analytics", description = "Retrieve inventory value analytics by date range")
    public ResponseEntity<List<Map<String, Object>>> getInventoryValueByDateRange(
            @Parameter(description = "Producer ID") @RequestParam Long producerId,
            @Parameter(description = "Start date (YYYY-MM-DD)") @RequestParam String startDate,
            @Parameter(description = "End date (YYYY-MM-DD)") @RequestParam String endDate) {
        
        List<Map<String, Object>> analytics = inventoryService.getInventoryValueByDateRange(
            producerId, startDate, endDate);
        return ResponseEntity.ok(analytics);
    }
    
    /**
     * Get inventory movement history
     */
    @GetMapping("/{inventoryId}/movements")
    @PreAuthorize("hasRole('PRODUCER') or hasRole('ADMIN')")
    @Operation(summary = "Get inventory movement history", description = "Retrieve inventory movement history")
    public ResponseEntity<List<Map<String, Object>>> getInventoryMovementHistory(
            @Parameter(description = "Inventory ID") @PathVariable Long inventoryId,
            @Parameter(description = "Number of days") @RequestParam(defaultValue = "30") int days) {
        
        List<Map<String, Object>> movements = inventoryService.getInventoryMovementHistory(inventoryId, days);
        return ResponseEntity.ok(movements);
    }
    
    /**
     * Delete inventory
     */
    @DeleteMapping("/{inventoryId}")
    @PreAuthorize("hasRole('PRODUCER')")
    @Operation(summary = "Delete inventory", description = "Soft delete an inventory record")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Inventory deleted successfully"),
        @ApiResponse(responseCode = "400", description = "Cannot delete inventory with remaining stock"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "404", description = "Inventory not found")
    })
    public ResponseEntity<Void> deleteInventory(
            @Parameter(description = "Inventory ID") @PathVariable Long inventoryId) {
        
        log.debug("Deleting inventory: {}", inventoryId);
        inventoryService.deleteInventory(inventoryId);
        return ResponseEntity.noContent().build();
    }
}