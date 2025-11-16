package com.procgrid.productservice.mapper.mybatis;

import com.procgrid.productservice.model.ProductLot;
import org.apache.ibatis.annotations.*;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * MyBatis mapper interface for ProductLot operations
 */
@Mapper
public interface ProductLotMyBatisMapper {
    
    /**
     * Insert new product lot
     */
    @Insert({
        "INSERT INTO product_lots (",
        "product_id, lot_number, quantity, available_quantity, reserved_quantity,",
        "quantity_unit, production_date, harvest_date, expiry_date, storage_location,",
        "warehouse, quality_grade, quality_test_results, supplier_details, field_location,",
        "gps_coordinates, status, cost_price, selling_price, notes, certifications,",
        "temperature_requirements, humidity_requirements, created_at, updated_at,",
        "created_by, updated_by, deleted",
        ") VALUES (",
        "#{productId}, #{lotNumber}, #{quantity}, #{availableQuantity}, #{reservedQuantity},",
        "#{quantityUnit}, #{productionDate}, #{harvestDate}, #{expiryDate}, #{storageLocation},",
        "#{warehouse}, #{qualityGrade}, #{qualityTestResults}, #{supplierDetails}, #{fieldLocation},",
        "#{gpsCoordinates}, #{status}, #{costPrice}, #{sellingPrice}, #{notes}, #{certifications},",
        "#{temperatureRequirements}, #{humidityRequirements}, #{createdAt}, #{updatedAt},",
        "#{createdBy}, #{updatedBy}, #{deleted}",
        ")"
    })
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insertProductLot(ProductLot productLot);
    
    /**
     * Update existing product lot
     */
    @UpdateProvider(type = ProductLotSqlProvider.class, method = "updateProductLotSql")
    int updateProductLot(ProductLot productLot);
    
    /**
     * Find product lot by ID
     */
    @Select({
        "SELECT * FROM product_lots WHERE id = #{id} AND deleted = false"
    })
    ProductLot findById(Long id);
    
    /**
     * Find product lot by lot number
     */
    @Select({
        "SELECT * FROM product_lots WHERE lot_number = #{lotNumber} AND deleted = false"
    })
    ProductLot findByLotNumber(String lotNumber);
    
    /**
     * Find product lots by product ID
     */
    @Select({
        "SELECT * FROM product_lots WHERE product_id = #{productId} AND deleted = false",
        "ORDER BY production_date DESC, created_at DESC",
        "LIMIT #{limit} OFFSET #{offset}"
    })
    List<ProductLot> findByProductId(@Param("productId") Long productId,
                                    @Param("offset") int offset,
                                    @Param("limit") int limit);
    
    /**
     * Count product lots by product ID
     */
    @Select({
        "SELECT COUNT(*) FROM product_lots WHERE product_id = #{productId} AND deleted = false"
    })
    Long countByProductId(Long productId);
    
    /**
     * Find available lots for product
     */
    @Select({
        "SELECT * FROM product_lots",
        "WHERE product_id = #{productId} AND status = 'AVAILABLE'",
        "AND available_quantity > 0 AND deleted = false",
        "ORDER BY expiry_date ASC, production_date ASC"
    })
    List<ProductLot> findAvailableLotsByProductId(Long productId);
    
    /**
     * Find lots by status
     */
    @Select({
        "SELECT * FROM product_lots WHERE status = #{status} AND deleted = false",
        "ORDER BY expiry_date ASC",
        "LIMIT #{limit} OFFSET #{offset}"
    })
    List<ProductLot> findByStatus(@Param("status") ProductLot.LotStatus status,
                                 @Param("offset") int offset,
                                 @Param("limit") int limit);
    
    /**
     * Find lots expiring soon
     */
    @Select({
        "SELECT * FROM product_lots",
        "WHERE expiry_date IS NOT NULL",
        "AND expiry_date BETWEEN NOW() AND DATE_ADD(NOW(), INTERVAL #{days} DAY)",
        "AND status IN ('AVAILABLE', 'RESERVED')",
        "AND deleted = false",
        "ORDER BY expiry_date ASC"
    })
    List<ProductLot> findLotsExpiringSoon(int days);
    
