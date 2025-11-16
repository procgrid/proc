package com.procgrid.productservice.mapper.mybatis;

import com.procgrid.productservice.model.Product;
import org.apache.ibatis.annotations.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * MyBatis mapper interface for Product operations
 */
@Mapper
public interface ProductMyBatisMapper {
    
    /**
     * Insert a new product
     */
    @Insert({
        "INSERT INTO products (",
        "producer_id, category_id, name, description, summary, variety, grade, origin,",
        "harvest_date, expiry_date, price, price_unit, available_quantity, quantity_unit,",
        "min_order_quantity, status, visibility, certifications, tags, image_urls,",
        "primary_image_url, packaging, storage_requirements, transportation_requirements,",
        "supports_bulk_orders, is_seasonal, season_info, view_count, average_rating,",
        "rating_count, slug, meta_keywords, meta_description, created_at, updated_at,",
        "created_by, updated_by, deleted",
        ") VALUES (",
        "#{producerId}, #{categoryId}, #{name}, #{description}, #{summary}, #{variety},",
        "#{grade}, #{origin}, #{harvestDate}, #{expiryDate}, #{price}, #{priceUnit},",
        "#{availableQuantity}, #{quantityUnit}, #{minOrderQuantity}, #{status}, #{visibility},",
        "#{certifications, typeHandler=com.procgrid.productservice.handler.JsonListTypeHandler},",
        "#{tags, typeHandler=com.procgrid.productservice.handler.JsonListTypeHandler},",
        "#{imageUrls, typeHandler=com.procgrid.productservice.handler.JsonListTypeHandler},",
        "#{primaryImageUrl}, #{packaging}, #{storageRequirements}, #{transportationRequirements},",
        "#{supportsBulkOrders}, #{isSeasonal}, #{seasonInfo}, #{viewCount}, #{averageRating},",
        "#{ratingCount}, #{slug}, #{metaKeywords}, #{metaDescription}, #{createdAt},",
        "#{updatedAt}, #{createdBy}, #{updatedBy}, #{deleted}",
        ")"
    })
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insertProduct(Product product);
    
    /**
     * Update an existing product
     */
    @UpdateProvider(type = ProductSqlProvider.class, method = "buildUpdateProduct")
    int updateProduct(Product product);
    
    /**
     * Count products by category ID
     */
    @Select({
        "SELECT COUNT(*) FROM products",
        "WHERE category_id = #{categoryId} AND deleted = false",
        "AND status = 'ACTIVE' AND visibility = 'PUBLIC'"
    })
    Long countByCategoryId(Long categoryId);
    
    /**
     * Find products by status
     */
    @Select({
        "SELECT * FROM products WHERE status = #{status} AND deleted = false",
        "ORDER BY created_at DESC",
        "LIMIT #{limit} OFFSET #{offset}"
    })
    List<Product> findByStatus(@Param("status") Product.ProductStatus status,
                              @Param("offset") int offset,
                              @Param("limit") int limit);
    
    /**
     * Count products by status
     */
    @Select({
        "SELECT COUNT(*) FROM products WHERE status = #{status} AND deleted = false"
    })
    Long countByStatus(Product.ProductStatus status);
    
    /**
     * Find featured products
     */
    @Select({
        "SELECT * FROM products",
        "WHERE deleted = false AND status = 'ACTIVE' AND visibility = 'PUBLIC'",
        "AND (tags LIKE '%featured%' OR average_rating >= 4.5)",
        "ORDER BY average_rating DESC, view_count DESC",
        "LIMIT #{limit}"
    })
    List<Product> findFeaturedProducts(int limit);
    
    /**
     * Find recent products
     */
    @Select({
        "SELECT * FROM products",
        "WHERE deleted = false AND status = 'ACTIVE' AND visibility = 'PUBLIC'",
        "ORDER BY created_at DESC",
        "LIMIT #{limit}"
    })
    List<Product> findRecentProducts(int limit);
    
