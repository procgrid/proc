package com.procgrid.productservice.mapper.mybatis;

import com.procgrid.productservice.model.Product;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * SQL provider for dynamic Product MyBatis statements replacing XML/dynamic script tags.
 */
public class ProductSqlProvider {

    public String buildUpdateProduct(final Product p) {
        StringBuilder sql = new StringBuilder();
        sql.append("UPDATE products SET ");
        boolean first = true;
        first = append(sql, "name = #{name}", p.getName() != null, first);
        first = append(sql, "description = #{description}", p.getDescription() != null, first);
        first = append(sql, "summary = #{summary}", p.getSummary() != null, first);
        first = append(sql, "variety = #{variety}", p.getVariety() != null, first);
        first = append(sql, "grade = #{grade}", p.getGrade() != null, first);
        first = append(sql, "origin = #{origin}", p.getOrigin() != null, first);
        first = append(sql, "harvest_date = #{harvestDate}", p.getHarvestDate() != null, first);
        first = append(sql, "expiry_date = #{expiryDate}", p.getExpiryDate() != null, first);
        first = append(sql, "price = #{price}", p.getPrice() != null, first);
        first = append(sql, "price_unit = #{priceUnit}", p.getPriceUnit() != null, first);
        first = append(sql, "available_quantity = #{availableQuantity}", p.getAvailableQuantity() != null, first);
        first = append(sql, "quantity_unit = #{quantityUnit}", p.getQuantityUnit() != null, first);
        first = append(sql, "min_order_quantity = #{minOrderQuantity}", p.getMinOrderQuantity() != null, first);
        first = append(sql, "max_order_quantity = #{maxOrderQuantity}", p.getMaxOrderQuantity() != null, first);
        first = append(sql, "status = #{status}", p.getStatus() != null, first);
        first = append(sql, "visibility = #{visibility}", p.getVisibility() != null, first);
        first = append(sql, "certifications = #{certifications, typeHandler=com.procgrid.productservice.handler.JsonListTypeHandler}", p.getCertifications() != null, first);
        first = append(sql, "tags = #{tags, typeHandler=com.procgrid.productservice.handler.JsonListTypeHandler}", p.getTags() != null, first);
        first = append(sql, "image_urls = #{imageUrls, typeHandler=com.procgrid.productservice.handler.JsonListTypeHandler}", p.getImageUrls() != null, first);
        first = append(sql, "primary_image_url = #{primaryImageUrl}", p.getPrimaryImageUrl() != null, first);
        first = append(sql, "packaging = #{packaging}", p.getPackaging() != null, first);
        first = append(sql, "storage_requirements = #{storageRequirements}", p.getStorageRequirements() != null, first);
        first = append(sql, "transportation_requirements = #{transportationRequirements}", p.getTransportationRequirements() != null, first);
        first = append(sql, "supports_bulk_orders = #{supportsBulkOrders}", p.getSupportsBulkOrders() != null, first);
        first = append(sql, "is_seasonal = #{isSeasonal}", p.getIsSeasonal() != null, first);
        first = append(sql, "season_info = #{seasonInfo}", p.getSeasonInfo() != null, first);
        first = append(sql, "slug = #{slug}", p.getSlug() != null, first);
        first = append(sql, "meta_keywords = #{metaKeywords}", p.getMetaKeywords() != null, first);
        first = append(sql, "meta_description = #{metaDescription}", p.getMetaDescription() != null, first);
        // Always update audit columns
        if (!first) {
            sql.append(',');
        }
        sql.append("updated_at = NOW(), updated_by = #{updatedBy} ");
        sql.append("WHERE id = #{id} AND deleted = false");
        return sql.toString();
    }

    private boolean append(StringBuilder sb, String fragment, boolean condition, boolean first) {
        if (!condition) return first;
        if (!first) sb.append(',');
        sb.append(fragment);
        return false; // after first append, first becomes false
    }

    // Dynamic select for certifications
    public String findByCertificationSql(Map<String, Object> params) {
        @SuppressWarnings("unchecked") List<String> certs = (List<String>) params.get("certifications");
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT * FROM products WHERE deleted = false AND status = 'ACTIVE' AND visibility = 'PUBLIC' ");
        if (certs != null && !certs.isEmpty()) {
            sql.append("AND (");
            for (int i = 0; i < certs.size(); i++) {
                if (i > 0) sql.append(" OR ");
                sql.append("certifications LIKE CONCAT('%', #{certifications[" + i + "]}, '%')");
            }
            sql.append(") ");
        }
        sql.append("ORDER BY created_at DESC LIMIT #{limit} OFFSET #{offset}");
        return sql.toString();
    }

    public String countByCertificationSql(Map<String, Object> params) {
        @SuppressWarnings("unchecked") List<String> certs = (List<String>) params.get("certifications");
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT COUNT(*) FROM products WHERE deleted = false AND status = 'ACTIVE' AND visibility = 'PUBLIC' ");
        if (certs != null && !certs.isEmpty()) {
            sql.append("AND (");
            for (int i = 0; i < certs.size(); i++) {
                if (i > 0) sql.append(" OR ");
                sql.append("certifications LIKE CONCAT('%', #{certifications[" + i + "]}, '%')");
            }
            sql.append(") ");
        }
        return sql.toString();
    }

    public String updateLocationSql(Map<String, Object> params) {
        StringBuilder sql = new StringBuilder("UPDATE products SET ");
        boolean first = true;
        first = append(sql, "city = #{city}", params.get("city") != null, first);
        first = append(sql, "state = #{state}", params.get("state") != null, first);
        first = append(sql, "country = #{country}", params.get("country") != null, first);
        first = append(sql, "zip_code = #{zipCode}", params.get("zipCode") != null, first);
        first = append(sql, "latitude = #{latitude}", params.get("latitude") != null, first);
        first = append(sql, "longitude = #{longitude}", params.get("longitude") != null, first);
        if (!first) sql.append(',');
        sql.append("updated_at = NOW(), updated_by = #{updatedBy} WHERE id = #{id} AND deleted = false");
        return sql.toString();
    }

