package com.procgrid.productservice.repository;

import com.procgrid.productservice.mapper.mybatis.InventoryMyBatisMapper;
import com.procgrid.productservice.model.Inventory;
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
 * Repository layer for Inventory operations
 * Provides caching, transaction management, and inventory business logic
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class InventoryRepository {
    
    private final InventoryMyBatisMapper inventoryMapper;
    
    /**
     * Create new inventory record
     */
    @Transactional
    @CacheEvict(value = {"inventory", "producerInventory", "inventoryStats"}, allEntries = true)
    public Inventory save(Inventory inventory) {
        log.debug("Creating new inventory for product: {}", inventory.getProductId());
        
        // Calculate total value
        inventory.calculateTotalValue();
        
        // Set initial status
        updateInventoryStatus(inventory);
        
        inventoryMapper.insertInventory(inventory);
        log.info("Created inventory with ID: {} for product: {}", inventory.getId(), inventory.getProductId());
        
        return inventory;
    }
    
    /**
     * Update existing inventory
     */
    @Transactional
    @CachePut(value = "inventory", key = "#inventory.id")
    @CacheEvict(value = {"producerInventory", "inventoryStats"}, allEntries = true)
    public Inventory update(Inventory inventory) {
        log.debug("Updating inventory: {}", inventory.getId());
        
        // Recalculate total value
        inventory.calculateTotalValue();
        
        // Update status based on quantities
        updateInventoryStatus(inventory);
        
        inventoryMapper.updateInventory(inventory);
        log.info("Updated inventory: {} for product: {}", inventory.getId(), inventory.getProductId());
        
        return inventory;
    }
    
    /**
     * Find inventory by ID with caching
     */
    @Cacheable(value = "inventory", key = "#id")
    public Optional<Inventory> findById(Long id) {
        log.debug("Finding inventory by ID: {}", id);
        Inventory inventory = inventoryMapper.findById(id);
        return Optional.ofNullable(inventory);
    }
    
    /**
     * Find inventory by product ID
     */
    @Cacheable(value = "inventory", key = "'product:' + #productId")
    public Optional<Inventory> findByProductId(Long productId) {
        log.debug("Finding inventory for product: {}", productId);
        Inventory inventory = inventoryMapper.findByProductId(productId);
        return Optional.ofNullable(inventory);
    }
    
    /**
     * Find inventory by producer ID with pagination
     */
    @Cacheable(value = "producerInventory", key = "#producerId + ':' + #page + ':' + #size")
    public List<Inventory> findByProducerId(Long producerId, int page, int size) {
        log.debug("Finding inventory for producer: {}, page: {}, size: {}", producerId, page, size);
        int offset = page * size;
        return inventoryMapper.findByProducerId(producerId, offset, size);
    }
    
    /**
     * Count inventory records by producer ID
     */
    @Cacheable(value = "producerInventoryCount", key = "#producerId")
    public Long countByProducerId(Long producerId) {
        return inventoryMapper.countByProducerId(producerId);
    }
    
    /**
     * Find low stock inventory for producer
     */
    public List<Inventory> findLowStockByProducerId(Long producerId) {
        log.debug("Finding low stock inventory for producer: {}", producerId);
        return inventoryMapper.findLowStockByProducerId(producerId);
    }
    
    /**
     * Find out of stock inventory for producer
     */
    public List<Inventory> findOutOfStockByProducerId(Long producerId) {
        log.debug("Finding out of stock inventory for producer: {}", producerId);
        return inventoryMapper.findOutOfStockByProducerId(producerId);
    }
    
    /**
     * Find overstocked inventory for producer
     */
    public List<Inventory> findOverstockedByProducerId(Long producerId) {
        log.debug("Finding overstocked inventory for producer: {}", producerId);
        return inventoryMapper.findOverstockedByProducerId(producerId);
    }
    
    /**
     * Find inventory by status with pagination
     */
    @Cacheable(value = "inventoryByStatus", key = "#status + ':' + #page + ':' + #size")
    public List<Inventory> findByStatus(Inventory.InventoryStatus status, int page, int size) {
        log.debug("Finding inventory by status: {}, page: {}, size: {}", status, page, size);
        int offset = page * size;
        return inventoryMapper.findByStatus(status, offset, size);
    }
    
    /**
     * Find inventory requiring stock count
     */
    public List<Inventory> findInventoryRequiringStockCount() {
        log.debug("Finding inventory requiring stock count");
        return inventoryMapper.findInventoryRequiringStockCount();
    }
    
    /**
     * Update available quantity
     */
    @Transactional
    @CacheEvict(value = {"inventory", "producerInventory"}, allEntries = true)
    public boolean updateAvailableQuantity(Long id, BigDecimal quantity, String updatedBy) {
        log.debug("Updating inventory available quantity: {} to {}", id, quantity);
        int rows = inventoryMapper.updateAvailableQuantity(id, quantity, updatedBy);
        
        if (rows > 0) {
            log.info("Updated inventory {} available quantity to {}", id, quantity);
            return true;
        }
        
        return false;
    }
    
    /**
     * Reserve quantity from inventory
     */
    @Transactional
    @CacheEvict(value = {"inventory", "producerInventory"}, allEntries = true)
    public boolean reserveQuantity(Long id, BigDecimal quantity, String updatedBy) {
        log.debug("Reserving quantity: {} from inventory: {}", quantity, id);
        int rows = inventoryMapper.reserveQuantity(id, quantity, updatedBy);
        
        if (rows > 0) {
            log.info("Reserved {} quantity from inventory: {}", quantity, id);
            
            // Update status if needed
            Optional<Inventory> inventoryOpt = findById(id);
            inventoryOpt.ifPresent(inventory -> {
                updateInventoryStatus(inventory);
                inventoryMapper.updateStatus(id, inventory.getStatus(), updatedBy);
            });
            
            return true;
        }
        
        log.warn("Failed to reserve {} quantity from inventory: {}", quantity, id);
        return false;
    }
    
    /**
     * Release reserved quantity back to available
     */
    @Transactional
    @CacheEvict(value = {"inventory", "producerInventory"}, allEntries = true)
    public boolean releaseReservedQuantity(Long id, BigDecimal quantity, String updatedBy) {
        log.debug("Releasing reserved quantity: {} from inventory: {}", quantity, id);
        int rows = inventoryMapper.releaseReservedQuantity(id, quantity, updatedBy);
        
        if (rows > 0) {
            log.info("Released {} reserved quantity from inventory: {}", quantity, id);
            
            // Update status if needed
            Optional<Inventory> inventoryOpt = findById(id);
            inventoryOpt.ifPresent(inventory -> {
                updateInventoryStatus(inventory);
                inventoryMapper.updateStatus(id, inventory.getStatus(), updatedBy);
            });
            
            return true;
        }
        
        return false;
    }
    
    /**
     * Complete sale (move from reserved to sold)
     */
    @Transactional
    @CacheEvict(value = {"inventory", "producerInventory", "inventoryStats"}, allEntries = true)
    public boolean completeSale(Long id, BigDecimal quantity, String updatedBy) {
        log.debug("Completing sale: {} from inventory: {}", quantity, id);
        int rows = inventoryMapper.completeSale(id, quantity, updatedBy);
        
        if (rows > 0) {
            log.info("Completed sale of {} from inventory: {}", quantity, id);
            
            // Update status
            Optional<Inventory> inventoryOpt = findById(id);
            inventoryOpt.ifPresent(inventory -> {
                updateInventoryStatus(inventory);
                inventoryMapper.updateStatus(id, inventory.getStatus(), updatedBy);
            });
            
            return true;
        }
        
        return false;
    }
    
    /**
     * Add stock to inventory
     */
    @Transactional
    @CacheEvict(value = {"inventory", "producerInventory", "inventoryStats"}, allEntries = true)
    public boolean addStock(Long id, BigDecimal quantity, String updatedBy) {
        log.debug("Adding stock: {} to inventory: {}", quantity, id);
        int rows = inventoryMapper.addStock(id, quantity, updatedBy);
        
        if (rows > 0) {
            log.info("Added {} stock to inventory: {}", quantity, id);
            
            // Update status
            Optional<Inventory> inventoryOpt = findById(id);
            inventoryOpt.ifPresent(inventory -> {
                updateInventoryStatus(inventory);
                inventoryMapper.updateStatus(id, inventory.getStatus(), updatedBy);
            });
            
            return true;
        }
        
        return false;
    }
    
    /**
     * Remove damaged stock
     */
    @Transactional
    @CacheEvict(value = {"inventory", "producerInventory", "inventoryStats"}, allEntries = true)
    public boolean removeDamagedStock(Long id, BigDecimal quantity, String updatedBy) {
        log.debug("Removing damaged stock: {} from inventory: {}", quantity, id);
        int rows = inventoryMapper.removeDamagedStock(id, quantity, updatedBy);
        
        if (rows > 0) {
            log.info("Removed {} damaged stock from inventory: {}", quantity, id);
            
            // Update status
            Optional<Inventory> inventoryOpt = findById(id);
            inventoryOpt.ifPresent(inventory -> {
                updateInventoryStatus(inventory);
                inventoryMapper.updateStatus(id, inventory.getStatus(), updatedBy);
            });
            
            return true;
        }
        
        return false;
    }
    
    /**
     * Update inventory status
     */
    @Transactional
    @CacheEvict(value = "inventory", key = "#id")
    public boolean updateStatus(Long id, Inventory.InventoryStatus status, String updatedBy) {
        log.debug("Updating inventory status: {} to {}", id, status);
        int rows = inventoryMapper.updateStatus(id, status, updatedBy);
        
        if (rows > 0) {
            log.info("Updated inventory {} status to {}", id, status);
            return true;
        }
        
        return false;
    }
    
    /**
     * Update stock levels
     */
    @Transactional
    @CacheEvict(value = "inventory", key = "#id")
    public boolean updateStockLevels(Long id, BigDecimal minLevel, BigDecimal maxLevel, 
                                   BigDecimal reorderQty, String updatedBy) {
        log.debug("Updating stock levels for inventory: {}", id);
        int rows = inventoryMapper.updateStockLevels(id, minLevel, maxLevel, reorderQty, updatedBy);
        
        if (rows > 0) {
            log.info("Updated stock levels for inventory: {}", id);
            return true;
        }
        
        return false;
    }
    
    /**
     * Update cost and value
     */
    @Transactional
    @CacheEvict(value = {"inventory", "inventoryStats"}, allEntries = true)
    public boolean updateCostAndValue(Long id, BigDecimal cost, BigDecimal value, String updatedBy) {
        log.debug("Updating cost and value for inventory: {}", id);
        int rows = inventoryMapper.updateCostAndValue(id, cost, value, updatedBy);
        
        if (rows > 0) {
            log.info("Updated cost and value for inventory: {}", id);
            return true;
        }
        
        return false;
    }
    
    /**
     * Update stock count date
     */
    @Transactional
    @CacheEvict(value = "inventory", key = "#id")
    public boolean updateStockCountDate(Long id, int days, String updatedBy) {
        log.debug("Updating stock count date for inventory: {}", id);
        int rows = inventoryMapper.updateStockCountDate(id, days, updatedBy);
        
        if (rows > 0) {
            log.info("Updated stock count date for inventory: {}", id);
            return true;
        }
        
        return false;
    }
    
    /**
     * Get inventory statistics for producer
     */
    @Cacheable(value = "inventoryStats", key = "#producerId")
    public Map<String, Object> getProducerInventoryStats(Long producerId) {
        log.debug("Getting inventory statistics for producer: {}", producerId);
        return inventoryMapper.getProducerInventoryStats(producerId);
    }
    
    /**
     * Get inventory value by date range
     */
    public List<Map<String, Object>> getInventoryValueByDateRange(Long producerId, String startDate, String endDate) {
        log.debug("Getting inventory value for producer: {} from {} to {}", producerId, startDate, endDate);
        return inventoryMapper.getInventoryValueByDateRange(producerId, startDate, endDate);
    }
    
    /**
     * Delete inventory (soft delete)
     */
    @Transactional
    @CacheEvict(value = {"inventory", "producerInventory", "inventoryStats"}, allEntries = true)
    public boolean deleteInventory(Long id, Long producerId, String updatedBy) {
        log.debug("Deleting inventory: {} for producer: {}", id, producerId);
        int rows = inventoryMapper.deleteInventory(id, producerId, updatedBy);
        
        if (rows > 0) {
            log.info("Deleted inventory: {} for producer: {}", id, producerId);
            return true;
        }
        
        return false;
    }
    
    /**
     * Check if inventory exists for product
     */
    public boolean existsByProductId(Long productId) {
        return inventoryMapper.existsByProductId(productId);
    }
    
    /**
     * Bulk update inventory status
     */
    @Transactional
    @CacheEvict(value = {"inventory", "producerInventory"}, allEntries = true)
    public int bulkUpdateStatus(List<Long> inventoryIds, Inventory.InventoryStatus status, String updatedBy) {
        log.debug("Bulk updating inventory status for {} items to {}", inventoryIds.size(), status);
        int rows = inventoryMapper.bulkUpdateStatus(inventoryIds, status, updatedBy);
        log.info("Bulk updated {} inventory items to status {}", rows, status);
        return rows;
    }
    
    /**
     * Get inventory movement history
     */
    @Cacheable(value = "inventoryHistory", key = "#inventoryId + ':' + #days")
    public List<Map<String, Object>> getInventoryMovementHistory(Long inventoryId, int days) {
        log.debug("Getting inventory movement history for: {} over {} days", inventoryId, days);
        return inventoryMapper.getInventoryMovementHistory(inventoryId, days);
    }
    
    /**
     * Update inventory status based on current quantities
     */
    private void updateInventoryStatus(Inventory inventory) {
        if (inventory.getAvailableQuantity().compareTo(BigDecimal.ZERO) == 0) {
            inventory.setStatus(Inventory.InventoryStatus.OUT_OF_STOCK);
        } else if (inventory.isLowStock()) {
            inventory.setStatus(Inventory.InventoryStatus.LOW_STOCK);
        } else if (inventory.isOverstocked()) {
            inventory.setStatus(Inventory.InventoryStatus.OVERSTOCK);
        } else {
            inventory.setStatus(Inventory.InventoryStatus.IN_STOCK);
        }
    }
}