    /**
     * Find products by certification
     */
    @Select({
        "<script>",
        "SELECT * FROM products",
        "WHERE deleted = false AND status = 'ACTIVE' AND visibility = 'PUBLIC'",
        "AND (",
        "<foreach collection='certifications' item='cert' separator='OR'>",
        "certifications LIKE CONCAT('%', #{cert}, '%')",
        "</foreach>",
        ")",
        "ORDER BY created_at DESC",
        "LIMIT #{limit} OFFSET #{offset}",
        "</script>"
    })
    List<Product> findByCertification(@Param("certifications") List<String> certifications,
                                     @Param("offset") int offset,
                                     @Param("limit") int limit);
    
    /**
     * Count products by certification
     */
    @Select({
        "<script>",
        "SELECT COUNT(*) FROM products",
        "WHERE deleted = false AND status = 'ACTIVE' AND visibility = 'PUBLIC'",
        "AND (",
        "<foreach collection='certifications' item='cert' separator='OR'>",
        "certifications LIKE CONCAT('%', #{cert}, '%')",
        "</foreach>",
        ")",
        "</script>"
    })
    Long countByCertification(@Param("certifications") List<String> certifications);
    
    /**
     * Update product pricing
     */
    @Update({
        "<script>",
        "UPDATE products SET",
        "price = #{price},",
        "<if test='minOrderQty != null'>min_order_quantity = #{minOrderQty},</if>",
        "<if test='maxOrderQty != null'>max_order_quantity = #{maxOrderQty},</if>",
        "updated_at = NOW(), updated_by = #{updatedBy}",
        "WHERE id = #{id} AND deleted = false",
        "</script>"
    })
    int updatePricing(@Param("id") Long id,
                     @Param("price") BigDecimal price,
                     @Param("minOrderQty") BigDecimal minOrderQty,
                     @Param("maxOrderQty") BigDecimal maxOrderQty,
                     @Param("updatedBy") String updatedBy);
    
    /**
     * Update product location
     */
    @Update({
        "<script>",
        "UPDATE products SET",
        "<if test='city != null'>city = #{city},</if>",
        "<if test='state != null'>state = #{state},</if>",
        "<if test='country != null'>country = #{country},</if>",
        "<if test='zipCode != null'>zip_code = #{zipCode},</if>",
        "<if test='latitude != null'>latitude = #{latitude},</if>",
        "<if test='longitude != null'>longitude = #{longitude},</if>",
        "updated_at = NOW(), updated_by = #{updatedBy}",
        "WHERE id = #{id} AND deleted = false",
        "</script>"
    })
    int updateLocation(@Param("id") Long id,
                      @Param("city") String city,
                      @Param("state") String state,
                      @Param("country") String country,
                      @Param("zipCode") String zipCode,
                      @Param("latitude") BigDecimal latitude,
                      @Param("longitude") BigDecimal longitude,
                      @Param("updatedBy") String updatedBy);
    
    /**
     * Search products with filters
     */
    @Select({
        "<script>",
        "SELECT * FROM products",
        "WHERE deleted = false",
        "<if test='query != null and query != \"\"'>",
        "AND (name LIKE CONCAT('%', #{query}, '%') OR description LIKE CONCAT('%', #{query}, '%') OR tags LIKE CONCAT('%', #{query}, '%'))",
        "</if>",
        "<if test='categoryId != null'>",
        "AND category_id = #{categoryId}",
        "</if>",
        "<if test='minPrice != null'>",
        "AND price &gt;= #{minPrice}",
        "</if>",
        "<if test='maxPrice != null'>",
        "AND price &lt;= #{maxPrice}",
        "</if>",
        "<if test='location != null and location != \"\"'>",
        "AND (city LIKE CONCAT('%', #{location}, '%') OR state LIKE CONCAT('%', #{location}, '%'))",
        "</if>",
        "AND status = 'ACTIVE' AND visibility = 'PUBLIC'",
        "ORDER BY created_at DESC",
        "LIMIT #{limit} OFFSET #{offset}",
        "</script>"
    })
    List<Product> searchProducts(@Param("query") String query,
                                @Param("categoryId") Long categoryId,
                                @Param("minPrice") BigDecimal minPrice,
                                @Param("maxPrice") BigDecimal maxPrice,
                                @Param("location") String location,
                                @Param("offset") int offset,
                                @Param("limit") int limit);
    
