package com.procgrid.productservice.mapper.mybatis;

import com.procgrid.productservice.model.ProductLot;

import java.util.List;
import java.util.Map;

/**
 * SQL provider for dynamic ProductLot statements to remove XML <script>/<if>/<foreach> usage.
 */
public class ProductLotSqlProvider {

    public String updateProductLotSql(ProductLot lot) {
        StringBuilder sql = new StringBuilder("UPDATE product_lots SET ");
        boolean first = true;
        first = append(sql, "product_id = #{productId}", lot.getProductId() != null, first);
        first = append(sql, "lot_number = #{lotNumber}", lot.getLotNumber() != null, first);
        first = append(sql, "quantity = #{quantity}", lot.getQuantity() != null, first);
        first = append(sql, "available_quantity = #{availableQuantity}", lot.getAvailableQuantity() != null, first);
        first = append(sql, "reserved_quantity = #{reservedQuantity}", lot.getReservedQuantity() != null, first);
        first = append(sql, "quantity_unit = #{quantityUnit}", lot.getQuantityUnit() != null, first);
        first = append(sql, "production_date = #{productionDate}", lot.getProductionDate() != null, first);
        first = append(sql, "harvest_date = #{harvestDate}", lot.getHarvestDate() != null, first);
        first = append(sql, "expiry_date = #{expiryDate}", lot.getExpiryDate() != null, first);
        first = append(sql, "storage_location = #{storageLocation}", lot.getStorageLocation() != null, first);
        first = append(sql, "warehouse = #{warehouse}", lot.getWarehouse() != null, first);
        first = append(sql, "quality_grade = #{qualityGrade}", lot.getQualityGrade() != null, first);
        first = append(sql, "quality_test_results = #{qualityTestResults}", lot.getQualityTestResults() != null, first);
        first = append(sql, "supplier_details = #{supplierDetails}", lot.getSupplierDetails() != null, first);
        first = append(sql, "field_location = #{fieldLocation}", lot.getFieldLocation() != null, first);
        first = append(sql, "gps_coordinates = #{gpsCoordinates}", lot.getGpsCoordinates() != null, first);
        first = append(sql, "status = #{status}", lot.getStatus() != null, first);
        first = append(sql, "cost_price = #{costPrice}", lot.getCostPrice() != null, first);
        first = append(sql, "selling_price = #{sellingPrice}", lot.getSellingPrice() != null, first);
        first = append(sql, "notes = #{notes}", lot.getNotes() != null, first);
        first = append(sql, "certifications = #{certifications}", lot.getCertifications() != null, first);
        first = append(sql, "temperature_requirements = #{temperatureRequirements}", lot.getTemperatureRequirements() != null, first);
        first = append(sql, "humidity_requirements = #{humidityRequirements}", lot.getHumidityRequirements() != null, first);
        if (!first) sql.append(',');
        sql.append("updated_at = #{updatedAt}, updated_by = #{updatedBy} WHERE id = #{id} AND deleted = false");
        return sql.toString();
    }

    public String bulkUpdateLotStatusSql(Map<String,Object> params) {
        @SuppressWarnings("unchecked") List<Long> ids = (List<Long>) params.get("lotIds");
        StringBuilder sql = new StringBuilder("UPDATE product_lots SET status = #{status}, updated_at = NOW(), updated_by = #{updatedBy} WHERE ");
        if (ids != null && !ids.isEmpty()) {
            sql.append("id IN (");
            for (int i=0;i<ids.size();i++){ if(i>0) sql.append(','); sql.append("#{lotIds["+i+"]}"); }
            sql.append(") AND deleted = false");
        } else {
            sql.append("1=0");
        }
        return sql.toString();
    }

    public String getLotHistorySql(Map<String,Object> params) {
        return "SELECT 'CREATION' as eventType, created_at as eventDate, quantity as eventQuantity, 'Lot created' as eventDescription, created_by as eventBy FROM product_lots WHERE id = #{lotId} AND deleted = false UNION ALL SELECT 'UPDATE' as eventType, updated_at as eventDate, available_quantity as eventQuantity, 'Lot updated' as eventDescription, updated_by as eventBy FROM product_lots WHERE id = #{lotId} AND updated_at > created_at AND deleted = false ORDER BY eventDate ASC";
    }