    /**
     * Find expired lots
     */
    @SelectProvider(type = ProductLotSqlProvider.class, method = "findExpiredLotsSql")
    List<ProductLot> findExpiredLots(@Param("producerId") Long producerId);
    
    /**
     * Find lots by warehouse
     */
    @Select({
        "SELECT * FROM product_lots WHERE warehouse = #{warehouse} AND deleted = false",
        "ORDER BY storage_location ASC, created_at DESC",
        "LIMIT #{limit} OFFSET #{offset}"
    })
    List<ProductLot> findByWarehouse(@Param("warehouse") String warehouse,
                                    @Param("offset") int offset,
                                    @Param("limit") int limit);
    
    /**
     * Find lots by quality grade
     */
    @Select({
        "SELECT * FROM product_lots WHERE quality_grade = #{grade} AND deleted = false",
        "ORDER BY production_date DESC",
        "LIMIT #{limit} OFFSET #{offset}"
    })
    List<ProductLot> findByQualityGrade(@Param("grade") String grade,
                                       @Param("offset") int offset,
                                       @Param("limit") int limit);
    
    /**
     * Reserve quantity from lot
     */
    @Update({
        "UPDATE product_lots SET",
        "available_quantity = available_quantity - #{quantity},",
        "reserved_quantity = reserved_quantity + #{quantity},",
        "updated_at = NOW(), updated_by = #{updatedBy}",
        "WHERE id = #{id} AND available_quantity >= #{quantity} AND deleted = false"
    })
    int reserveQuantity(@Param("id") Long id,
                       @Param("quantity") BigDecimal quantity,
                       @Param("updatedBy") String updatedBy);
    
    /**
     * Release reserved quantity from lot
     */
    @Update({
        "UPDATE product_lots SET",
        "available_quantity = available_quantity + #{quantity},",
        "reserved_quantity = reserved_quantity - #{quantity},",
        "updated_at = NOW(), updated_by = #{updatedBy}",
        "WHERE id = #{id} AND reserved_quantity >= #{quantity} AND deleted = false"
    })
    int releaseReservedQuantity(@Param("id") Long id,
                               @Param("quantity") BigDecimal quantity,
                               @Param("updatedBy") String updatedBy);
    
    /**
     * Update lot status
     */
    @Update({
        "UPDATE product_lots SET status = #{status}, updated_at = NOW(), updated_by = #{updatedBy}",
        "WHERE id = #{id} AND deleted = false"
    })
    int updateStatus(@Param("id") Long id,
                    @Param("status") ProductLot.LotStatus status,
                    @Param("updatedBy") String updatedBy);
    
    /**
     * Update available quantity
     */
    @Update({
        "UPDATE product_lots SET available_quantity = #{quantity}, updated_at = NOW(), updated_by = #{updatedBy}",
        "WHERE id = #{id} AND deleted = false"
    })
    int updateAvailableQuantity(@Param("id") Long id,
                               @Param("quantity") BigDecimal quantity,
                               @Param("updatedBy") String updatedBy);
    
    /**
     * Update storage location
     */
    @Update({
        "UPDATE product_lots SET storage_location = #{location}, warehouse = #{warehouse},",
        "updated_at = NOW(), updated_by = #{updatedBy}",
        "WHERE id = #{id} AND deleted = false"
    })
    int updateStorageLocation(@Param("id") Long id,
                             @Param("location") String location,
                             @Param("warehouse") String warehouse,
                             @Param("updatedBy") String updatedBy);
    
    /**
     * Update quality grade and test results
     */
    @Update({
        "UPDATE product_lots SET quality_grade = #{grade}, quality_test_results = #{results},",
        "updated_at = NOW(), updated_by = #{updatedBy}",
        "WHERE id = #{id} AND deleted = false"
    })
    int updateQuality(@Param("id") Long id,
                     @Param("grade") String grade,
                     @Param("results") String results,
                     @Param("updatedBy") String updatedBy);
    