    /**
     * Count search results
     */
    @Select({
        "<script>",
        "SELECT COUNT(*) FROM products",
        "WHERE deleted = false",
        "<if test='query != null and query != \"\"'>",
        "AND (name LIKE CONCAT('%', #{query}, '%') OR description LIKE CONCAT('%', #{query}, '%') OR tags LIKE CONCAT('%', #{query}, '%'))",
        "</if>",
        "<if test='categoryId != null'>",
        "AND category_id = #{categoryId}",
        "</if>",
        "<if test='minPrice != null'>",
        "AND price &gt;= #{minPrice}",
        "</if>",
        "<if test='maxPrice != null'>",
        "AND price &lt;= #{maxPrice}",
        "</if>",
        "<if test='location != null and location != \"\"'>",
        "AND (city LIKE CONCAT('%', #{location}, '%') OR state LIKE CONCAT('%', #{location}, '%'))",
        "</if>",
        "AND status = 'ACTIVE' AND visibility = 'PUBLIC'",
        "</script>"
    })
    Long countSearchResults(@Param("query") String query,
                           @Param("categoryId") Long categoryId,
                           @Param("minPrice") BigDecimal minPrice,
                           @Param("maxPrice") BigDecimal maxPrice,
                           @Param("location") String location);
    
    /**
     * Get product performance by date range
     */
    @Select({
        "SELECT",
        "p.id, p.name, p.price,",
        "SUM(o.quantity) as total_sold,",
        "SUM(o.quantity * o.unit_price) as total_revenue,",
        "COUNT(DISTINCT o.id) as order_count,",
        "AVG(r.rating) as avg_rating",
        "FROM products p",
        "LEFT JOIN order_items o ON p.id = o.product_id",
        "LEFT JOIN reviews r ON p.id = r.product_id",
        "WHERE p.producer_id = #{producerId} AND p.deleted = false",
        "AND o.created_at BETWEEN #{startDate} AND #{endDate}",
        "GROUP BY p.id, p.name, p.price",
        "ORDER BY total_revenue DESC"
    })
    List<Map<String, Object>> getProductPerformanceByDateRange(@Param("producerId") Long producerId,
                                                              @Param("startDate") String startDate,
                                                              @Param("endDate") String endDate);
    
    /**
     * Get product view history
     */
    @Select({
        "SELECT",
        "DATE(created_at) as view_date,",
        "COUNT(*) as view_count",
        "FROM product_views",
        "WHERE product_id = #{productId}",
        "AND created_at &gt;= DATE_SUB(NOW(), INTERVAL #{days} DAY)",
        "GROUP BY DATE(created_at)",
        "ORDER BY view_date DESC"
    })
    List<Map<String, Object>> getProductViewHistory(@Param("productId") Long productId,
                                                   @Param("days") int days);
    
    /**
     * Find featured/trending products
     */
    @Select({
        "SELECT * FROM products",
        "WHERE deleted = false AND status = 'ACTIVE' AND visibility = 'PUBLIC'",
        "ORDER BY view_count DESC, average_rating DESC",
        "LIMIT #{limit}"
    })
    List<Product> findTrendingProducts(int limit);
    
    /**
     * Find products expiring soon
     */
    @Select({
        "SELECT * FROM products",
        "WHERE deleted = false AND status = 'ACTIVE'",
        "AND expiry_date IS NOT NULL",
        "AND expiry_date BETWEEN NOW() AND DATE_ADD(NOW(), INTERVAL #{days} DAY)",
        "ORDER BY expiry_date ASC"
    })
    List<Product> findProductsExpiringSoon(int days);
    
    /**
     * Find low stock products
     */
    @Select({
        "SELECT p.* FROM products p",
        "INNER JOIN inventory i ON p.id = i.product_id",
        "WHERE p.deleted = false AND p.status = 'ACTIVE'",
        "AND p.producer_id = #{producerId}",
        "AND i.status = 'LOW_STOCK'",
        "ORDER BY i.available_quantity ASC"
    })
    List<Product> findLowStockProducts(Long producerId);
    
