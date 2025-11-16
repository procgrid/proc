package com.procgrid.productservice.mapper.mybatis;

import com.procgrid.productservice.model.Inventory;
import org.apache.ibatis.annotations.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * MyBatis mapper interface for Inventory operations
 */
@Mapper
public interface InventoryMyBatisMapper {
    
    /**
     * Insert new inventory record
     */
    @Insert({
        "INSERT INTO inventory (",
        "product_id, producer_id, total_quantity, available_quantity, reserved_quantity,",
        "sold_quantity, damaged_quantity, quantity_unit, min_stock_level, max_stock_level,",
        "reorder_quantity, average_cost, total_value, status, location, warehouse_id,",
        "last_updated, last_stock_count, next_stock_count_due, turnover_rate, days_of_supply,",
        "lead_time_days, seasonal_factor, notes, created_at, updated_at, created_by,",
        "updated_by, deleted",
        ") VALUES (",
        "#{productId}, #{producerId}, #{totalQuantity}, #{availableQuantity}, #{reservedQuantity},",
        "#{soldQuantity}, #{damagedQuantity}, #{quantityUnit}, #{minStockLevel}, #{maxStockLevel},",
        "#{reorderQuantity}, #{averageCost}, #{totalValue}, #{status}, #{location}, #{warehouseId},",
        "#{lastUpdated}, #{lastStockCount}, #{nextStockCountDue}, #{turnoverRate}, #{daysOfSupply},",
        "#{leadTimeDays}, #{seasonalFactor}, #{notes}, #{createdAt}, #{updatedAt}, #{createdBy},",
        "#{updatedBy}, #{deleted}",
        ")"
    })
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insertInventory(Inventory inventory);
    
    /**
     * Update existing inventory
     */
    @Update({
        "<script>",
        "UPDATE inventory",
        "SET",
        "  <if test='productId != null'>product_id = #{productId},</if>",
        "  <if test='producerId != null'>producer_id = #{producerId},</if>",
        "  <if test='totalQuantity != null'>total_quantity = #{totalQuantity},</if>",
        "  <if test='availableQuantity != null'>available_quantity = #{availableQuantity},</if>",
        "  <if test='reservedQuantity != null'>reserved_quantity = #{reservedQuantity},</if>",
        "  <if test='soldQuantity != null'>sold_quantity = #{soldQuantity},</if>",
        "  <if test='damagedQuantity != null'>damaged_quantity = #{damagedQuantity},</if>",
        "  <if test='quantityUnit != null'>quantity_unit = #{quantityUnit},</if>",
        "  <if test='minStockLevel != null'>min_stock_level = #{minStockLevel},</if>",
        "  <if test='maxStockLevel != null'>max_stock_level = #{maxStockLevel},</if>",
        "  <if test='reorderQuantity != null'>reorder_quantity = #{reorderQuantity},</if>",
        "  <if test='averageCost != null'>average_cost = #{averageCost},</if>",
        "  <if test='totalValue != null'>total_value = #{totalValue},</if>",
        "  <if test='status != null'>status = #{status},</if>",
        "  <if test='location != null'>location = #{location},</if>",
        "  <if test='warehouseId != null'>warehouse_id = #{warehouseId},</if>",
        "  <if test='lastUpdated != null'>last_updated = #{lastUpdated},</if>",
        "  <if test='lastStockCount != null'>last_stock_count = #{lastStockCount},</if>",
        "  <if test='nextStockCountDue != null'>next_stock_count_due = #{nextStockCountDue},</if>",
        "  <if test='turnoverRate != null'>turnover_rate = #{turnoverRate},</if>",
        "  <if test='daysOfSupply != null'>days_of_supply = #{daysOfSupply},</if>",
        "  <if test='leadTimeDays != null'>lead_time_days = #{leadTimeDays},</if>",
        "  <if test='seasonalFactor != null'>seasonal_factor = #{seasonalFactor},</if>",
        "  <if test='notes != null'>notes = #{notes},</if>",
        "  updated_at = #{updatedAt},",
        "  updated_by = #{updatedBy}",
        "WHERE id = #{id} AND deleted = false",
        "</script>"
    })
    int updateInventory(Inventory inventory);
    