    /**
     * Update selling price
     */
    @Update({
        "UPDATE product_lots SET selling_price = #{price}, updated_at = NOW(), updated_by = #{updatedBy}",
        "WHERE id = #{id} AND deleted = false"
    })
    int updateSellingPrice(@Param("id") Long id,
                          @Param("price") BigDecimal price,
                          @Param("updatedBy") String updatedBy);
    
    /**
     * Get lot statistics for product
     */
    @Select({
        "SELECT",
        "COUNT(*) as totalLots,",
        "SUM(quantity) as totalQuantity,",
        "SUM(available_quantity) as availableQuantity,",
        "SUM(reserved_quantity) as reservedQuantity,",
        "COUNT(CASE WHEN status = 'AVAILABLE' THEN 1 END) as availableLots,",
        "COUNT(CASE WHEN status = 'EXPIRED' THEN 1 END) as expiredLots",
        "FROM product_lots WHERE product_id = #{productId} AND deleted = false"
    })
    Map<String, Object> getProductLotStats(Long productId);
    
    /**
     * Get lot utilization
     */
    @Select({
        "SELECT",
        "id, lot_number,",
        "quantity as totalQuantity,",
        "available_quantity as availableQuantity,",
        "reserved_quantity as reservedQuantity,",
        "ROUND((quantity - available_quantity) / quantity * 100, 2) as utilizationPercentage",
        "FROM product_lots",
        "WHERE product_id = #{productId} AND deleted = false AND quantity > 0",
        "ORDER BY utilizationPercentage DESC"
    })
    List<Map<String, Object>> getLotUtilization(Long productId);
    
    /**
     * Check if lot number exists for different lot
     */
    @Select({
        "SELECT COUNT(*) FROM product_lots",
        "WHERE lot_number = #{lotNumber} AND id != #{lotId} AND deleted = false"
    })
    boolean existsByLotNumberAndNotId(@Param("lotNumber") String lotNumber, @Param("lotId") Long lotId);
    
    /**
     * Soft delete product lot
     */
    @Update({
        "UPDATE product_lots SET deleted = true, updated_at = NOW(), updated_by = #{updatedBy}",
        "WHERE id = #{id} AND producer_id = #{producerId}"
    })
    int deleteProductLot(@Param("id") Long id, 
                        @Param("producerId") Long producerId,
                        @Param("updatedBy") String updatedBy);
    
    /**
     * Bulk update lot status
     */
    @UpdateProvider(type = ProductLotSqlProvider.class, method = "bulkUpdateLotStatusSql")
    int bulkUpdateLotStatus(@Param("lotIds") List<Long> lotIds,
                           @Param("status") ProductLot.LotStatus status,
                           @Param("updatedBy") String updatedBy);
    
    /**
     * Find lots for FIFO allocation
     */
    @Select({
        "SELECT * FROM product_lots",
        "WHERE product_id = #{productId} AND status = 'AVAILABLE'",
        "AND available_quantity >= #{minQuantity} AND deleted = false",
        "ORDER BY expiry_date ASC, production_date ASC",
        "LIMIT #{limit}"
    })
    List<ProductLot> findLotsForFIFO(@Param("productId") Long productId,
                                    @Param("minQuantity") BigDecimal minQuantity,
                                    @Param("limit") int limit);
    
    /**
     * Get lot history for traceability
     */
    @SelectProvider(type = ProductLotSqlProvider.class, method = "getLotHistorySql")
    List<Map<String, Object>> getLotHistory(@Param("lotId") Long lotId);

    /**
     * Find product lots by inventory ID with pagination
     */
    @Select({
        "SELECT * FROM product_lots WHERE inventory_id = #{inventoryId} AND deleted = false",
        "ORDER BY created_at DESC",
        "LIMIT #{limit} OFFSET #{offset}"
    })
    List<ProductLot> findByInventoryId(@Param("inventoryId") Long inventoryId,
                                      @Param("offset") int offset,
                                      @Param("limit") int limit);

    /**
     * Count product lots by inventory ID
     */
    @Select({
        "SELECT COUNT(*) FROM product_lots WHERE inventory_id = #{inventoryId} AND deleted = false"
    })
    Long countByInventoryId(Long inventoryId);