    public String searchProductsSql(Map<String, Object> params) {
        StringBuilder sql = new StringBuilder("SELECT * FROM products WHERE deleted = false ");
        addLike(sql, params, "query", "AND (name LIKE CONCAT('%', #{query}, '%') OR description LIKE CONCAT('%', #{query}, '%') OR tags LIKE CONCAT('%', #{query}, '%')) ");
        addEq(sql, params, "categoryId", "AND category_id = #{categoryId} ");
        addGe(sql, params, "minPrice", "AND price >= #{minPrice} ");
        addLe(sql, params, "maxPrice", "AND price <= #{maxPrice} ");
        addLike(sql, params, "location", "AND (city LIKE CONCAT('%', #{location}, '%') OR state LIKE CONCAT('%', #{location}, '%')) ");
        sql.append("AND status = 'ACTIVE' AND visibility = 'PUBLIC' ORDER BY created_at DESC LIMIT #{limit} OFFSET #{offset}");
        return sql.toString();
    }

    public String countSearchResultsSql(Map<String, Object> params) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM products WHERE deleted = false ");
        addLike(sql, params, "query", "AND (name LIKE CONCAT('%', #{query}, '%') OR description LIKE CONCAT('%', #{query}, '%') OR tags LIKE CONCAT('%', #{query}, '%')) ");
        addEq(sql, params, "categoryId", "AND category_id = #{categoryId} ");
        addGe(sql, params, "minPrice", "AND price >= #{minPrice} ");
        addLe(sql, params, "maxPrice", "AND price <= #{maxPrice} ");
        addLike(sql, params, "location", "AND (city LIKE CONCAT('%', #{location}, '%') OR state LIKE CONCAT('%', #{location}, '%')) ");
        sql.append("AND status = 'ACTIVE' AND visibility = 'PUBLIC'");
        return sql.toString();
    }

    public String findByCategoryHierarchySql(Map<String, Object> params) {
        @SuppressWarnings("unchecked") List<Long> ids = (List<Long>) params.get("categoryIds");
        StringBuilder sql = new StringBuilder("SELECT * FROM products WHERE ");
        if (ids != null && !ids.isEmpty()) {
            sql.append("category_id IN (");
            for (int i = 0; i < ids.size(); i++) {
                if (i > 0) sql.append(',');
                sql.append("#{categoryIds[" + i + "]}");
            }
            sql.append(") ");
        } else {
            // empty list should yield no results
            sql.append("1=0 ");
        }
        sql.append("AND deleted = false AND status = 'ACTIVE' AND visibility = 'PUBLIC' ORDER BY created_at DESC LIMIT #{limit} OFFSET #{offset}");
        return sql.toString();
    }

    public String bulkUpdateStatusSql(Map<String, Object> params) {
        @SuppressWarnings("unchecked") List<Long> ids = (List<Long>) params.get("productIds");
        StringBuilder sql = new StringBuilder("UPDATE products SET status = #{status}, updated_at = NOW(), updated_by = #{updatedBy} WHERE ");
        if (ids != null && !ids.isEmpty()) {
            sql.append("id IN (");
            for (int i = 0; i < ids.size(); i++) {
                if (i > 0) sql.append(',');
                sql.append("#{productIds[" + i + "]}");
            }
            sql.append(") AND deleted = false");
        } else {
            sql.append("1=0");
        }
        return sql.toString();
    }

    public String findSimilarProductsSql(Map<String, Object> params) {
        @SuppressWarnings("unchecked") List<String> tags = (List<String>) params.get("tags");
        StringBuilder sql = new StringBuilder("SELECT * FROM products WHERE id != #{productId} AND deleted = false AND status = 'ACTIVE' AND visibility = 'PUBLIC' AND (category_id = #{categoryId} ");
        if (tags != null && !tags.isEmpty()) {
            sql.append(" OR (");
            for (int i = 0; i < tags.size(); i++) {
                if (i > 0) sql.append(" OR ");
                sql.append("tags LIKE CONCAT('%', #{tags[" + i + "]}, '%')");
            }
            sql.append(")");
        }
        sql.append(") ORDER BY CASE WHEN category_id = #{categoryId} THEN 1 ELSE 2 END, average_rating DESC, view_count DESC LIMIT #{limit}");
        return sql.toString();
    }

    public String updatePricingSql(Map<String, Object> params) {
        StringBuilder sql = new StringBuilder("UPDATE products SET price = #{price}");
        if (params.get("minOrderQty") != null) sql.append(", min_order_quantity = #{minOrderQty}");
        if (params.get("maxOrderQty") != null) sql.append(", max_order_quantity = #{maxOrderQty}");
        sql.append(", updated_at = NOW(), updated_by = #{updatedBy} WHERE id = #{id} AND deleted = false");
        return sql.toString();
    }

    private void addLike(StringBuilder sql, Map<String,Object> params, String key, String fragment) {
        Object v = params.get(key);
        if (v instanceof String && !((String) v).isBlank()) sql.append(fragment);
    }
    private void addEq(StringBuilder sql, Map<String,Object> params, String key, String fragment) {
        if (params.get(key) != null) sql.append(fragment);
    }
    private void addGe(StringBuilder sql, Map<String,Object> params, String key, String fragment) {
        if (params.get(key) != null) sql.append(fragment);
    }
    private void addLe(StringBuilder sql, Map<String,Object> params, String key, String fragment) {
        if (params.get(key) != null) sql.append(fragment);
    }
}