    /**
     * Find inventory by ID
     */
    @Select({
        "SELECT * FROM inventory WHERE id = #{id} AND deleted = false"
    })
    Inventory findById(Long id);
    
    /**
     * Find inventory by product ID
     */
    @Select({
        "SELECT * FROM inventory WHERE product_id = #{productId} AND deleted = false"
    })
    Inventory findByProductId(Long productId);
    
    /**
     * Find inventory by producer ID
     */
    @Select({
        "SELECT * FROM inventory WHERE producer_id = #{producerId} AND deleted = false",
        "ORDER BY last_updated DESC",
        "LIMIT #{limit} OFFSET #{offset}"
    })
    List<Inventory> findByProducerId(@Param("producerId") Long producerId,
                                    @Param("offset") int offset,
                                    @Param("limit") int limit);
    
    /**
     * Count inventory records by producer ID
     */
    @Select({
        "SELECT COUNT(*) FROM inventory WHERE producer_id = #{producerId} AND deleted = false"
    })
    Long countByProducerId(Long producerId);
    
    /**
     * Find low stock inventory
     */
    @Select({
        "SELECT * FROM inventory",
        "WHERE available_quantity <= min_stock_level",
        "AND min_stock_level IS NOT NULL",
        "AND producer_id = #{producerId}",
        "AND deleted = false",
        "ORDER BY available_quantity ASC"
    })
    List<Inventory> findLowStockByProducerId(Long producerId);
    
    /**
     * Find out of stock inventory
     */
    @Select({
        "SELECT * FROM inventory",
        "WHERE available_quantity = 0",
        "AND producer_id = #{producerId}",
        "AND deleted = false",
        "ORDER BY last_updated DESC"
    })
    List<Inventory> findOutOfStockByProducerId(Long producerId);
    
    /**
     * Find overstocked inventory
     */
    @Select({
        "SELECT * FROM inventory",
        "WHERE total_quantity > max_stock_level",
        "AND max_stock_level IS NOT NULL",
        "AND producer_id = #{producerId}",
        "AND deleted = false",
        "ORDER BY total_quantity DESC"
    })
    List<Inventory> findOverstockedByProducerId(Long producerId);
    
    /**
     * Find inventory by status
     */
    @Select({
        "SELECT * FROM inventory",
        "WHERE status = #{status} AND deleted = false",
        "ORDER BY last_updated DESC",
        "LIMIT #{limit} OFFSET #{offset}"
    })
    List<Inventory> findByStatus(@Param("status") Inventory.InventoryStatus status,
                                @Param("offset") int offset,
                                @Param("limit") int limit);
    
    /**
     * Find inventory requiring stock count
     */
    @Select({
        "SELECT * FROM inventory",
        "WHERE next_stock_count_due <= NOW()",
        "AND deleted = false",
        "ORDER BY next_stock_count_due ASC"
    })
    List<Inventory> findInventoryRequiringStockCount();
    
    /**
     * Update available quantity
     */
    @Update({
        "UPDATE inventory SET available_quantity = #{quantity}, last_updated = NOW(), updated_by = #{updatedBy}",
        "WHERE id = #{id} AND deleted = false"
    })
    int updateAvailableQuantity(@Param("id") Long id,
                               @Param("quantity") BigDecimal quantity,
                               @Param("updatedBy") String updatedBy);
    
    /**
     * Reserve quantity
     */
    @Update({
        "UPDATE inventory SET",
        "available_quantity = available_quantity - #{quantity},",
        "reserved_quantity = reserved_quantity + #{quantity},",
        "last_updated = NOW(), updated_by = #{updatedBy}",
        "WHERE id = #{id} AND available_quantity >= #{quantity} AND deleted = false"
    })
    int reserveQuantity(@Param("id") Long id,
                       @Param("quantity") BigDecimal quantity,
                       @Param("updatedBy") String updatedBy);
    