    /**
     * Find product lots by producer ID with pagination
     */
    @Select({
        "SELECT * FROM product_lots WHERE producer_id = #{producerId} AND deleted = false",
        "ORDER BY created_at DESC",
        "LIMIT #{limit} OFFSET #{offset}"
    })
    List<ProductLot> findByProducerId(@Param("producerId") Long producerId,
                                     @Param("offset") int offset,
                                     @Param("limit") int limit);

    /**
     * Find expiring lots
     */
    @SelectProvider(type = ProductLotSqlProvider.class, method = "findExpiringLotsSql")
    List<ProductLot> findExpiringLots(@Param("days") int days, @Param("producerId") Long producerId);

    /**
     * Find available lots for sale
     */
    @SelectProvider(type = ProductLotSqlProvider.class, method = "findAvailableLotsForSaleSql")
    List<ProductLot> findAvailableLotsForSale(@Param("producerId") Long producerId);

    /**
     * Find lots by harvest date range
     */
    @SelectProvider(type = ProductLotSqlProvider.class, method = "findByHarvestDateRangeSql")
    List<ProductLot> findByHarvestDateRange(@Param("producerId") Long producerId,
                                           @Param("startDate") java.time.LocalDate startDate,
                                           @Param("endDate") java.time.LocalDate endDate,
                                           @Param("offset") int offset,
                                           @Param("limit") int limit);

    /**
     * Find lots by processing method
     */
    @Select({
        "SELECT * FROM product_lots",
        "WHERE processing_method = #{processingMethod} AND deleted = false",
        "ORDER BY created_at DESC",
        "LIMIT #{limit} OFFSET #{offset}"
    })
    List<ProductLot> findByProcessingMethod(@Param("processingMethod") String processingMethod,
                                           @Param("offset") int offset,
                                           @Param("limit") int limit);

    /**
     * Complete sale operation
     */
    @Update({
        "UPDATE product_lots SET",
        "sold_quantity = sold_quantity + #{quantity},",
        "available_quantity = available_quantity - #{quantity},",
        "reserved_quantity = reserved_quantity - #{quantity},",
        "updated_at = NOW(),",
        "updated_by = #{updatedBy}",
        "WHERE id = #{lotId} AND deleted = false"
    })
    int completeSale(@Param("lotId") Long lotId,
                        @Param("quantity") BigDecimal quantity,
                        @Param("updatedBy") String updatedBy);

    /**
     * Update expiry date
     */
    @Update({
        "UPDATE product_lots SET",
        "expiry_date = #{expiryDate},",
        "updated_at = NOW(),",
        "updated_by = #{updatedBy}",
        "WHERE id = #{lotId} AND deleted = false"
    })
    int updateExpiryDate(@Param("lotId") Long lotId,
                            @Param("expiryDate") java.time.LocalDate expiryDate,
                            @Param("updatedBy") String updatedBy);

    /**
     * Update storage conditions
     */
    @Update({
        "UPDATE product_lots SET",
        "storage_location = #{storageLocation},",
        "temperature_requirements = #{temperatureRequirements},",
        "humidity_requirements = #{humidityRequirements},",
        "updated_at = NOW(),",
        "updated_by = #{updatedBy}",
        "WHERE id = #{lotId} AND deleted = false"
    })
    int updateStorageConditions(@Param("lotId") Long lotId,
                                   @Param("storageLocation") String storageLocation,
                                   @Param("temperatureRequirements") String temperatureRequirements,
                                   @Param("humidityRequirements") String humidityRequirements,
                                   @Param("updatedBy") String updatedBy);

    /**
     * Update processing information
     */
    @Update({
        "UPDATE product_lots SET",
        "processing_method = #{processingMethod},",
        "processing_notes = #{processingNotes},",
        "updated_at = NOW(),",
        "updated_by = #{updatedBy}",
        "WHERE id = #{lotId} AND deleted = false"
    })
    int updateProcessing(@Param("lotId") Long lotId,
                           @Param("processingMethod") String processingMethod,
                           @Param("processingNotes") String processingNotes,
                           @Param("updatedBy") String updatedBy);

