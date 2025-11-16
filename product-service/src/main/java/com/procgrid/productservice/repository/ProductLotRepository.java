package com.procgrid.productservice.repository;

import com.procgrid.productservice.mapper.mybatis.ProductLotMyBatisMapper;
import com.procgrid.productservice.model.ProductLot;
import com.procgrid.productservice.model.QualityGrade;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Repository layer for ProductLot operations
 * Provides caching, transaction management, and lot tracking business logic
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class ProductLotRepository {
    
    private final ProductLotMyBatisMapper productLotMapper;
    
    /**
     * Create new product lot
     */
    @Transactional
    @CacheEvict(value = {"productLots", "inventoryLots", "expiringLots", "lotStats"}, allEntries = true)
    public ProductLot save(ProductLot productLot) {
        log.debug("Creating new product lot for inventory: {}", productLot.getInventoryId());
        
        // Calculate total quantity
        productLot.calculateTotalQuantity();
        
        // Set initial status based on expiry and quality
        updateLotStatus(productLot);
        
        productLotMapper.insertProductLot(productLot);
        log.info("Created product lot with ID: {} for inventory: {}", 
            productLot.getId(), productLot.getInventoryId());
        
        return productLot;
    }
    
    /**
     * Update existing product lot
     */
    @Transactional
    @CachePut(value = "productLots", key = "#productLot.id")
    @CacheEvict(value = {"inventoryLots", "expiringLots", "lotStats"}, allEntries = true)
    public ProductLot update(ProductLot productLot) {
        log.debug("Updating product lot: {}", productLot.getId());
        
        // Recalculate total quantity
        productLot.calculateTotalQuantity();
        
        // Update status based on expiry and quality
        updateLotStatus(productLot);
        
        productLotMapper.updateProductLot(productLot);
        log.info("Updated product lot: {} for inventory: {}", 
            productLot.getId(), productLot.getInventoryId());
        
        return productLot;
    }
    
    /**
     * Find product lot by ID with caching
     */
    @Cacheable(value = "productLots", key = "#id")
    public Optional<ProductLot> findById(Long id) {
        log.debug("Finding product lot by ID: {}", id);
        ProductLot productLot = productLotMapper.findById(id);
        return Optional.ofNullable(productLot);
    }
    
    /**
     * Find product lot by lot number
     */
    @Cacheable(value = "productLots", key = "'lot:' + #lotNumber")
    public Optional<ProductLot> findByLotNumber(String lotNumber) {
        log.debug("Finding product lot by lot number: {}", lotNumber);
        ProductLot productLot = productLotMapper.findByLotNumber(lotNumber);
        return Optional.ofNullable(productLot);
    }
    
    /**
     * Find product lots by inventory ID with pagination
     */
    @Cacheable(value = "inventoryLots", key = "#inventoryId + ':' + #page + ':' + #size")
    public List<ProductLot> findByInventoryId(Long inventoryId, int page, int size) {
        log.debug("Finding product lots for inventory: {}, page: {}, size: {}", 
            inventoryId, page, size);
        int offset = page * size;
        return productLotMapper.findByInventoryId(inventoryId, offset, size);
    }
    
    /**
     * Count product lots by inventory ID
     */
    @Cacheable(value = "inventoryLotCount", key = "#inventoryId")
    public Long countByInventoryId(Long inventoryId) {
        return productLotMapper.countByInventoryId(inventoryId);
    }
    
    /**
     * Find product lots by product ID
     */
    @Cacheable(value = "productLots", key = "'product:' + #productId + ':' + #page + ':' + #size")
    public List<ProductLot> findByProductId(Long productId, int page, int size) {
        log.debug("Finding product lots for product: {}, page: {}, size: {}", 
            productId, page, size);
        int offset = page * size;
        return productLotMapper.findByProductId(productId, offset, size);
    }
    
    /**
     * Find product lots by producer ID
     */
    @Cacheable(value = "producerLots", key = "#producerId + ':' + #page + ':' + #size")
    public List<ProductLot> findByProducerId(Long producerId, int page, int size) {
        log.debug("Finding product lots for producer: {}, page: {}, size: {}", 
            producerId, page, size);
        int offset = page * size;
        return productLotMapper.findByProducerId(producerId, offset, size);
    }
    
    /**
     * Find product lots by status with pagination
     */
    @Cacheable(value = "lotsByStatus", key = "#status + ':' + #page + ':' + #size")
    public List<ProductLot> findByStatus(ProductLot.LotStatus status, int page, int size) {
        log.debug("Finding product lots by status: {}, page: {}, size: {}", 
            status, page, size);
        int offset = page * size;
        return productLotMapper.findByStatus(status, offset, size);
    }
    
    /**
     * Find expiring lots within specified days
     */
    @Cacheable(value = "expiringLots", key = "#days + ':' + #producerId")
    public List<ProductLot> findExpiringLots(int days, Long producerId) {
        log.debug("Finding lots expiring within {} days for producer: {}", days, producerId);
        return productLotMapper.findExpiringLots(days, producerId);
    }
    
    /**
     * Find expired lots for producer
     */
    public List<ProductLot> findExpiredLots(Long producerId) {
        log.debug("Finding expired lots for producer: {}", producerId);
        return productLotMapper.findExpiredLots(producerId);
    }
    
    /**
     * Find available lots for sale
     */
    @Cacheable(value = "availableLots", key = "#inventoryId")
    public List<ProductLot> findAvailableLotsForSale(Long inventoryId) {
        log.debug("Finding available lots for sale for inventory: {}", inventoryId);
        return productLotMapper.findAvailableLotsForSale(inventoryId);
    }
    
    /**
     * Find lots by quality grade
     */
    @Cacheable(value = "lotsByQuality", key = "#qualityGrade + ':' + #page + ':' + #size")
    public List<ProductLot> findByQualityGrade(QualityGrade qualityGrade, int page, int size) {
        log.debug("Finding lots by quality grade: {}, page: {}, size: {}", 
            qualityGrade, page, size);
        int offset = page * size;
        return productLotMapper.findByQualityGrade(qualityGrade.name(), offset, size);
    }
    
    /**
     * Find lots by harvest date range
     */
    public List<ProductLot> findByHarvestDateRange(Long producerId, LocalDate startDate, 
                                                  LocalDate endDate, int page, int size) {
        log.debug("Finding lots by harvest date range for producer: {} from {} to {}", 
            producerId, startDate, endDate);
        int offset = page * size;
        return productLotMapper.findByHarvestDateRange(producerId, startDate, endDate, offset, size);
    }
    
    /**
     * Find lots by processing method
     */
    public List<ProductLot> findByProcessingMethod(String processingMethod, int page, int size) {
        log.debug("Finding lots by processing method: {}, page: {}, size: {}", 
            processingMethod, page, size);
        int offset = page * size;
        return productLotMapper.findByProcessingMethod(processingMethod, offset, size);
    }
    
    /**
     * Update available quantity
     */
    @Transactional
    @CacheEvict(value = {"productLots", "inventoryLots", "availableLots"}, allEntries = true)
    public boolean updateAvailableQuantity(Long id, BigDecimal quantity, String updatedBy) {
        log.debug("Updating lot available quantity: {} to {}", id, quantity);
        int rows = productLotMapper.updateAvailableQuantity(id, quantity, updatedBy);
        
        if (rows > 0) {
            log.info("Updated lot {} available quantity to {}", id, quantity);
            
            // Update status based on quantity
            Optional<ProductLot> lotOpt = findById(id);
            lotOpt.ifPresent(lot -> {
                updateLotStatus(lot);
                productLotMapper.updateStatus(id, lot.getStatus(), updatedBy);
            });
            
            return true;
        }
        
        return false;
    }
    
    /**
     * Reserve quantity from lot
     */
    @Transactional
    @CacheEvict(value = {"productLots", "inventoryLots", "availableLots"}, allEntries = true)
    public boolean reserveQuantity(Long id, BigDecimal quantity, String updatedBy) {
        log.debug("Reserving quantity: {} from lot: {}", quantity, id);
        int rows = productLotMapper.reserveQuantity(id, quantity, updatedBy);
        
        if (rows > 0) {
            log.info("Reserved {} quantity from lot: {}", quantity, id);
            
            // Update status if needed
            Optional<ProductLot> lotOpt = findById(id);
            lotOpt.ifPresent(lot -> {
                updateLotStatus(lot);
                productLotMapper.updateStatus(id, lot.getStatus(), updatedBy);
            });
            
            return true;
        }
        
        log.warn("Failed to reserve {} quantity from lot: {}", quantity, id);
        return false;
    }
    
    /**
     * Release reserved quantity back to available
     */
    @Transactional
    @CacheEvict(value = {"productLots", "inventoryLots", "availableLots"}, allEntries = true)
    public boolean releaseReservedQuantity(Long id, BigDecimal quantity, String updatedBy) {
        log.debug("Releasing reserved quantity: {} from lot: {}", quantity, id);
        int rows = productLotMapper.releaseReservedQuantity(id, quantity, updatedBy);
        
        if (rows > 0) {
            log.info("Released {} reserved quantity from lot: {}", quantity, id);
            
            // Update status if needed
            Optional<ProductLot> lotOpt = findById(id);
            lotOpt.ifPresent(lot -> {
                updateLotStatus(lot);
                productLotMapper.updateStatus(id, lot.getStatus(), updatedBy);
            });
            
            return true;
        }
        
        return false;
    }
    
    /**
     * Complete sale from lot
     */
    @Transactional
    @CacheEvict(value = {"productLots", "inventoryLots", "availableLots", "lotStats"}, allEntries = true)
    public boolean completeSale(Long id, BigDecimal quantity, String updatedBy) {
        log.debug("Completing sale: {} from lot: {}", quantity, id);
        int rows = productLotMapper.completeSale(id, quantity, updatedBy);
        
        if (rows > 0) {
            log.info("Completed sale of {} from lot: {}", quantity, id);
            
            // Update status
            Optional<ProductLot> lotOpt = findById(id);
            lotOpt.ifPresent(lot -> {
                updateLotStatus(lot);
                productLotMapper.updateStatus(id, lot.getStatus(), updatedBy);
            });
            
            return true;
        }
        
        return false;
    }
    
    /**
     * Update lot status
     */
    @Transactional
    @CacheEvict(value = "productLots", key = "#id")
    public boolean updateStatus(Long id, ProductLot.LotStatus status, String updatedBy) {
        log.debug("Updating lot status: {} to {}", id, status);
        int rows = productLotMapper.updateStatus(id, status, updatedBy);
        
        if (rows > 0) {
            log.info("Updated lot {} status to {}", id, status);
            return true;
        }
        
        return false;
    }
    
    /**
     * Update quality grade and notes
     */
    @Transactional
    @CacheEvict(value = "productLots", key = "#id")
    public boolean updateQuality(Long id, QualityGrade qualityGrade, 
                                String qualityNotes, String updatedBy) {
        log.debug("Updating lot quality: {} to grade {}", id, qualityGrade);
        int rows = productLotMapper.updateQuality(id, qualityGrade.name(), qualityNotes, updatedBy);
        
        if (rows > 0) {
            log.info("Updated lot {} quality to grade {}", id, qualityGrade);
            
            // Update status based on quality
            Optional<ProductLot> lotOpt = findById(id);
            lotOpt.ifPresent(lot -> {
                updateLotStatus(lot);
                productLotMapper.updateStatus(id, lot.getStatus(), updatedBy);
            });
            
            return true;
        }
        
        return false;
    }
    
    /**
     * Update expiry date
     */
    @Transactional
    @CacheEvict(value = {"productLots", "expiringLots"}, allEntries = true)
    public boolean updateExpiryDate(Long id, LocalDate expiryDate, String updatedBy) {
        log.debug("Updating lot expiry date: {} to {}", id, expiryDate);
        int rows = productLotMapper.updateExpiryDate(id, expiryDate, updatedBy);
        
        if (rows > 0) {
            log.info("Updated lot {} expiry date to {}", id, expiryDate);
            
            // Update status based on expiry
            Optional<ProductLot> lotOpt = findById(id);
            lotOpt.ifPresent(lot -> {
                updateLotStatus(lot);
                productLotMapper.updateStatus(id, lot.getStatus(), updatedBy);
            });
            
            return true;
        }
        
        return false;
    }
    
    /**
     * Update storage conditions
     */
    @Transactional
    @CacheEvict(value = "productLots", key = "#id")
    public boolean updateStorageConditions(Long id, String storageConditions, 
                                         String storageLocation, String updatedBy) {
        log.debug("Updating storage conditions for lot: {}", id);
        int rows = productLotMapper.updateStorageConditions(id, storageLocation, 
            storageConditions, null, updatedBy); // Pass null for humidity requirements
        
        if (rows > 0) {
            log.info("Updated storage conditions for lot: {}", id);
            return true;
        }
        
        return false;
    }
    
    /**
     * Update processing method and notes
     */
    @Transactional
    @CacheEvict(value = "productLots", key = "#id")
    public boolean updateProcessing(Long id, String processingMethod, 
                                  String processingNotes, String updatedBy) {
        log.debug("Updating processing information for lot: {}", id);
        int rows = productLotMapper.updateProcessing(id, processingMethod, 
            processingNotes, updatedBy);
        
        if (rows > 0) {
            log.info("Updated processing information for lot: {}", id);
            return true;
        }
        
        return false;
    }
    
    /**
     * Mark lot as damaged
     */
    @Transactional
    @CacheEvict(value = {"productLots", "inventoryLots", "availableLots"}, allEntries = true)
    public boolean markAsDamaged(Long id, BigDecimal damagedQuantity, String damageReason, 
                                String updatedBy) {
        log.debug("Marking lot as damaged: {} with quantity: {}", id, damagedQuantity);
        int rows = productLotMapper.markAsDamaged(id, damagedQuantity, damageReason, updatedBy);
        
        if (rows > 0) {
            log.info("Marked lot {} as damaged with quantity: {}", id, damagedQuantity);
            
            // Update status
            Optional<ProductLot> lotOpt = findById(id);
            lotOpt.ifPresent(lot -> {
                updateLotStatus(lot);
                productLotMapper.updateStatus(id, lot.getStatus(), updatedBy);
            });
            
            return true;
        }
        
        return false;
    }
    
    /**
     * Get lot statistics for producer
     */
    @Cacheable(value = "lotStats", key = "#producerId")
    public Map<String, Object> getProducerLotStats(Long producerId) {
        log.debug("Getting lot statistics for producer: {}", producerId);
        return productLotMapper.getProducerLotStats(producerId);
    }
    
    /**
     * Get lot performance by date range
     */
    public List<Map<String, Object>> getLotPerformanceByDateRange(Long producerId, 
                                                                  String startDate, String endDate) {
        log.debug("Getting lot performance for producer: {} from {} to {}", 
            producerId, startDate, endDate);
        return productLotMapper.getLotPerformanceByDateRange(producerId, startDate, endDate);
    }
    
    /**
     * Get quality grade distribution
     */
    @Cacheable(value = "qualityStats", key = "#producerId")
    public List<Map<String, Object>> getQualityGradeDistribution(Long producerId) {
        log.debug("Getting quality grade distribution for producer: {}", producerId);
        return productLotMapper.getQualityGradeDistribution(producerId);
    }
    
    /**
     * Get expiry analysis
     */
    public Map<String, Object> getExpiryAnalysis(Long producerId, int days) {
        log.debug("Getting expiry analysis for producer: {} over {} days", producerId, days);
        return productLotMapper.getExpiryAnalysis(producerId, days);
    }
    
    /**
     * Delete product lot (soft delete)
     */
    @Transactional
    @CacheEvict(value = {"productLots", "inventoryLots", "expiringLots", "lotStats"}, allEntries = true)
    public boolean deleteProductLot(Long id, Long producerId, String updatedBy) {
        log.debug("Deleting product lot: {} for producer: {}", id, producerId);
        int rows = productLotMapper.deleteProductLot(id, producerId, updatedBy);
        
        if (rows > 0) {
            log.info("Deleted product lot: {} for producer: {}", id, producerId);
            return true;
        }
        
        return false;
    }
    
    /**
     * Check if lot number exists
     */
    public boolean existsByLotNumber(String lotNumber) {
        return productLotMapper.existsByLotNumber(lotNumber);
    }
    
    /**
     * Bulk update lot status
     */
    @Transactional
    @CacheEvict(value = {"productLots", "inventoryLots"}, allEntries = true)
    public int bulkUpdateStatus(List<Long> lotIds, ProductLot.LotStatus status, String updatedBy) {
        log.debug("Bulk updating lot status for {} items to {}", lotIds.size(), status);
        int rows = productLotMapper.bulkUpdateStatus(lotIds, status, updatedBy);
        log.info("Bulk updated {} lot items to status {}", rows, status);
        return rows;
    }
    
    /**
     * Get lot traceability information
     */
    @Cacheable(value = "lotTraceability", key = "#lotNumber")
    public Map<String, Object> getLotTraceability(String lotNumber) {
        log.debug("Getting traceability information for lot: {}", lotNumber);
        return productLotMapper.getLotTraceability(lotNumber);
    }
    
    /**
     * Find lots by certification
     */
    public List<ProductLot> findByCertification(List<String> certifications, int page, int size) {
        log.debug("Finding lots by certifications: {}, page: {}, size: {}", 
            certifications, page, size);
        int offset = page * size;
        return productLotMapper.findByCertification(certifications, offset, size);
    }
    
    /**
     * Get lot movement history
     */
    @Cacheable(value = "lotHistory", key = "#lotId + ':' + #days")
    public List<Map<String, Object>> getLotMovementHistory(Long lotId, int days) {
        log.debug("Getting lot movement history for: {} over {} days", lotId, days);
        return productLotMapper.getLotMovementHistory(lotId, days);
    }
    
    /**
     * Update lot status based on expiry date, quality, and quantities
     */
    private void updateLotStatus(ProductLot productLot) {
        if (productLot.isExpired()) {
            productLot.setStatus(ProductLot.LotStatus.EXPIRED);
        } else if (productLot.isExpiringSoon(7)) {
            productLot.setStatus(ProductLot.LotStatus.EXPIRING_SOON);
        } else if (productLot.getDamagedQuantity().compareTo(BigDecimal.ZERO) > 0) {
            productLot.setStatus(ProductLot.LotStatus.DAMAGED);
        } else if (QualityGrade.REJECT.name().equals(productLot.getQualityGrade())) {
            productLot.setStatus(ProductLot.LotStatus.QUALITY_ISSUE);
        } else if (productLot.getAvailableQuantity().compareTo(BigDecimal.ZERO) == 0) {
            productLot.setStatus(ProductLot.LotStatus.SOLD_OUT);
        } else {
            productLot.setStatus(ProductLot.LotStatus.AVAILABLE);
        }
    }

    /**
     * Find available lots by product and producer
     */
    public List<ProductLot> findAvailableByProductAndProducer(Long productId, Long producerId) {
        log.debug("Finding available lots for product: {} and producer: {}", productId, producerId);
        return productLotMapper.findAvailableByProductAndProducer(productId, producerId);
    }

    /**
     * Find sold out lots by product and producer
     */
    public List<ProductLot> findSoldOutByProductAndProducer(Long productId, Long producerId) {
        log.debug("Finding sold out lots for product: {} and producer: {}", productId, producerId);
        return productLotMapper.findSoldOutByProductAndProducer(productId, producerId);
    }

    /**
     * Find lots by quality grade, product and producer
     */
    public List<ProductLot> findByQualityGradeAndProductAndProducer(String qualityGrade, Long productId, Long producerId) {
        log.debug("Finding lots by quality grade: {} for product: {} and producer: {}", qualityGrade, productId, producerId);
        return productLotMapper.findByQualityGradeAndProductAndProducer(qualityGrade, productId, producerId);
    }

    /**
     * Find lots by production date range, product and producer
     */
    public List<ProductLot> findByProductionDateRange(LocalDate startDate, LocalDate endDate, Long productId, Long producerId) {
        log.debug("Finding lots by production date range: {} to {} for product: {} and producer: {}", 
            startDate, endDate, productId, producerId);
        return productLotMapper.findByProductionDateRange(productId, producerId, startDate, endDate);
    }

    /**
     * Find lots by field location and producer
     */
    public List<ProductLot> findByFieldLocationAndProducer(String fieldLocation, Long producerId) {
        log.debug("Finding lots by field location: {} for producer: {}", fieldLocation, producerId);
        return productLotMapper.findByFieldLocationAndProducer(fieldLocation, producerId);
    }

    /**
     * Search lots with comprehensive filters
     */
    public Page<ProductLot> searchLots(String searchTerm, Long productId, Long producerId, 
                                     String qualityGrade, String status, 
                                     LocalDate harvestStartDate, LocalDate harvestEndDate,
                                     LocalDate productionStartDate, LocalDate productionEndDate,
                                     BigDecimal minQuantity, Pageable pageable) {
        log.debug("Searching lots with comprehensive filters");
        
        List<ProductLot> lots = productLotMapper.searchLots(searchTerm, productId, producerId, 
            qualityGrade, status, harvestStartDate, harvestEndDate, 
            productionStartDate, productionEndDate, minQuantity, pageable);
        
        // For simplicity, return a page without total count for now
        return new PageImpl<>(lots, pageable, lots.size());
    }

    /**
     * Count lots by product ID
     */
    public Long countByProductId(Long productId) {
        log.debug("Counting lots for product: {}", productId);
        return productLotMapper.countByProductId(productId);
    }

    /**
     * Count lots by producer ID
     */
    public Long countByProducerId(Long producerId) {
        log.debug("Counting lots for producer: {}", producerId);
        return productLotMapper.countByProducerId(producerId);
    }
}