    /**
     * Update product status
     */
    @Update({
        "UPDATE products SET status = #{status}, updated_at = NOW(), updated_by = #{updatedBy}",
        "WHERE id = #{id} AND deleted = false"
    })
    int updateProductStatus(@Param("id") Long id,
                           @Param("status") Product.ProductStatus status,
                           @Param("updatedBy") String updatedBy);
    
    /**
     * Update product visibility
     */
    @Update({
        "UPDATE products SET visibility = #{visibility}, updated_at = NOW(), updated_by = #{updatedBy}",
        "WHERE id = #{id} AND deleted = false"
    })
    int updateProductVisibility(@Param("id") Long id,
                               @Param("visibility") Product.ProductVisibility visibility,
                               @Param("updatedBy") String updatedBy);
    
    /**
     * Update product view count
     */
    @Update({
        "UPDATE products SET view_count = view_count + 1",
        "WHERE id = #{id} AND deleted = false"
    })
    void incrementViewCount(Long id);
    
    /**
     * Update product rating
     */
    @Update({
        "UPDATE products SET average_rating = #{rating}, rating_count = #{count}, updated_at = NOW()",
        "WHERE id = #{id} AND deleted = false"
    })
    void updateRating(@Param("id") Long id,
                     @Param("rating") BigDecimal rating,
                     @Param("count") Integer count);
    
    /**
     * Update available quantity
     */
    @Update({
        "UPDATE products SET available_quantity = #{quantity}, updated_at = NOW(), updated_by = #{updatedBy}",
        "WHERE id = #{id} AND deleted = false"
    })
    int updateAvailableQuantity(@Param("id") Long id,
                               @Param("quantity") BigDecimal quantity,
                               @Param("updatedBy") String updatedBy);
    
    /**
     * Soft delete product
     */
    @Update({
        "UPDATE products SET deleted = true, updated_at = NOW(), updated_by = #{updatedBy}",
        "WHERE id = #{id} AND producer_id = #{producerId}"
    })
    int deleteProduct(@Param("id") Long id,
                     @Param("producerId") Long producerId,
                     @Param("updatedBy") String updatedBy);
    
    /**
     * Check if product exists and belongs to producer
     */
    @Select({
        "SELECT COUNT(*) FROM products",
        "WHERE id = #{productId} AND producer_id = #{producerId} AND deleted = false"
    })
    boolean existsByIdAndProducerId(@Param("productId") Long productId,
                                   @Param("producerId") Long producerId);
    
    /**
     * Check if slug exists for different product
     */
    @Select({
        "SELECT COUNT(*) FROM products",
        "WHERE slug = #{slug} AND id != #{productId} AND deleted = false"
    })
    boolean existsBySlugAndNotId(@Param("slug") String slug, @Param("productId") Long productId);
    
    
    /**
     * Find products by category hierarchy
     */
    @Select({
        "<script>",
        "SELECT * FROM products",
        "WHERE category_id IN",
        "<foreach collection='categoryIds' item='catId' open='(' separator=',' close=')'>",
        "#{catId}",
        "</foreach>",
        "AND deleted = false AND status = 'ACTIVE' AND visibility = 'PUBLIC'",
        "ORDER BY created_at DESC",
        "LIMIT #{limit} OFFSET #{offset}",
        "</script>"
    })
    List<Product> findByCategoryHierarchy(@Param("categoryIds") List<Long> categoryIds,
                                          @Param("offset") int offset,
                                          @Param("limit") int limit);
    
    /**
     * Get product statistics for producer
     */
    @Select({
        "SELECT",
        "COUNT(*) as total_products,",
        "SUM(CASE WHEN status = 'ACTIVE' THEN 1 ELSE 0 END) as active_products,",
        "SUM(CASE WHEN status = 'INACTIVE' THEN 1 ELSE 0 END) as inactive_products,",
        "SUM(CASE WHEN status = 'OUT_OF_STOCK' THEN 1 ELSE 0 END) as out_of_stock_products,",
        "AVG(price) as avg_price,",
        "SUM(view_count) as total_views,",
        "AVG(average_rating) as avg_rating",
        "FROM products",
        "WHERE producer_id = #{producerId} AND deleted = false"
    })
    Map<String, Object> getProducerProductStats(Long producerId);
    