    /**
     * Mark lot as damaged
     */
    @Update({
        "UPDATE product_lots SET",
        "damaged_quantity = damaged_quantity + #{damagedQuantity},",
        "available_quantity = available_quantity - #{damagedQuantity},",
        "status = CASE WHEN available_quantity <= 0 THEN 'DAMAGED' ELSE status END,",
        "notes = CONCAT(IFNULL(notes, ''), ' - ', #{reason}),",
        "updated_at = NOW(),",
        "updated_by = #{updatedBy}",
        "WHERE id = #{lotId} AND deleted = false"
    })
    int markAsDamaged(@Param("lotId") Long lotId,
                         @Param("damagedQuantity") BigDecimal damagedQuantity,
                         @Param("reason") String reason,
                         @Param("updatedBy") String updatedBy);

    /**
     * Get producer lot statistics
     */
    @Select({
        "SELECT",
        "COUNT(*) as total_lots,",
        "SUM(total_quantity) as total_quantity,",
        "SUM(available_quantity) as available_quantity,",
        "SUM(sold_quantity) as sold_quantity,",
        "SUM(damaged_quantity) as damaged_quantity",
        "FROM product_lots",
        "WHERE producer_id = #{producerId} AND deleted = false"
    })
    Map<String, Object> getProducerLotStats(Long producerId);

    /**
     * Get lot performance by date range
     */
    @Select({
        "SELECT",
        "DATE(created_at) as date,",
        "COUNT(*) as lots_created,",
        "SUM(total_quantity) as total_quantity,",
        "AVG(DATEDIFF(expiry_date, harvest_date)) as avg_shelf_life",
        "FROM product_lots",
        "WHERE producer_id = #{producerId}",
        "AND created_at BETWEEN #{startDate} AND #{endDate}",
        "AND deleted = false",
        "GROUP BY DATE(created_at)",
        "ORDER BY date DESC"
    })
    List<Map<String, Object>> getLotPerformanceByDateRange(@Param("producerId") Long producerId,
                                                          @Param("startDate") String startDate,
                                                          @Param("endDate") String endDate);

    /**
     * Get quality grade distribution
     */
    @Select({
        "SELECT quality_grade, COUNT(*) as count",
        "FROM product_lots",
        "WHERE producer_id = #{producerId} AND deleted = false",
        "GROUP BY quality_grade",
        "ORDER BY count DESC"
    })
    List<Map<String, Object>> getQualityGradeDistribution(Long producerId);

    /**
     * Get expiry analysis
     */
    @Select({
        "SELECT",
        "COUNT(CASE WHEN expiry_date < NOW() THEN 1 END) as expired_count,",
        "COUNT(CASE WHEN expiry_date BETWEEN NOW() AND DATE_ADD(NOW(), INTERVAL #{days} DAY) THEN 1 END) as expiring_count,",
        "COUNT(*) as total_count",
        "FROM product_lots",
        "WHERE producer_id = #{producerId} AND deleted = false"
    })
    Map<String, Object> getExpiryAnalysis(@Param("producerId") Long producerId, @Param("days") int days);

    /**
     * Check if lot number exists
     */
    @Select({
        "SELECT COUNT(*) > 0 FROM product_lots WHERE lot_number = #{lotNumber} AND deleted = false"
    })
    boolean existsByLotNumber(String lotNumber);

    /**
     * Bulk update lot status
     */
    @UpdateProvider(type = ProductLotSqlProvider.class, method = "bulkUpdateStatusSql")
    int bulkUpdateStatus(@Param("lotIds") List<Long> lotIds,
                           @Param("status") ProductLot.LotStatus status,
                           @Param("updatedBy") String updatedBy);

    /**
     * Get lot traceability information
     */
    @Select({
        "SELECT * FROM product_lots WHERE lot_number = #{lotNumber} AND deleted = false"
    })
    Map<String, Object> getLotTraceability(String lotNumber);

