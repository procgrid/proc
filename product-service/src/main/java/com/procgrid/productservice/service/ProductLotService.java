package com.procgrid.productservice.service;

import com.procgrid.productservice.exception.ProductLotNotFoundException;
import com.procgrid.productservice.exception.UnauthorizedAccessException;
import com.procgrid.productservice.exception.ValidationException;
import com.procgrid.productservice.exception.InsufficientStockException;
import com.procgrid.common.exception.EntityNotFoundException;
import com.procgrid.productservice.model.ProductLot;
import com.procgrid.productservice.model.QualityGrade;
import com.procgrid.productservice.model.Inventory;
import com.procgrid.productservice.repository.ProductLotRepository;
import com.procgrid.productservice.repository.InventoryRepository;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Optional;

/**
 * Service layer for ProductLot operations
 * Provides business logic for lot tracking, quality management, and traceability
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductLotService {
    
    private final ProductLotRepository productLotRepository;
    private final InventoryRepository inventoryRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    
    /**
     * Create new product lot
     */
    @Transactional
    /**
     * Create product lot (simplified version for controller)
     */
    public ProductLot createProductLot(Long productId, String lotNumber, BigDecimal totalQuantity,
                                     LocalDate productionDate, LocalDate expiryDate, 
                                     LocalDate harvestDate, String fieldLocation,
                                     String growingConditions, String processingMethod,
                                     String qualityGrade) {
        log.debug("Creating product lot: {} for product: {}", lotNumber, productId);
        
        // For this simplified version, we'll create a basic lot with the provided parameters
        // This would need to be integrated with your inventory system properly
        
        ProductLot productLot = ProductLot.builder()
            .productId(productId)
            .producerId(getCurrentProducerId())
            .lotNumber(lotNumber)
            .totalQuantity(totalQuantity)
            .quantity(totalQuantity)
            .availableQuantity(totalQuantity)
            .reservedQuantity(BigDecimal.ZERO)
            .damagedQuantity(BigDecimal.ZERO)
            .harvestDate(harvestDate.atStartOfDay())
            .expiryDate(expiryDate.atStartOfDay())
            .productionDate(productionDate.atStartOfDay())
            .qualityGrade(qualityGrade)
            .processingMethod(processingMethod)
            .fieldLocation(fieldLocation)
            .notes(growingConditions)
            .status(ProductLot.LotStatus.AVAILABLE)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .createdBy(getCurrentUsername())
            .updatedBy(getCurrentUsername())
            .deleted(false)
            .build();

        ProductLot savedLot = productLotRepository.save(productLot);
        log.info("Created product lot: {} with ID: {}", lotNumber, savedLot.getId());
        
        return savedLot;
    }

    /**
     * Create product lot (full version with inventory)
     */
    public ProductLot createProductLot(Long inventoryId, String lotNumber, BigDecimal quantity,
                                     LocalDate harvestDate, LocalDate expiryDate, 
                                     QualityGrade qualityGrade, String qualityNotes,
                                     String processingMethod, String processingNotes,
                                     String storageConditions, String storageLocation,
                                     List<String> certifications, Map<String, Object> metadata) {
        log.debug("Creating product lot: {} for inventory: {}", lotNumber, inventoryId);
        
        // Validate inventory exists and belongs to current producer
        Inventory inventory = inventoryRepository.findById(inventoryId)
            .orElseThrow(() -> new ValidationException("Inventory not found: " + inventoryId));
        
        validateInventoryOwnership(inventory);
        
        // Validate lot parameters
        validateLotParameters(lotNumber, quantity, harvestDate, expiryDate);
        
        // Check lot number uniqueness
        if (productLotRepository.existsByLotNumber(lotNumber)) {
            throw new ValidationException("Lot number already exists: " + lotNumber);
        }
        
        // Create product lot
        ProductLot productLot = ProductLot.builder()
            .inventoryId(inventoryId)
            .productId(inventory.getProductId())
            .producerId(inventory.getProducerId())
            .lotNumber(lotNumber)
            .totalQuantity(quantity)
            .availableQuantity(quantity)
            .reservedQuantity(BigDecimal.ZERO)
            .soldQuantity(BigDecimal.ZERO)
            .damagedQuantity(BigDecimal.ZERO)
            .harvestDate(harvestDate.atStartOfDay())
            .expiryDate(expiryDate.atStartOfDay())
            .qualityGrade(qualityGrade != null ? qualityGrade.name() : QualityGrade.GRADE_A.name())
            .qualityNotes(qualityNotes)
            .processingMethod(processingMethod)
            .processingNotes(processingNotes)
            .storageConditions(storageConditions)
            .storageLocation(storageLocation)
            .certifications(certifications != null ? String.join(",", certifications) : null)
            .createdBy(getCurrentUsername())
            .createdAt(LocalDateTime.now())
            .build();
        
        ProductLot savedLot = productLotRepository.save(productLot);
        
        // Publish lot created event
        publishLotEvent("lot.created", savedLot);
        
        log.info("Created product lot with ID: {} for inventory: {}", 
            savedLot.getId(), inventoryId);
        
        return savedLot;
    }
    
    /**
     * Get product lot by ID
     */
    @Cacheable(value = "productLots", key = "#id")
    public ProductLot getProductLot(Long id) {
        log.debug("Getting product lot by ID: {}", id);
        
        ProductLot productLot = productLotRepository.findById(id)
            .orElseThrow(() -> new ProductLotNotFoundException("Product lot not found: " + id));
        
        validateLotAccess(productLot);
        return productLot;
    }
    
    /**
     * Get product lot by lot number
     */
    @Cacheable(value = "productLots", key = "'lot:' + #lotNumber")
    public ProductLot getProductLotByLotNumber(String lotNumber) {
        log.debug("Getting product lot by lot number: {}", lotNumber);
        
        ProductLot productLot = productLotRepository.findByLotNumber(lotNumber)
            .orElseThrow(() -> new ProductLotNotFoundException("Product lot not found with lot number: " + lotNumber));
        
        validateLotAccess(productLot);
        return productLot;
    }
    
    /**
     * Get product lots by inventory ID with pagination
     */
    public Page<ProductLot> getLotsByInventory(Long inventoryId, Pageable pageable) {
        log.debug("Getting product lots for inventory: {}, page: {}, size: {}", 
            inventoryId, pageable.getPageNumber(), pageable.getPageSize());
        
        // Validate inventory access
        Inventory inventory = inventoryRepository.findById(inventoryId)
            .orElseThrow(() -> new ValidationException("Inventory not found: " + inventoryId));
        validateInventoryOwnership(inventory);
        
        List<ProductLot> lots = productLotRepository.findByInventoryId(
            inventoryId, pageable.getPageNumber(), pageable.getPageSize());
        
        Long totalCount = productLotRepository.countByInventoryId(inventoryId);
        
        return new PageImpl<>(lots, pageable, totalCount);
    }
    
    /**
     * Get product lots by producer with pagination
     */
    public Page<ProductLot> getLotsByProducer(Long producerId, Pageable pageable) {
        log.debug("Getting product lots for producer: {}, page: {}, size: {}", 
            producerId, pageable.getPageNumber(), pageable.getPageSize());
        
        validateProducerAccess(producerId);
        
        List<ProductLot> lots = productLotRepository.findByProducerId(
            producerId, pageable.getPageNumber(), pageable.getPageSize());
        
        return new PageImpl<>(lots, pageable, lots.size());
    }
    
    /**
     * Get current producer's lots
     */
    public Page<ProductLot> getMyLots(Pageable pageable) {
        Long producerId = getCurrentProducerId();
        return getLotsByProducer(producerId, pageable);
    }
    
    /**
     * Get lots by status
     */
    public Page<ProductLot> getLotsByStatus(ProductLot.LotStatus status, Pageable pageable) {
        log.debug("Getting lots by status: {}, page: {}, size: {}", 
            status, pageable.getPageNumber(), pageable.getPageSize());
        
        List<ProductLot> lots = productLotRepository.findByStatus(
            status, pageable.getPageNumber(), pageable.getPageSize());
        
        // Filter by producer ownership
        Long currentProducerId = getCurrentProducerId();
        List<ProductLot> filteredLots = lots.stream()
            .filter(lot -> lot.getProducerId().equals(currentProducerId))
            .toList();
        
        return new PageImpl<>(filteredLots, pageable, filteredLots.size());
    }
    
    /**
     * Get expiring lots within specified days
     */
    public List<ProductLot> getExpiringLots(int days) {
        log.debug("Getting lots expiring within {} days", days);
        
        Long producerId = getCurrentProducerId();
        return productLotRepository.findExpiringLots(days, producerId);
    }
    
    /**
     * Get expired lots
     */
    public List<ProductLot> getExpiredLots() {
        log.debug("Getting expired lots");
        
        Long producerId = getCurrentProducerId();
        return productLotRepository.findExpiredLots(producerId);
    }
    
    /**
     * Get available lots for sale
     */
    public List<ProductLot> getAvailableLotsForSale(Long inventoryId) {
        log.debug("Getting available lots for sale for inventory: {}", inventoryId);
        
        // Validate inventory access
        Inventory inventory = inventoryRepository.findById(inventoryId)
            .orElseThrow(() -> new ValidationException("Inventory not found: " + inventoryId));
        validateInventoryOwnership(inventory);
        
        return productLotRepository.findAvailableLotsForSale(inventoryId);
    }
    
    /**
     * Get lots by quality grade
     */
    public Page<ProductLot> getLotsByQualityGrade(QualityGrade qualityGrade, Pageable pageable) {
        log.debug("Getting lots by quality grade: {}, page: {}, size: {}", 
            qualityGrade, pageable.getPageNumber(), pageable.getPageSize());
        
        List<ProductLot> lots = productLotRepository.findByQualityGrade(
            qualityGrade, pageable.getPageNumber(), pageable.getPageSize());
        
        // Filter by producer ownership
        Long currentProducerId = getCurrentProducerId();
        List<ProductLot> filteredLots = lots.stream()
            .filter(lot -> lot.getProducerId().equals(currentProducerId))
            .toList();
        
        return new PageImpl<>(filteredLots, pageable, filteredLots.size());
    }
    
    /**
     * Get lots by harvest date range
     */
    public Page<ProductLot> getLotsByHarvestDateRange(LocalDate startDate, LocalDate endDate, 
                                                     Pageable pageable) {
        log.debug("Getting lots by harvest date range from {} to {}", startDate, endDate);
        
        Long producerId = getCurrentProducerId();
        List<ProductLot> lots = productLotRepository.findByHarvestDateRange(
            producerId, startDate, endDate, pageable.getPageNumber(), pageable.getPageSize());
        
        return new PageImpl<>(lots, pageable, lots.size());
    }
    
    /**
     * Get lots by processing method
     */
    public Page<ProductLot> getLotsByProcessingMethod(String processingMethod, Pageable pageable) {
        log.debug("Getting lots by processing method: {}", processingMethod);
        
        List<ProductLot> lots = productLotRepository.findByProcessingMethod(
            processingMethod, pageable.getPageNumber(), pageable.getPageSize());
        
        // Filter by producer ownership
        Long currentProducerId = getCurrentProducerId();
        List<ProductLot> filteredLots = lots.stream()
            .filter(lot -> lot.getProducerId().equals(currentProducerId))
            .toList();
        
        return new PageImpl<>(filteredLots, pageable, filteredLots.size());
    }
    
    /**
     * Get lots by certification
     */
    public Page<ProductLot> getLotsByCertification(List<String> certifications, Pageable pageable) {
        log.debug("Getting lots by certifications: {}", certifications);
        
        List<ProductLot> lots = productLotRepository.findByCertification(
            certifications, pageable.getPageNumber(), pageable.getPageSize());
        
        // Filter by producer ownership
        Long currentProducerId = getCurrentProducerId();
        List<ProductLot> filteredLots = lots.stream()
            .filter(lot -> lot.getProducerId().equals(currentProducerId))
            .toList();
        
        return new PageImpl<>(filteredLots, pageable, filteredLots.size());
    }
    
    /**
     * Reserve quantity from lot
     */
    @Transactional
    @CacheEvict(value = {"productLots", "availableLots"}, allEntries = true)
    public void reserveQuantity(Long lotId, BigDecimal quantity, String orderId) {
        log.debug("Reserving quantity: {} from lot: {} for order: {}", quantity, lotId, orderId);
        
        validateQuantity(quantity, "Reserve quantity");
        
        ProductLot lot = getProductLot(lotId);
        validateLotOwnership(lot);
        
        // Check if enough stock is available
        if (lot.getAvailableQuantity().compareTo(quantity) < 0) {
            throw new InsufficientStockException("Insufficient lot quantity available. Required: " + 
                quantity + ", Available: " + lot.getAvailableQuantity());
        }
        
        // Check if lot is available for sale
        if (lot.getStatus() != ProductLot.LotStatus.AVAILABLE) {
            throw new ValidationException("Lot is not available for reservation. Status: " + lot.getStatus());
        }
        
        // Check if lot is not expired
        if (lot.isExpired()) {
            throw new ValidationException("Cannot reserve from expired lot");
        }
        
        boolean reserved = productLotRepository.reserveQuantity(lotId, quantity, getCurrentUsername());
        
        if (reserved) {
            // Publish reservation event
            Map<String, Object> eventData = Map.of(
                "lotId", lotId,
                "lotNumber", lot.getLotNumber(),
                "inventoryId", lot.getInventoryId(),
                "productId", lot.getProductId(),
                "quantity", quantity,
                "orderId", orderId,
                "reservedBy", getCurrentUsername()
            );
            kafkaTemplate.send("lot.quantity.reserved", eventData);
            
            log.info("Reserved {} quantity from lot: {} for order: {}", quantity, lotId, orderId);
        } else {
            throw new ValidationException("Failed to reserve quantity from lot");
        }
    }
    
    /**
     * Release reserved quantity
     */
    @Transactional
    @CacheEvict(value = {"productLots", "availableLots"}, allEntries = true)
    public void releaseReservedQuantity(Long lotId, BigDecimal quantity, String reason) {
        log.debug("Releasing reserved quantity: {} from lot: {}", quantity, lotId);
        
        validateQuantity(quantity, "Release quantity");
        
        ProductLot lot = getProductLot(lotId);
        validateLotOwnership(lot);
        
        // Check if enough reserved stock exists
        if (lot.getReservedQuantity().compareTo(quantity) < 0) {
            throw new ValidationException("Insufficient reserved quantity. Requested: " + 
                quantity + ", Reserved: " + lot.getReservedQuantity());
        }
        
        boolean released = productLotRepository.releaseReservedQuantity(lotId, quantity, getCurrentUsername());
        
        if (released) {
            // Publish release event
            Map<String, Object> eventData = Map.of(
                "lotId", lotId,
                "lotNumber", lot.getLotNumber(),
                "inventoryId", lot.getInventoryId(),
                "productId", lot.getProductId(),
                "quantity", quantity,
                "reason", reason != null ? reason : "Manual release",
                "releasedBy", getCurrentUsername()
            );
            kafkaTemplate.send("lot.quantity.released", eventData);
            
            log.info("Released {} reserved quantity from lot: {}", quantity, lotId);
        } else {
            throw new ValidationException("Failed to release reserved quantity");
        }
    }
    
    /**
     * Complete sale from lot
     */
    @Transactional
    @CacheEvict(value = {"productLots", "availableLots", "lotStats"}, allEntries = true)
    public void completeSale(Long lotId, BigDecimal quantity, String orderId) {
        log.debug("Completing sale: {} from lot: {} for order: {}", quantity, lotId, orderId);
        
        validateQuantity(quantity, "Sale quantity");
        
        ProductLot lot = getProductLot(lotId);
        
        // Check if enough reserved stock exists
        if (lot.getReservedQuantity().compareTo(quantity) < 0) {
            throw new ValidationException("Insufficient reserved quantity for sale. Required: " + 
                quantity + ", Reserved: " + lot.getReservedQuantity());
        }
        
        boolean completed = productLotRepository.completeSale(lotId, quantity, getCurrentUsername());
        
        if (completed) {
            // Publish sale completed event
            Map<String, Object> eventData = Map.of(
                "lotId", lotId,
                "lotNumber", lot.getLotNumber(),
                "inventoryId", lot.getInventoryId(),
                "productId", lot.getProductId(),
                "quantity", quantity,
                "orderId", orderId,
                "soldBy", getCurrentUsername()
            );
            kafkaTemplate.send("lot.sale.completed", eventData);
            
            log.info("Completed sale of {} from lot: {} for order: {}", quantity, lotId, orderId);
        } else {
            throw new ValidationException("Failed to complete sale from lot");
        }
    }
    
    /**
     * Update quality grade and notes
     */
    @Transactional
    @CacheEvict(value = "productLots", key = "#lotId")
    public void updateQuality(Long lotId, QualityGrade qualityGrade, String qualityNotes) {
        log.debug("Updating lot quality: {} to grade {}", lotId, qualityGrade);
        
        ProductLot lot = getProductLot(lotId);
        validateLotOwnership(lot);
        
        boolean updated = productLotRepository.updateQuality(lotId, qualityGrade, qualityNotes, getCurrentUsername());
        
        if (updated) {
            // Publish quality updated event
            Map<String, Object> eventData = Map.of(
                "lotId", lotId,
                "lotNumber", lot.getLotNumber(),
                "oldGrade", lot.getQualityGrade(),
                "newGrade", qualityGrade,
                "qualityNotes", qualityNotes,
                "updatedBy", getCurrentUsername()
            );
            kafkaTemplate.send("lot.quality.updated", eventData);
            
            log.info("Updated lot {} quality to grade {}", lotId, qualityGrade);
        } else {
            throw new ValidationException("Failed to update lot quality");
        }
    }
    
    /**
     * Update expiry date
     */
    @Transactional
    @CacheEvict(value = {"productLots", "expiringLots"}, allEntries = true)
    public void updateExpiryDate(Long lotId, LocalDate expiryDate) {
        log.debug("Updating lot expiry date: {} to {}", lotId, expiryDate);
        
        ProductLot lot = getProductLot(lotId);
        validateLotOwnership(lot);
        
        if (expiryDate.isBefore(LocalDate.now())) {
            throw new ValidationException("Expiry date cannot be in the past");
        }
        
        if (lot.getHarvestDate() != null && expiryDate.isBefore(lot.getHarvestDate().toLocalDate())) {
            throw new ValidationException("Expiry date cannot be before harvest date");
        }
        
        boolean updated = productLotRepository.updateExpiryDate(lotId, expiryDate, getCurrentUsername());
        
        if (updated) {
            // Publish expiry updated event
            Map<String, Object> eventData = Map.of(
                "lotId", lotId,
                "lotNumber", lot.getLotNumber(),
                "oldExpiryDate", lot.getExpiryDate(),
                "newExpiryDate", expiryDate,
                "updatedBy", getCurrentUsername()
            );
            kafkaTemplate.send("lot.expiry.updated", eventData);
            
            log.info("Updated lot {} expiry date to {}", lotId, expiryDate);
        } else {
            throw new ValidationException("Failed to update expiry date");
        }
    }
    
    /**
     * Update storage conditions
     */
    @Transactional
    @CacheEvict(value = "productLots", key = "#lotId")
    public void updateStorageConditions(Long lotId, String storageConditions, String storageLocation) {
        log.debug("Updating storage conditions for lot: {}", lotId);
        
        ProductLot lot = getProductLot(lotId);
        validateLotOwnership(lot);
        
        boolean updated = productLotRepository.updateStorageConditions(lotId, storageConditions, 
            storageLocation, getCurrentUsername());
        
        if (updated) {
            log.info("Updated storage conditions for lot: {}", lotId);
        } else {
            throw new ValidationException("Failed to update storage conditions");
        }
    }
    
    /**
     * Update processing information
     */
    @Transactional
    @CacheEvict(value = "productLots", key = "#lotId")
    public void updateProcessing(Long lotId, String processingMethod, String processingNotes) {
        log.debug("Updating processing information for lot: {}", lotId);
        
        ProductLot lot = getProductLot(lotId);
        validateLotOwnership(lot);
        
        boolean updated = productLotRepository.updateProcessing(lotId, processingMethod, 
            processingNotes, getCurrentUsername());
        
        if (updated) {
            log.info("Updated processing information for lot: {}", lotId);
        } else {
            throw new ValidationException("Failed to update processing information");
        }
    }
    
    /**
     * Mark lot as damaged
     */
    @Transactional
    @CacheEvict(value = {"productLots", "availableLots"}, allEntries = true)
    public void markAsDamaged(Long lotId, BigDecimal damagedQuantity, String damageReason) {
        log.debug("Marking lot as damaged: {} with quantity: {}", lotId, damagedQuantity);
        
        validateQuantity(damagedQuantity, "Damaged quantity");
        
        ProductLot lot = getProductLot(lotId);
        validateLotOwnership(lot);
        
        // Check if enough total stock exists
        BigDecimal totalAvailable = lot.getAvailableQuantity().add(lot.getReservedQuantity());
        if (totalAvailable.compareTo(damagedQuantity) < 0) {
            throw new ValidationException("Insufficient quantity to mark as damaged. Required: " + 
                damagedQuantity + ", Available: " + totalAvailable);
        }
        
        boolean marked = productLotRepository.markAsDamaged(lotId, damagedQuantity, 
            damageReason, getCurrentUsername());
        
        if (marked) {
            // Publish damaged event
            Map<String, Object> eventData = Map.of(
                "lotId", lotId,
                "lotNumber", lot.getLotNumber(),
                "inventoryId", lot.getInventoryId(),
                "productId", lot.getProductId(),
                "damagedQuantity", damagedQuantity,
                "damageReason", damageReason,
                "reportedBy", getCurrentUsername()
            );
            kafkaTemplate.send("lot.damaged", eventData);
            
            log.info("Marked lot {} as damaged with quantity: {}", lotId, damagedQuantity);
        } else {
            throw new ValidationException("Failed to mark lot as damaged");
        }
    }
    
    /**
     * Get lot statistics for producer
     */
    @Cacheable(value = "lotStats", key = "#producerId")
    public Map<String, Object> getLotStats(Long producerId) {
        log.debug("Getting lot statistics for producer: {}", producerId);
        
        validateProducerAccess(producerId);
        return productLotRepository.getProducerLotStats(producerId);
    }
    
    /**
     * Get lot performance by date range
     */
    public List<Map<String, Object>> getLotPerformanceByDateRange(Long producerId, 
                                                                 String startDate, String endDate) {
        log.debug("Getting lot performance for producer: {} from {} to {}", 
            producerId, startDate, endDate);
        
        validateProducerAccess(producerId);
        return productLotRepository.getLotPerformanceByDateRange(producerId, startDate, endDate);
    }
    
    /**
     * Get quality grade distribution
     */
    @Cacheable(value = "qualityStats", key = "#producerId")
    public List<Map<String, Object>> getQualityGradeDistribution(Long producerId) {
        log.debug("Getting quality grade distribution for producer: {}", producerId);
        
        validateProducerAccess(producerId);
        return productLotRepository.getQualityGradeDistribution(producerId);
    }
    
    /**
     * Get expiry analysis
     */
    public Map<String, Object> getExpiryAnalysis(int days) {
        log.debug("Getting expiry analysis over {} days", days);
        
        Long producerId = getCurrentProducerId();
        return productLotRepository.getExpiryAnalysis(producerId, days);
    }
    
    /**
     * Get lot traceability information
     */
    @Cacheable(value = "lotTraceability", key = "#lotNumber")
    public Map<String, Object> getLotTraceability(String lotNumber) {
        log.debug("Getting traceability information for lot: {}", lotNumber);
        
        // Verify lot belongs to current producer
        ProductLot lot = getProductLotByLotNumber(lotNumber);
        validateLotOwnership(lot);
        
        return productLotRepository.getLotTraceability(lotNumber);
    }
    
    /**
     * Get lot movement history
     */
    @Cacheable(value = "lotHistory", key = "#lotId + ':' + #days")
    public List<Map<String, Object>> getLotMovementHistory(Long lotId, int days) {
        log.debug("Getting lot movement history for: {} over {} days", lotId, days);
        
        ProductLot lot = getProductLot(lotId);
        validateLotAccess(lot);
        
        return productLotRepository.getLotMovementHistory(lotId, days);
    }
    
    /**
     * Delete product lot (soft delete)
     */
    @Transactional
    @CacheEvict(value = {"productLots", "expiringLots", "lotStats"}, allEntries = true)
    public void deleteProductLot(Long lotId) {
        log.debug("Deleting product lot: {}", lotId);
        
        ProductLot lot = getProductLot(lotId);
        validateLotOwnership(lot);
        
        // Validate deletion is allowed
        validateLotDeletion(lot);
        
        boolean deleted = productLotRepository.deleteProductLot(lotId, lot.getProducerId(), 
            getCurrentUsername());
        
        if (deleted) {
            // Publish lot deleted event
            publishLotEvent("lot.deleted", lot);
            
            log.info("Deleted product lot: {} with lot number: {}", lotId, lot.getLotNumber());
        } else {
            throw new ValidationException("Failed to delete product lot");
        }
    }
    
    // Private helper methods
    
    private void validateLotParameters(String lotNumber, BigDecimal quantity, 
                                     LocalDate harvestDate, LocalDate expiryDate) {
        if (lotNumber == null || lotNumber.trim().isEmpty()) {
            throw new ValidationException("Lot number is required");
        }
        
        if (lotNumber.length() > 50) {
            throw new ValidationException("Lot number cannot exceed 50 characters");
        }
        
        validateQuantity(quantity, "Lot quantity");
        
        if (harvestDate != null && harvestDate.isAfter(LocalDate.now())) {
            throw new ValidationException("Harvest date cannot be in the future");
        }
        
        if (expiryDate != null && expiryDate.isBefore(LocalDate.now())) {
            throw new ValidationException("Expiry date cannot be in the past");
        }
        
        if (harvestDate != null && expiryDate != null && expiryDate.isBefore(harvestDate)) {
            throw new ValidationException("Expiry date cannot be before harvest date");
        }
    }
    
    private void validateQuantity(BigDecimal quantity, String fieldName) {
        if (quantity == null || quantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException(fieldName + " must be greater than zero");
        }
    }
    
    private void validateInventoryOwnership(Inventory inventory) {
        Long currentProducerId = getCurrentProducerId();
        if (!inventory.getProducerId().equals(currentProducerId)) {
            throw new UnauthorizedAccessException("Access denied for inventory: " + inventory.getId());
        }
    }
    
    private void validateLotOwnership(ProductLot lot) {
        Long currentProducerId = getCurrentProducerId();
        if (!lot.getProducerId().equals(currentProducerId)) {
            throw new UnauthorizedAccessException("Access denied for lot: " + lot.getId());
        }
    }
    
    private void validateLotAccess(ProductLot lot) {
        // For now, only validate ownership. In future, could add role-based access
        validateLotOwnership(lot);
    }
    
    private void validateProducerAccess(Long producerId) {
        Long currentProducerId = getCurrentProducerId();
        if (!currentProducerId.equals(producerId)) {
            throw new UnauthorizedAccessException("Access denied for producer: " + producerId);
        }
    }
    
    private void validateLotDeletion(ProductLot lot) {
        if (lot.getReservedQuantity().compareTo(BigDecimal.ZERO) > 0) {
            throw new ValidationException("Cannot delete lot with reserved stock");
        }
        
        if (lot.getSoldQuantity().compareTo(BigDecimal.ZERO) > 0) {
            throw new ValidationException("Cannot delete lot with sales history");
        }
    }
    
    private void publishLotEvent(String eventType, ProductLot lot) {
        Map<String, Object> eventData = Map.of(
            "eventType", eventType,
            "lotId", lot.getId(),
            "lotNumber", lot.getLotNumber(),
            "inventoryId", lot.getInventoryId(),
            "productId", lot.getProductId(),
            "producerId", lot.getProducerId(),
            "status", lot.getStatus(),
            "qualityGrade", lot.getQualityGrade(),
            "timestamp", LocalDateTime.now(),
            "updatedBy", getCurrentUsername()
        );
        
        kafkaTemplate.send("lot.events", eventData);
    }

    /**
     * Get product lots by product ID with pagination
     */
    public Page<ProductLot> getProductLotsByProductId(Long productId, Pageable pageable) {
        log.debug("Getting product lots for product: {} with pagination", productId);
        
        List<ProductLot> lots = productLotRepository.findByProductId(productId, 
            pageable.getPageNumber(), pageable.getPageSize());
        
        // Get total count
        Long total = productLotRepository.countByProductId(productId);
        
        return new PageImpl<>(lots, pageable, total);
    }

    /**
     * Get my product lots (current producer) with pagination
     */
    public Page<ProductLot> getMyProductLots(Pageable pageable) {
        Long producerId = getCurrentProducerId();
        log.debug("Getting lots for current producer: {} with pagination", producerId);
        
        List<ProductLot> lots = productLotRepository.findByProducerId(producerId, 
            pageable.getPageNumber(), pageable.getPageSize());
        
        // Get total count
        Long total = productLotRepository.countByProducerId(producerId);
        
        return new PageImpl<>(lots, pageable, total);
    }

    /**
     * Get product lots by producer ID with pagination
     */
    public Page<ProductLot> getProductLotsByProducerId(Long producerId, Pageable pageable) {
        log.debug("Getting product lots for producer: {} with pagination", producerId);
        
        List<ProductLot> lots = productLotRepository.findByProducerId(producerId, 
            pageable.getPageNumber(), pageable.getPageSize());
        
        // Get total count  
        Long total = productLotRepository.countByProducerId(producerId);
        
        return new PageImpl<>(lots, pageable, total);
    }

    /**
     * Get available product lots
     */
    public List<ProductLot> getAvailableProductLots(Long productId, Long producerId) {
        log.debug("Getting available lots for product: {} and producer: {}", productId, producerId);
        return productLotRepository.findAvailableByProductAndProducer(productId, producerId);
    }

    /**
     * Get sold out product lots
     */
    public List<ProductLot> getSoldOutProductLots(Long productId, Long producerId) {
        log.debug("Getting sold out lots for product: {} and producer: {}", productId, producerId);
        return productLotRepository.findSoldOutByProductAndProducer(productId, producerId);
    }

    /**
     * Get expiring product lots
     */
    public List<ProductLot> getExpiringProductLots(int days, Long producerId) {
        log.debug("Getting lots expiring in {} days for producer: {}", days, producerId);
        return productLotRepository.findExpiringLots(days, producerId);
    }

    /**
     * Get expired product lots
     */
    public List<ProductLot> getExpiredProductLots(Long producerId) {
        log.debug("Getting expired lots for producer: {}", producerId);
        return productLotRepository.findExpiredLots(producerId);
    }

    /**
     * Get product lots by quality grade
     */
    public List<ProductLot> getProductLotsByQualityGrade(String qualityGrade, Long productId, Long producerId) {
        log.debug("Getting lots with quality grade: {} for product: {} and producer: {}", qualityGrade, productId, producerId);
        return productLotRepository.findByQualityGradeAndProductAndProducer(qualityGrade, productId, producerId);
    }

    /**
     * Get product lots by production date range
     */
    public List<ProductLot> getProductLotsByProductionDateRange(LocalDate startDate, LocalDate endDate, Long productId, Long producerId) {
        log.debug("Getting lots by production date range: {} to {} for product: {} and producer: {}", startDate, endDate, productId, producerId);
        return productLotRepository.findByProductionDateRange(startDate, endDate, productId, producerId);
    }

    /**
     * Get product lots by harvest date range
     */
    public List<ProductLot> getProductLotsByHarvestDateRange(LocalDate startDate, LocalDate endDate, Long productId, Long producerId) {
        log.debug("Getting lots by harvest date range: {} to {} for product: {} and producer: {}", startDate, endDate, productId, producerId);
        return productLotRepository.findByHarvestDateRange(producerId, startDate, endDate, 0, 1000);
    }

    /**
     * Get product lots by field location
     */
    public List<ProductLot> getProductLotsByFieldLocation(String fieldLocation, Long producerId) {
        log.debug("Getting lots by field location: {} for producer: {}", fieldLocation, producerId);
        return productLotRepository.findByFieldLocationAndProducer(fieldLocation, producerId);
    }

    /**
     * Search product lots with multiple criteria
     */
    public Page<ProductLot> searchProductLots(String query, Long productId, Long producerId, 
                                            String qualityGrade, String status,
                                            LocalDate productionStartDate, LocalDate productionEndDate,
                                            LocalDate harvestStartDate, LocalDate harvestEndDate,
                                            BigDecimal minQuantity, Pageable pageable) {
        log.debug("Searching lots with query: {} for product: {} and producer: {}", query, productId, producerId);
        return productLotRepository.searchLots(query, productId, producerId, qualityGrade, status,
                productionStartDate, productionEndDate, harvestStartDate, harvestEndDate, 
                minQuantity, pageable);
    }

    /**
     * Update production information
     */
    public void updateProductionInfo(Long id, String fieldLocation, String harvestMethod, 
                                   String processingMethod, String qualityGrade, LocalDate harvestDate) {
        log.debug("Updating production info for lot: {}", id);
        ProductLot lot = productLotRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("ProductLot not found with id: " + id, "PRODUCT_LOT_NOT_FOUND"));
        
        // Update fields
        lot.setFieldLocation(fieldLocation);
        lot.setProcessingMethod(processingMethod);
        lot.setQualityGrade(qualityGrade);
        if (harvestDate != null) {
            lot.setHarvestDate(harvestDate.atStartOfDay());
        }
        lot.setUpdatedAt(LocalDateTime.now());
        lot.setUpdatedBy(getCurrentUsername());
        
        productLotRepository.update(lot);
        log.info("Updated production info for lot: {}", id);
    }

    /**
     * Mark lot as expired
     */
    public void markAsExpired(Long id) {
        log.debug("Marking lot as expired: {}", id);
        ProductLot lot = productLotRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("ProductLot not found with id: " + id, "PRODUCT_LOT_NOT_FOUND"));
        
        lot.setStatus(ProductLot.LotStatus.EXPIRED);
        lot.setUpdatedAt(LocalDateTime.now());
        lot.setUpdatedBy(getCurrentUsername());
        
        productLotRepository.update(lot);
        log.info("Marked lot {} as expired", id);
        
        // Send notification event
        publishLotStatusChangeEvent(lot, "LOT_EXPIRED", "Lot marked as expired");
    }
    
    private Long getCurrentProducerId() {
        // Extract producer ID from security context
        // This would be populated by your JWT token
        return 1L; // Placeholder - implement based on your security setup
    }
    
    private String getCurrentUsername() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    private void publishLotStatusChangeEvent(ProductLot lot, String eventType, String description) {
        log.debug("Publishing lot status change event: {} for lot: {}", eventType, lot.getId());
        // Create event object and publish to Kafka
        Map<String, Object> event = Map.of(
            "lotId", lot.getId(),
            "eventType", eventType,
            "description", description,
            "timestamp", LocalDateTime.now()
        );
        kafkaTemplate.send("lot-status-changes", event);
    }
}