    /**
     * Bulk update product status
     */
    @Update({
        "<script>",
        "UPDATE products SET",
        "status = #{status},",
        "updated_at = NOW(),",
        "updated_by = #{updatedBy}",
        "WHERE id IN",
        "<foreach collection='productIds' item='productId' open='(' separator=',' close=')'>",
        "#{productId}",
        "</foreach>",
        "AND deleted = false",
        "</script>"
    })
    int bulkUpdateStatus(@Param("productIds") List<Long> productIds,
                        @Param("status") Product.ProductStatus status,
                        @Param("updatedBy") String updatedBy);
    
    /**
     * Find similar products
     */
    @Select({
        "<script>",
        "SELECT * FROM products",
        "WHERE id != #{productId} AND deleted = false",
        "AND status = 'ACTIVE' AND visibility = 'PUBLIC'",
        "AND (",
        "category_id = #{categoryId}",
        "<if test='tags != null and tags.size() &gt; 0'>",
        "OR (",
        "<foreach collection='tags' item='tag' separator='OR'>",
        "tags LIKE CONCAT('%', #{tag}, '%')",
        "</foreach>",
        ")",
        "</if>",
        ")",
        "ORDER BY",
        "CASE WHEN category_id = #{categoryId} THEN 1 ELSE 2 END,",
        "average_rating DESC, view_count DESC",
        "LIMIT #{limit}",
        "</script>"
    })
    List<Product> findSimilarProducts(@Param("productId") Long productId,
                                     @Param("categoryId") Long categoryId,
                                     @Param("tags") List<String> tags,
                                     @Param("limit") int limit);

    /**
     * Find product by ID
     */
    @Select({
        "SELECT * FROM products WHERE id = #{id} AND deleted = false"
    })
    Product findById(Long id);

    /**
     * Find product by slug
     */
    @Select({
        "SELECT * FROM products WHERE slug = #{slug} AND deleted = false"
    })
    Product findBySlug(String slug);

    /**
     * Find products by producer ID with pagination
     */
    @Select({
        "SELECT * FROM products WHERE producer_id = #{producerId} AND deleted = false",
        "ORDER BY created_at DESC",
        "LIMIT #{limit} OFFSET #{offset}"
    })
    List<Product> findByProducerId(@Param("producerId") Long producerId,
                                  @Param("offset") int offset,
                                  @Param("limit") int limit);

    /**
     * Count products by producer ID
     */
    @Select({
        "SELECT COUNT(*) FROM products WHERE producer_id = #{producerId} AND deleted = false"
    })
    Long countByProducerId(Long producerId);

    /**
     * Find products by category ID with pagination
     */
    @Select({
        "SELECT * FROM products WHERE category_id = #{categoryId} AND deleted = false",
        "AND status = 'ACTIVE' AND visibility = 'PUBLIC'",
        "ORDER BY created_at DESC",
        "LIMIT #{limit} OFFSET #{offset}"
    })
    List<Product> findByCategoryId(@Param("categoryId") Long categoryId,
                                  @Param("offset") int offset,
                                  @Param("limit") int limit);

    /**
     * Get search suggestions
     */
    @Select({
        "SELECT DISTINCT name FROM products",
        "WHERE name LIKE CONCAT('%', #{query}, '%')",
        "AND deleted = false AND status = 'ACTIVE'",
        "ORDER BY name",
        "LIMIT #{limit}"
    })
    List<String> getSearchSuggestions(@Param("query") String query, @Param("limit") int limit);

    /**
     * Find product by SKU
     */
    @Select({
        "SELECT * FROM products WHERE sku = #{sku} AND deleted = false"
    })
    Product findBySku(String sku);
}