    /**
     * Find lots by certification
     */
    @SelectProvider(type = ProductLotSqlProvider.class, method = "findByCertificationSql")
    List<ProductLot> findByCertification(@Param("certifications") List<String> certifications,
                                        @Param("offset") int offset,
                                        @Param("limit") int limit);

    /**
     * Get lot movement history
     */
    @Select({
        "SELECT",
        "created_at as movement_date,",
        "'CREATED' as movement_type,",
        "total_quantity as quantity,",
        "storage_location as location",
        "FROM product_lots",
        "WHERE id = #{lotId} AND deleted = false",
        "UNION ALL",
        "SELECT",
        "updated_at as movement_date,",
        "'UPDATED' as movement_type,",
        "available_quantity as quantity,",
        "storage_location as location",
        "FROM product_lots",
        "WHERE id = #{lotId} AND updated_at > created_at AND deleted = false",
        "ORDER BY movement_date DESC",
        "LIMIT #{limit}"
    })
    List<Map<String, Object>> getLotMovementHistory(@Param("lotId") Long lotId, @Param("limit") int limit);

    @Select({
        "SELECT * FROM product_lots",
        "WHERE product_id = #{productId} AND producer_id = #{producerId}",
        "AND status = 'AVAILABLE' AND deleted = false",
        "ORDER BY harvest_date DESC"
    })
    List<ProductLot> findAvailableByProductAndProducer(@Param("productId") Long productId, @Param("producerId") Long producerId);

    @Select({
        "SELECT * FROM product_lots",
        "WHERE product_id = #{productId} AND producer_id = #{producerId}",
        "AND status = 'SOLD_OUT' AND deleted = false",
        "ORDER BY harvest_date DESC"
    })
    List<ProductLot> findSoldOutByProductAndProducer(@Param("productId") Long productId, @Param("producerId") Long producerId);

    @Select({
        "SELECT * FROM product_lots",
        "WHERE quality_grade = #{qualityGrade} AND product_id = #{productId}",
        "AND producer_id = #{producerId} AND deleted = false",
        "ORDER BY harvest_date DESC"
    })
    List<ProductLot> findByQualityGradeAndProductAndProducer(@Param("qualityGrade") String qualityGrade, 
                                                           @Param("productId") Long productId, 
                                                           @Param("producerId") Long producerId);

    @Select({
        "SELECT * FROM product_lots",
        "WHERE product_id = #{productId} AND producer_id = #{producerId}",
        "AND production_date BETWEEN #{startDate} AND #{endDate}",
        "AND deleted = false",
        "ORDER BY production_date DESC"
    })
    List<ProductLot> findByProductionDateRange(@Param("productId") Long productId, 
                                             @Param("producerId") Long producerId,
                                             @Param("startDate") LocalDate startDate, 
                                             @Param("endDate") LocalDate endDate);

    @Select({
        "SELECT * FROM product_lots",
        "WHERE field_location = #{fieldLocation} AND producer_id = #{producerId}",
        "AND deleted = false",
        "ORDER BY harvest_date DESC"
    })
    List<ProductLot> findByFieldLocationAndProducer(@Param("fieldLocation") String fieldLocation, 
                                                   @Param("producerId") Long producerId);

    @SelectProvider(type = ProductLotSqlProvider.class, method = "searchLotsSql")
    List<ProductLot> searchLots(@Param("searchTerm") String searchTerm, 
                               @Param("productId") Long productId,
                               @Param("producerId") Long producerId,
                               @Param("qualityGrade") String qualityGrade,
                               @Param("status") String status,
                               @Param("harvestStartDate") LocalDate harvestStartDate,
                               @Param("harvestEndDate") LocalDate harvestEndDate,
                               @Param("productionStartDate") LocalDate productionStartDate,
                               @Param("productionEndDate") LocalDate productionEndDate,
                               @Param("minQuantity") BigDecimal minQuantity,
                               @Param("pageable") Pageable pageable);

    @Select("SELECT COUNT(*) FROM product_lots WHERE producer_id = #{producerId} AND deleted = false")
    Long countByProducerId(@Param("producerId") Long producerId);
}