    /**
     * Release reserved quantity
     */
    @Update({
        "UPDATE inventory SET",
        "available_quantity = available_quantity + #{quantity},",
        "reserved_quantity = reserved_quantity - #{quantity},",
        "last_updated = NOW(), updated_by = #{updatedBy}",
        "WHERE id = #{id} AND reserved_quantity >= #{quantity} AND deleted = false"
    })
    int releaseReservedQuantity(@Param("id") Long id,
                               @Param("quantity") BigDecimal quantity,
                               @Param("updatedBy") String updatedBy);
    
    /**
     * Complete sale (move from reserved to sold)
     */
    @Update({
        "UPDATE inventory SET",
        "reserved_quantity = reserved_quantity - #{quantity},",
        "sold_quantity = sold_quantity + #{quantity},",
        "total_quantity = total_quantity - #{quantity},",
        "last_updated = NOW(), updated_by = #{updatedBy}",
        "WHERE id = #{id} AND reserved_quantity >= #{quantity} AND deleted = false"
    })
    int completeSale(@Param("id") Long id,
                    @Param("quantity") BigDecimal quantity,
                    @Param("updatedBy") String updatedBy);
    
    /**
     * Add stock
     */
    @Update({
        "UPDATE inventory SET",
        "total_quantity = total_quantity + #{quantity},",
        "available_quantity = available_quantity + #{quantity},",
        "last_updated = NOW(), updated_by = #{updatedBy}",
        "WHERE id = #{id} AND deleted = false"
    })
    int addStock(@Param("id") Long id,
                @Param("quantity") BigDecimal quantity,
                @Param("updatedBy") String updatedBy);
    
    /**
     * Remove damaged stock
     */
    @Update({
        "UPDATE inventory SET",
        "available_quantity = available_quantity - #{quantity},",
        "total_quantity = total_quantity - #{quantity},",
        "damaged_quantity = damaged_quantity + #{quantity},",
        "last_updated = NOW(), updated_by = #{updatedBy}",
        "WHERE id = #{id} AND available_quantity >= #{quantity} AND deleted = false"
    })
    int removeDamagedStock(@Param("id") Long id,
                          @Param("quantity") BigDecimal quantity,
                          @Param("updatedBy") String updatedBy);
    
    /**
     * Update inventory status
     */
    @Update({
        "UPDATE inventory SET status = #{status}, last_updated = NOW(), updated_by = #{updatedBy}",
        "WHERE id = #{id} AND deleted = false"
    })
    int updateStatus(@Param("id") Long id,
                    @Param("status") Inventory.InventoryStatus status,
                    @Param("updatedBy") String updatedBy);
    
    /**
     * Update stock levels
     */
    @Update({
        "UPDATE inventory SET",
        "min_stock_level = #{minLevel}, max_stock_level = #{maxLevel},",
        "reorder_quantity = #{reorderQty}, last_updated = NOW(), updated_by = #{updatedBy}",
        "WHERE id = #{id} AND deleted = false"
    })
    int updateStockLevels(@Param("id") Long id,
                         @Param("minLevel") BigDecimal minLevel,
                         @Param("maxLevel") BigDecimal maxLevel,
                         @Param("reorderQty") BigDecimal reorderQty,
                         @Param("updatedBy") String updatedBy);
    
    /**
     * Update cost and value
     */
    @Update({
        "UPDATE inventory SET",
        "average_cost = #{cost}, total_value = #{value},",
        "last_updated = NOW(), updated_by = #{updatedBy}",
        "WHERE id = #{id} AND deleted = false"
    })
    int updateCostAndValue(@Param("id") Long id,
                          @Param("cost") BigDecimal cost,
                          @Param("value") BigDecimal value,
                          @Param("updatedBy") String updatedBy);
    