    public String findByCertificationSql(Map<String,Object> params) {
        @SuppressWarnings("unchecked") List<String> certs = (List<String>) params.get("certifications");
        StringBuilder sql = new StringBuilder("SELECT * FROM product_lots WHERE deleted = false ");
        if (certs != null && !certs.isEmpty()) {
            sql.append("AND (");
            for (int i=0;i<certs.size();i++){ if(i>0) sql.append(" OR "); sql.append("FIND_IN_SET(#{certifications["+i+"]}, certifications) > 0"); }
            sql.append(") ");
        }
        sql.append("ORDER BY created_at DESC LIMIT #{limit} OFFSET #{offset}");
        return sql.toString();
    }

    public String bulkUpdateStatusSql(Map<String,Object> params) { // second bulk update variant
        return bulkUpdateLotStatusSql(params);
    }

    public String searchLotsSql(Map<String,Object> params) {
        StringBuilder sql = new StringBuilder("SELECT * FROM product_lots WHERE deleted = false ");
        like(sql, params, "searchTerm", "AND (lot_number LIKE CONCAT('%', #{searchTerm}, '%') OR notes LIKE CONCAT('%', #{searchTerm}, '%')) ");
        eq(sql, params, "productId", "AND product_id = #{productId} ");
        eq(sql, params, "producerId", "AND producer_id = #{producerId} ");
        eq(sql, params, "qualityGrade", "AND quality_grade = #{qualityGrade} ");
        eq(sql, params, "status", "AND status = #{status} ");
        ge(sql, params, "harvestStartDate", "AND harvest_date >= #{harvestStartDate} ");
        le(sql, params, "harvestEndDate", "AND harvest_date <= #{harvestEndDate} ");
        ge(sql, params, "productionStartDate", "AND production_date >= #{productionStartDate} ");
        le(sql, params, "productionEndDate", "AND production_date <= #{productionEndDate} ");
        ge(sql, params, "minQuantity", "AND total_quantity >= #{minQuantity} ");
        sql.append("ORDER BY created_at DESC LIMIT #{pageable.pageSize} OFFSET #{pageable.offset}");
        return sql.toString();
    }

    public String findExpiredLotsSql(Map<String,Object> params) {
        StringBuilder sql = new StringBuilder("SELECT * FROM product_lots WHERE expiry_date IS NOT NULL AND expiry_date < NOW() AND status NOT IN ('EXPIRED','DAMAGED') AND deleted = false ");
        if (params.get("producerId") != null) sql.append("AND producer_id = #{producerId} ");
        sql.append("ORDER BY expiry_date ASC");
        return sql.toString();
    }

    public String findExpiringLotsSql(Map<String,Object> params) {
        StringBuilder sql = new StringBuilder("SELECT * FROM product_lots WHERE expiry_date BETWEEN NOW() AND DATE_ADD(NOW(), INTERVAL #{days} DAY) AND status = 'AVAILABLE' AND deleted = false ");
        if (params.get("producerId") != null) sql.append("AND producer_id = #{producerId} ");
        sql.append("ORDER BY expiry_date ASC");
        return sql.toString();
    }

    public String findAvailableLotsForSaleSql(Map<String,Object> params) {
        StringBuilder sql = new StringBuilder("SELECT * FROM product_lots WHERE status='AVAILABLE' AND available_quantity > 0 AND expiry_date > NOW() AND deleted = false ");
        if (params.get("producerId") != null) sql.append("AND producer_id = #{producerId} ");
        sql.append("ORDER BY expiry_date ASC");
        return sql.toString();
    }

    public String findByHarvestDateRangeSql(Map<String,Object> params) {
        StringBuilder sql = new StringBuilder("SELECT * FROM product_lots WHERE harvest_date BETWEEN #{startDate} AND #{endDate} AND deleted = false ");
        if (params.get("producerId") != null) sql.append("AND producer_id = #{producerId} ");
        sql.append("ORDER BY harvest_date DESC LIMIT #{limit} OFFSET #{offset}");
        return sql.toString();
    }

    private boolean append(StringBuilder sb, String fragment, boolean condition, boolean first) {
        if (!condition) return first;
        if (!first) sb.append(',');
        sb.append(fragment);
        return false;
    }
    private void like(StringBuilder sb, Map<String,Object> params, String key, String fragment) {
        Object v = params.get(key); if (v instanceof String && !((String)v).isBlank()) sb.append(fragment);
    }
    private void eq(StringBuilder sb, Map<String,Object> params, String key, String fragment) { if (params.get(key) != null) sb.append(fragment); }
    private void ge(StringBuilder sb, Map<String,Object> params, String key, String fragment) { if (params.get(key) != null) sb.append(fragment); }
    private void le(StringBuilder sb, Map<String,Object> params, String key, String fragment) { if (params.get(key) != null) sb.append(fragment); }
}