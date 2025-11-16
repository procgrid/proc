package com.procgrid.productservice.service;

import com.procgrid.productservice.exception.InventoryNotFoundException;
import com.procgrid.productservice.exception.UnauthorizedAccessException;
import com.procgrid.productservice.exception.ValidationException;
import com.procgrid.productservice.exception.InsufficientStockException;
import com.procgrid.productservice.model.Inventory;
import com.procgrid.productservice.model.Product;
import com.procgrid.productservice.repository.InventoryRepository;
import com.procgrid.productservice.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
 * Service layer for Inventory operations
 * Provides business logic for stock management, reservations, and inventory tracking
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryService {
    
    private final InventoryRepository inventoryRepository;
    private final ProductRepository productRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    
    /**
     * Create new inventory for product
     */
    @Transactional
    public Inventory createInventory(Long productId, BigDecimal initialQuantity, 
                                   BigDecimal costPerUnit, BigDecimal minStockLevel, 
                                   BigDecimal maxStockLevel, BigDecimal reorderQuantity) {
        log.debug("Creating inventory for product: {} with initial quantity: {}", 
            productId, initialQuantity);
        
        // Validate product exists and belongs to current producer
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new ValidationException("Product not found: " + productId));
        
        validateProductOwnership(product);
        
        // Check if inventory already exists for this product
        if (inventoryRepository.existsByProductId(productId)) {
            throw new ValidationException("Inventory already exists for product: " + productId);
        }
        
        // Validate inventory parameters
        validateInventoryParameters(initialQuantity, costPerUnit, minStockLevel, 
            maxStockLevel, reorderQuantity);
        
        // Create inventory
        Inventory inventory = Inventory.builder()
            .productId(productId)
            .producerId(product.getProducerId())
            .totalQuantity(initialQuantity)
            .availableQuantity(initialQuantity)
            .reservedQuantity(BigDecimal.ZERO)
            .soldQuantity(BigDecimal.ZERO)
            .damagedQuantity(BigDecimal.ZERO)
            .costPerUnit(costPerUnit)
            .minStockLevel(minStockLevel)
            .maxStockLevel(maxStockLevel)
            .reorderQuantity(reorderQuantity)
            .createdBy(getCurrentUsername())
            .createdAt(LocalDateTime.now())
            .build();
        
        Inventory savedInventory = inventoryRepository.save(inventory);
        
        // Publish inventory created event
        publishInventoryEvent("inventory.created", savedInventory);
        
        log.info("Created inventory with ID: {} for product: {}", 
            savedInventory.getId(), productId);
        
        return savedInventory;
    }
    
    /**
     * Get inventory by ID
     */
    @Cacheable(value = "inventory", key = "#id")
    public Inventory getInventory(Long id) {
        log.debug("Getting inventory by ID: {}", id);
        
        Inventory inventory = inventoryRepository.findById(id)
            .orElseThrow(() -> new InventoryNotFoundException("Inventory not found: " + id));
        
        validateInventoryAccess(inventory);
        return inventory;
    }
    
    /**
     * Get inventory by product ID
     */
    @Cacheable(value = "inventory", key = "'product:' + #productId")
    public Inventory getInventoryByProductId(Long productId) {
        log.debug("Getting inventory for product: {}", productId);
        
        Inventory inventory = inventoryRepository.findByProductId(productId)
            .orElseThrow(() -> new InventoryNotFoundException("Inventory not found for product: " + productId));
        
        validateInventoryAccess(inventory);
        return inventory;
    }
    
    /**
     * Get inventory list for producer with pagination
     */
    public Page<Inventory> getProducerInventory(Long producerId, Pageable pageable) {
        log.debug("Getting inventory for producer: {}, page: {}, size: {}", 
            producerId, pageable.getPageNumber(), pageable.getPageSize());
        
        // Validate producer access
        validateProducerAccess(producerId);
        
        List<Inventory> inventories = inventoryRepository.findByProducerId(
            producerId, pageable.getPageNumber(), pageable.getPageSize());
        
        Long totalCount = inventoryRepository.countByProducerId(producerId);
        
        return new PageImpl<>(inventories, pageable, totalCount);
    }
    
    /**
     * Get current producer's inventory
     */
    public Page<Inventory> getMyInventory(Pageable pageable) {
        Long producerId = getCurrentProducerId();
        return getProducerInventory(producerId, pageable);
    }
    
    /**
     * Get low stock inventory for producer
     */
    public List<Inventory> getLowStockInventory(Long producerId) {
        log.debug("Getting low stock inventory for producer: {}", producerId);
        
        validateProducerAccess(producerId);
        return inventoryRepository.findLowStockByProducerId(producerId);
    }
    
    /**
     * Get out of stock inventory for producer
     */
    public List<Inventory> getOutOfStockInventory(Long producerId) {
        log.debug("Getting out of stock inventory for producer: {}", producerId);
        
        validateProducerAccess(producerId);
        return inventoryRepository.findOutOfStockByProducerId(producerId);
    }
    
    /**
     * Get overstocked inventory for producer
     */
    public List<Inventory> getOverstockedInventory(Long producerId) {
        log.debug("Getting overstocked inventory for producer: {}", producerId);
        
        validateProducerAccess(producerId);
        return inventoryRepository.findOverstockedByProducerId(producerId);
    }
    
    /**
     * Get inventory requiring stock count
     */
    public List<Inventory> getInventoryRequiringStockCount() {
        Long producerId = getCurrentProducerId();
        List<Inventory> allInventoryNeedingCount = inventoryRepository.findInventoryRequiringStockCount();
        
        // Filter by producer
        return allInventoryNeedingCount.stream()
            .filter(inventory -> inventory.getProducerId().equals(producerId))
            .toList();
    }
    
    /**
     * Add stock to inventory
     */
    @Transactional
    @CacheEvict(value = {"inventory", "inventoryStats"}, allEntries = true)
    public void addStock(Long inventoryId, BigDecimal quantity, String reason) {
        log.debug("Adding stock: {} to inventory: {}", quantity, inventoryId);
        
        validateQuantity(quantity, "Additional quantity");
        
        Inventory inventory = getInventory(inventoryId);
        validateInventoryOwnership(inventory);
        
        boolean updated = inventoryRepository.addStock(inventoryId, quantity, getCurrentUsername());
        
        if (updated) {
            // Publish stock added event
            Map<String, Object> eventData = Map.of(
                "inventoryId", inventoryId,
                "productId", inventory.getProductId(),
                "quantity", quantity,
                "reason", reason != null ? reason : "Manual stock addition",
                "updatedBy", getCurrentUsername()
            );
            kafkaTemplate.send("inventory.stock.added", eventData);
            
            log.info("Added {} stock to inventory: {}", quantity, inventoryId);
        } else {
            throw new ValidationException("Failed to add stock to inventory");
        }
    }
    
    /**
     * Reserve quantity for order
     */
    @Transactional
    @CacheEvict(value = "inventory", key = "#inventoryId")
    public void reserveQuantity(Long inventoryId, BigDecimal quantity, String orderId) {
        log.debug("Reserving quantity: {} from inventory: {} for order: {}", 
            quantity, inventoryId, orderId);
        
        validateQuantity(quantity, "Reserve quantity");
        
        Inventory inventory = getInventory(inventoryId);
        
        // Check if enough stock is available
        if (inventory.getAvailableQuantity().compareTo(quantity) < 0) {
            throw new InsufficientStockException("Insufficient stock available. Required: " + 
                quantity + ", Available: " + inventory.getAvailableQuantity());
        }
        
        boolean reserved = inventoryRepository.reserveQuantity(inventoryId, quantity, getCurrentUsername());
        
        if (reserved) {
            // Publish reservation event
            Map<String, Object> eventData = Map.of(
                "inventoryId", inventoryId,
                "productId", inventory.getProductId(),
                "quantity", quantity,
                "orderId", orderId,
                "reservedBy", getCurrentUsername()
            );
            kafkaTemplate.send("inventory.quantity.reserved", eventData);
            
            log.info("Reserved {} quantity from inventory: {} for order: {}", 
                quantity, inventoryId, orderId);
        } else {
            throw new ValidationException("Failed to reserve quantity");
        }
    }
    
    /**
     * Release reserved quantity
     */
    @Transactional
    @CacheEvict(value = "inventory", key = "#inventoryId")
    public void releaseReservedQuantity(Long inventoryId, BigDecimal quantity, String reason) {
        log.debug("Releasing reserved quantity: {} from inventory: {}", quantity, inventoryId);
        
        validateQuantity(quantity, "Release quantity");
        
        Inventory inventory = getInventory(inventoryId);
        validateInventoryOwnership(inventory);
        
        // Check if enough reserved stock exists
        if (inventory.getReservedQuantity().compareTo(quantity) < 0) {
            throw new ValidationException("Insufficient reserved quantity. Requested: " + 
                quantity + ", Reserved: " + inventory.getReservedQuantity());
        }
        
        boolean released = inventoryRepository.releaseReservedQuantity(inventoryId, quantity, getCurrentUsername());
        
        if (released) {
            // Publish release event
            Map<String, Object> eventData = Map.of(
                "inventoryId", inventoryId,
                "productId", inventory.getProductId(),
                "quantity", quantity,
                "reason", reason != null ? reason : "Manual release",
                "releasedBy", getCurrentUsername()
            );
            kafkaTemplate.send("inventory.quantity.released", eventData);
            
            log.info("Released {} reserved quantity from inventory: {}", quantity, inventoryId);
        } else {
            throw new ValidationException("Failed to release reserved quantity");
        }
    }
    
    /**
     * Complete sale (move from reserved to sold)
     */
    @Transactional
    @CacheEvict(value = {"inventory", "inventoryStats"}, allEntries = true)
    public void completeSale(Long inventoryId, BigDecimal quantity, String orderId) {
        log.debug("Completing sale: {} from inventory: {} for order: {}", 
            quantity, inventoryId, orderId);
        
        validateQuantity(quantity, "Sale quantity");
        
        Inventory inventory = getInventory(inventoryId);
        
        // Check if enough reserved stock exists
        if (inventory.getReservedQuantity().compareTo(quantity) < 0) {
            throw new ValidationException("Insufficient reserved quantity for sale. Required: " + 
                quantity + ", Reserved: " + inventory.getReservedQuantity());
        }
        
        boolean completed = inventoryRepository.completeSale(inventoryId, quantity, getCurrentUsername());
        
        if (completed) {
            // Publish sale completed event
            Map<String, Object> eventData = Map.of(
                "inventoryId", inventoryId,
                "productId", inventory.getProductId(),
                "quantity", quantity,
                "orderId", orderId,
                "soldBy", getCurrentUsername()
            );
            kafkaTemplate.send("inventory.sale.completed", eventData);
            
            log.info("Completed sale of {} from inventory: {} for order: {}", 
                quantity, inventoryId, orderId);
        } else {
            throw new ValidationException("Failed to complete sale");
        }
    }
    
    /**
     * Remove damaged stock
     */
    @Transactional
    @CacheEvict(value = {"inventory", "inventoryStats"}, allEntries = true)
    public void removeDamagedStock(Long inventoryId, BigDecimal quantity, String reason) {
        log.debug("Removing damaged stock: {} from inventory: {}", quantity, inventoryId);
        
        validateQuantity(quantity, "Damaged quantity");
        
        Inventory inventory = getInventory(inventoryId);
        validateInventoryOwnership(inventory);
        
        // Check if enough total stock exists
        BigDecimal totalAvailable = inventory.getAvailableQuantity().add(inventory.getReservedQuantity());
        if (totalAvailable.compareTo(quantity) < 0) {
            throw new ValidationException("Insufficient stock to mark as damaged. Required: " + 
                quantity + ", Available: " + totalAvailable);
        }
        
        boolean removed = inventoryRepository.removeDamagedStock(inventoryId, quantity, getCurrentUsername());
        
        if (removed) {
            // Publish damaged stock event
            Map<String, Object> eventData = Map.of(
                "inventoryId", inventoryId,
                "productId", inventory.getProductId(),
                "quantity", quantity,
                "reason", reason != null ? reason : "Stock damage",
                "reportedBy", getCurrentUsername()
            );
            kafkaTemplate.send("inventory.stock.damaged", eventData);
            
            log.info("Removed {} damaged stock from inventory: {}", quantity, inventoryId);
        } else {
            throw new ValidationException("Failed to remove damaged stock");
        }
    }
    
    /**
     * Update stock levels (min, max, reorder)
     */
    @Transactional
    @CacheEvict(value = "inventory", key = "#inventoryId")
    public void updateStockLevels(Long inventoryId, BigDecimal minLevel, BigDecimal maxLevel, 
                                BigDecimal reorderQty) {
        log.debug("Updating stock levels for inventory: {}", inventoryId);
        
        Inventory inventory = getInventory(inventoryId);
        validateInventoryOwnership(inventory);
        validateStockLevels(minLevel, maxLevel, reorderQty);
        
        boolean updated = inventoryRepository.updateStockLevels(inventoryId, minLevel, 
            maxLevel, reorderQty, getCurrentUsername());
        
        if (updated) {
            // Publish stock levels updated event
            Map<String, Object> eventData = Map.of(
                "inventoryId", inventoryId,
                "productId", inventory.getProductId(),
                "minLevel", minLevel,
                "maxLevel", maxLevel,
                "reorderQty", reorderQty,
                "updatedBy", getCurrentUsername()
            );
            kafkaTemplate.send("inventory.levels.updated", eventData);
            
            log.info("Updated stock levels for inventory: {}", inventoryId);
        } else {
            throw new ValidationException("Failed to update stock levels");
        }
    }
    
    /**
     * Update inventory cost and value
     */
    @Transactional
    @CacheEvict(value = {"inventory", "inventoryStats"}, allEntries = true)
    public void updateCostAndValue(Long inventoryId, BigDecimal costPerUnit, BigDecimal valuePerUnit) {
        log.debug("Updating cost and value for inventory: {}", inventoryId);
        
        Inventory inventory = getInventory(inventoryId);
        validateInventoryOwnership(inventory);
        
        if (costPerUnit.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Cost per unit must be greater than zero");
        }
        
        if (valuePerUnit.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Value per unit must be greater than zero");
        }
        
        boolean updated = inventoryRepository.updateCostAndValue(inventoryId, costPerUnit, 
            valuePerUnit, getCurrentUsername());
        
        if (updated) {
            log.info("Updated cost and value for inventory: {}", inventoryId);
        } else {
            throw new ValidationException("Failed to update cost and value");
        }
    }
    
    /**
     * Update stock count date
     */
    @Transactional
    @CacheEvict(value = "inventory", key = "#inventoryId")
    public void updateStockCountDate(Long inventoryId) {
        log.debug("Updating stock count date for inventory: {}", inventoryId);
        
        Inventory inventory = getInventory(inventoryId);
        validateInventoryOwnership(inventory);
        
        // Update to current date (0 days from now)
        boolean updated = inventoryRepository.updateStockCountDate(inventoryId, 0, getCurrentUsername());
        
        if (updated) {
            // Publish stock count event
            Map<String, Object> eventData = Map.of(
                "inventoryId", inventoryId,
                "productId", inventory.getProductId(),
                "countedBy", getCurrentUsername(),
                "timestamp", LocalDateTime.now()
            );
            kafkaTemplate.send("inventory.stock.counted", eventData);
            
            log.info("Updated stock count date for inventory: {}", inventoryId);
        } else {
            throw new ValidationException("Failed to update stock count date");
        }
    }
    
    /**
     * Get inventory statistics for producer
     */
    @Cacheable(value = "inventoryStats", key = "#producerId")
    public Map<String, Object> getInventoryStats(Long producerId) {
        log.debug("Getting inventory statistics for producer: {}", producerId);
        
        validateProducerAccess(producerId);
        return inventoryRepository.getProducerInventoryStats(producerId);
    }
    
    /**
     * Get inventory value by date range
     */
    public List<Map<String, Object>> getInventoryValueByDateRange(Long producerId, 
                                                                 String startDate, String endDate) {
        log.debug("Getting inventory value for producer: {} from {} to {}", 
            producerId, startDate, endDate);
        
        validateProducerAccess(producerId);
        return inventoryRepository.getInventoryValueByDateRange(producerId, startDate, endDate);
    }
    
    /**
     * Get inventory movement history
     */
    @Cacheable(value = "inventoryHistory", key = "#inventoryId + ':' + #days")
    public List<Map<String, Object>> getInventoryMovementHistory(Long inventoryId, int days) {
        log.debug("Getting inventory movement history for: {} over {} days", inventoryId, days);
        
        Inventory inventory = getInventory(inventoryId);
        validateInventoryAccess(inventory);
        
        return inventoryRepository.getInventoryMovementHistory(inventoryId, days);
    }
    
    /**
     * Delete inventory (soft delete)
     */
    @Transactional
    @CacheEvict(value = {"inventory", "inventoryStats"}, allEntries = true)
    public void deleteInventory(Long inventoryId) {
        log.debug("Deleting inventory: {}", inventoryId);
        
        Inventory inventory = getInventory(inventoryId);
        validateInventoryOwnership(inventory);
        
        // Validate deletion is allowed
        validateInventoryDeletion(inventory);
        
        boolean deleted = inventoryRepository.deleteInventory(inventoryId, 
            inventory.getProducerId(), getCurrentUsername());
        
        if (deleted) {
            // Publish inventory deleted event
            publishInventoryEvent("inventory.deleted", inventory);
            
            log.info("Deleted inventory: {} for product: {}", inventoryId, inventory.getProductId());
        } else {
            throw new ValidationException("Failed to delete inventory");
        }
    }
    
    // Private helper methods
    
    private void validateInventoryParameters(BigDecimal initialQuantity, BigDecimal costPerUnit,
                                           BigDecimal minStockLevel, BigDecimal maxStockLevel,
                                           BigDecimal reorderQuantity) {
        validateQuantity(initialQuantity, "Initial quantity");
        
        if (costPerUnit.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Cost per unit must be greater than zero");
        }
        
        validateStockLevels(minStockLevel, maxStockLevel, reorderQuantity);
    }
    
    private void validateStockLevels(BigDecimal minLevel, BigDecimal maxLevel, BigDecimal reorderQty) {
        if (minLevel != null && minLevel.compareTo(BigDecimal.ZERO) < 0) {
            throw new ValidationException("Minimum stock level cannot be negative");
        }
        
        if (maxLevel != null && maxLevel.compareTo(BigDecimal.ZERO) < 0) {
            throw new ValidationException("Maximum stock level cannot be negative");
        }
        
        if (minLevel != null && maxLevel != null && maxLevel.compareTo(minLevel) < 0) {
            throw new ValidationException("Maximum stock level cannot be less than minimum");
        }
        
        if (reorderQty != null && reorderQty.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Reorder quantity must be greater than zero");
        }
    }
    
    private void validateQuantity(BigDecimal quantity, String fieldName) {
        if (quantity == null || quantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException(fieldName + " must be greater than zero");
        }
    }
    
    private void validateProductOwnership(Product product) {
        Long currentProducerId = getCurrentProducerId();
        if (!product.getProducerId().equals(currentProducerId)) {
            throw new UnauthorizedAccessException("Access denied for product: " + product.getId());
        }
    }
    
    private void validateInventoryOwnership(Inventory inventory) {
        Long currentProducerId = getCurrentProducerId();
        if (!inventory.getProducerId().equals(currentProducerId)) {
            throw new UnauthorizedAccessException("Access denied for inventory: " + inventory.getId());
        }
    }
    
    private void validateInventoryAccess(Inventory inventory) {
        // For now, only validate ownership. In future, could add role-based access
        validateInventoryOwnership(inventory);
    }
    
    private void validateProducerAccess(Long producerId) {
        Long currentProducerId = getCurrentProducerId();
        if (!currentProducerId.equals(producerId)) {
            throw new UnauthorizedAccessException("Access denied for producer: " + producerId);
        }
    }
    
    private void validateInventoryDeletion(Inventory inventory) {
        if (inventory.getReservedQuantity().compareTo(BigDecimal.ZERO) > 0) {
            throw new ValidationException("Cannot delete inventory with reserved stock");
        }
        
        if (inventory.getTotalQuantity().compareTo(BigDecimal.ZERO) > 0) {
            throw new ValidationException("Cannot delete inventory with remaining stock. " +
                "Remove all stock before deletion.");
        }
    }
    
    private void publishInventoryEvent(String eventType, Inventory inventory) {
        Map<String, Object> eventData = Map.of(
            "eventType", eventType,
            "inventoryId", inventory.getId(),
            "productId", inventory.getProductId(),
            "producerId", inventory.getProducerId(),
            "status", inventory.getStatus(),
            "timestamp", LocalDateTime.now(),
            "updatedBy", getCurrentUsername()
        );
        
        kafkaTemplate.send("inventory.events", eventData);
    }
    
    private Long getCurrentProducerId() {
        // Extract producer ID from security context
        // This would be populated by your JWT token
        return 1L; // Placeholder - implement based on your security setup
    }
    
    private String getCurrentUsername() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
}