    /**
     * Update stock count date
     */
    @Update({
        "UPDATE inventory SET",
        "last_stock_count = NOW(),",
        "next_stock_count_due = DATE_ADD(NOW(), INTERVAL #{days} DAY),",
        "last_updated = NOW(), updated_by = #{updatedBy}",
        "WHERE id = #{id} AND deleted = false"
    })
    int updateStockCountDate(@Param("id") Long id,
                            @Param("days") int days,
                            @Param("updatedBy") String updatedBy);
    
    /**
     * Get inventory statistics for producer
     */
    @Select({
        "SELECT",
        "COUNT(*) as totalItems,",
        "SUM(total_quantity) as totalQuantity,",
        "SUM(available_quantity) as availableQuantity,",
        "SUM(reserved_quantity) as reservedQuantity,",
        "SUM(sold_quantity) as soldQuantity,",
        "SUM(total_value) as totalValue,",
        "COUNT(CASE WHEN status = 'LOW_STOCK' THEN 1 END) as lowStockItems,",
        "COUNT(CASE WHEN status = 'OUT_OF_STOCK' THEN 1 END) as outOfStockItems",
        "FROM inventory WHERE producer_id = #{producerId} AND deleted = false"
    })
    Map<String, Object> getProducerInventoryStats(Long producerId);
    
    /**
     * Get inventory value by date range
     */
    @Select({
        "<script>",
        "SELECT",
        "  DATE(last_updated) as date,",
        "  SUM(total_value) as totalValue,",
        "  SUM(total_quantity) as totalQuantity,",
        "  COUNT(*) as itemCount",
        "FROM inventory",
        "WHERE producer_id = #{producerId}",
        "  AND deleted = false",
        "  AND DATE(last_updated) BETWEEN #{startDate} AND #{endDate}",
        "GROUP BY DATE(last_updated)",
        "ORDER BY date ASC",
        "</script>"
    })
    List<Map<String, Object>> getInventoryValueByDateRange(@Param("producerId") Long producerId,
                                                           @Param("startDate") String startDate,
                                                           @Param("endDate") String endDate);
    
    /**
     * Soft delete inventory
     */
    @Update({
        "UPDATE inventory SET deleted = true, updated_at = NOW(), updated_by = #{updatedBy}",
        "WHERE id = #{id} AND producer_id = #{producerId}"
    })
    int deleteInventory(@Param("id") Long id,
                       @Param("producerId") Long producerId,
                       @Param("updatedBy") String updatedBy);
    
    /**
     * Check if inventory exists for product
     */
    @Select({
        "SELECT COUNT(*) > 0 FROM inventory",
        "WHERE product_id = #{productId} AND deleted = false"
    })
    boolean existsByProductId(Long productId);
    
    /**
     * Bulk update inventory status
     */
    @Update({
        "<script>",
        "UPDATE inventory SET",
        "  status = #{status},",
        "  updated_at = NOW(),",
        "  updated_by = #{updatedBy}",
        "WHERE id IN",
        "  <foreach collection='inventoryIds' item='id' open='(' separator=',' close=')'>",
        "    #{id}",
        "  </foreach>",
        "  AND deleted = false",
        "</script>"
    })
    int bulkUpdateStatus(@Param("inventoryIds") List<Long> inventoryIds,
                        @Param("status") Inventory.InventoryStatus status,
                        @Param("updatedBy") String updatedBy);
    
    /**
     * Get inventory movement history
     */
    @Select({
        "<script>",
        "SELECT",
        "  'STOCK_MOVEMENT' as movementType,",
        "  DATE(last_updated) as movementDate,",
        "  total_quantity as quantity,",
        "  'Inventory Update' as description,",
        "  updated_by as updatedBy",
        "FROM inventory",
        "WHERE id = #{inventoryId}",
        "  AND deleted = false",
        "  AND DATE(last_updated) >= DATE_SUB(CURDATE(), INTERVAL #{days} DAY)",
        "ORDER BY last_updated DESC",
        "</script>"
    })
    List<Map<String, Object>> getInventoryMovementHistory(@Param("inventoryId") Long inventoryId,
                                                          @Param("days") int days);
}