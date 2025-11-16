package com.procgrid.productservice.mapper.mybatis;

import com.procgrid.productservice.model.Category;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

/**
 * MyBatis mapper interface for Category operations
 */
@Mapper
public interface CategoryMyBatisMapper {
    
    /**
     * Insert a new category
     */
    @Insert({
        "INSERT INTO categories (",
        "parent_id, name, description, slug, icon, image_url, sort_order, level,",
        "active, featured, product_count, tags, meta_keywords, meta_description,",
        "category_path, breadcrumb, created_at, updated_at, created_by, updated_by, deleted",
        ") VALUES (",
        "#{parentId}, #{name}, #{description}, #{slug}, #{icon}, #{imageUrl}, #{sortOrder},",
        "#{level}, #{active}, #{featured}, #{productCount},",
        "#{tags, typeHandler=com.procgrid.productservice.handler.JsonListTypeHandler},",
        "#{metaKeywords}, #{metaDescription}, #{categoryPath},",
        "#{breadcrumb, typeHandler=com.procgrid.productservice.handler.JsonListTypeHandler},",
        "#{createdAt}, #{updatedAt}, #{createdBy}, #{updatedBy}, #{deleted}",
        ")"
    })
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insertCategory(Category category);
    
    /**
     * Update an existing category
     */
    @Update({
        "<script>",
        "UPDATE categories",
        "SET",
        "  <if test='parentId != null'>parent_id = #{parentId},</if>",
        "  <if test='name != null'>name = #{name},</if>",
        "  <if test='description != null'>description = #{description},</if>",
        "  <if test='slug != null'>slug = #{slug},</if>",
        "  <if test='icon != null'>icon = #{icon},</if>",
        "  <if test='imageUrl != null'>image_url = #{imageUrl},</if>",
        "  <if test='sortOrder != null'>sort_order = #{sortOrder},</if>",
        "  <if test='level != null'>level = #{level},</if>",
        "  <if test='active != null'>active = #{active},</if>",
        "  <if test='featured != null'>featured = #{featured},</if>",
        "  <if test='productCount != null'>product_count = #{productCount},</if>",
        "  <if test='tags != null'>tags = #{tags, typeHandler=com.procgrid.productservice.handler.JsonListTypeHandler},</if>",
        "  <if test='metaKeywords != null'>meta_keywords = #{metaKeywords},</if>",
        "  <if test='metaDescription != null'>meta_description = #{metaDescription},</if>",
        "  <if test='categoryPath != null'>category_path = #{categoryPath},</if>",
        "  <if test='breadcrumb != null'>breadcrumb = #{breadcrumb, typeHandler=com.procgrid.productservice.handler.JsonListTypeHandler},</if>",
        "  updated_at = #{updatedAt},",
        "  updated_by = #{updatedBy}",
        "WHERE id = #{id} AND deleted = false",
        "</script>"
    })
    int updateCategory(Category category);
    
    /**
     * Find category by ID
     */
    @Select({
        "SELECT * FROM categories WHERE id = #{id} AND deleted = false"
    })
    Category findById(Long id);
    
    /**
     * Find category by slug
     */
    @Select({
        "SELECT * FROM categories WHERE slug = #{slug} AND deleted = false"
    })
    Category findBySlug(String slug);
    
    /**
     * Find all root categories (parent_id is null)
     */
    @Select({
        "SELECT * FROM categories",
        "WHERE parent_id IS NULL AND deleted = false AND active = true",
        "ORDER BY sort_order ASC, name ASC"
    })
    List<Category> findRootCategories();
    
    /**
     * Find child categories by parent ID
     */
    @Select({
        "SELECT * FROM categories",
        "WHERE parent_id = #{parentId} AND deleted = false AND active = true",
        "ORDER BY sort_order ASC, name ASC"
    })
    List<Category> findByParentId(Long parentId);
    
    /**
     * Find categories by level
     */
    @Select({
        "SELECT * FROM categories",
        "WHERE level = #{level} AND deleted = false AND active = true",
        "ORDER BY sort_order ASC, name ASC"
    })
    List<Category> findByLevel(Integer level);
    
    /**
     * Find all categories (hierarchical)
     */
    @Select({
        "SELECT * FROM categories WHERE deleted = false",
        "ORDER BY level ASC, sort_order ASC, name ASC"
    })
    List<Category> findAllCategories();
    
    /**
     * Find featured categories
     */
    @Select({
        "SELECT * FROM categories",
        "WHERE featured = true AND deleted = false AND active = true",
        "ORDER BY sort_order ASC, name ASC",
        "LIMIT #{limit}"
    })
    List<Category> findFeaturedCategories(int limit);
    
    /**
     * Find categories with products
     */
    @Select({
        "SELECT * FROM categories",
        "WHERE product_count > 0 AND deleted = false AND active = true",
        "ORDER BY product_count DESC, name ASC"
    })
    List<Category> findCategoriesWithProducts();
    
    /**
     * Search categories by name
     */
    @Select({
        "SELECT * FROM categories",
        "WHERE name LIKE CONCAT('%', #{query}, '%')",
        "AND deleted = false AND active = true",
        "ORDER BY name ASC",
        "LIMIT #{limit}"
    })
    List<Category> searchCategoriesByName(@Param("query") String query, @Param("limit") int limit);
    
    /**
     * Get category hierarchy path
     */
    @Select({
        "<script>",
        "WITH RECURSIVE category_hierarchy AS (",
        "  SELECT id, parent_id, name, description, slug, icon, image_url,",
        "         sort_order, level, active, featured, product_count, tags,",
        "         meta_keywords, meta_description, category_path, breadcrumb,",
        "         created_at, updated_at, created_by, updated_by, deleted",
        "  FROM categories WHERE id = #{categoryId} AND deleted = false",
        "  UNION ALL",
        "  SELECT c.id, c.parent_id, c.name, c.description, c.slug, c.icon, c.image_url,",
        "         c.sort_order, c.level, c.active, c.featured, c.product_count, c.tags,",
        "         c.meta_keywords, c.meta_description, c.category_path, c.breadcrumb,",
        "         c.created_at, c.updated_at, c.created_by, c.updated_by, c.deleted",
        "  FROM categories c",
        "  INNER JOIN category_hierarchy ch ON c.parent_id = ch.id AND c.deleted = false",
        ")",
        "SELECT * FROM category_hierarchy ORDER BY level ASC",
        "</script>"
    })
    List<Category> getCategoryHierarchy(Long categoryId);
    
    /**
     * Update product count for category
     */
    @Update({
        "UPDATE categories SET product_count = #{count}, updated_at = NOW()",
        "WHERE id = #{categoryId} AND deleted = false"
    })
    void updateProductCount(@Param("categoryId") Long categoryId, @Param("count") Long count);
    
    /**
     * Update category level and path
     */
    @Update({
        "UPDATE categories SET level = #{level}, category_path = #{categoryPath},",
        "breadcrumb = #{breadcrumb, typeHandler=com.procgrid.productservice.handler.JsonListTypeHandler},",
        "updated_at = NOW(), updated_by = #{updatedBy}",
        "WHERE id = #{id} AND deleted = false"
    })
    void updateCategoryHierarchy(@Param("id") Long id,
                                @Param("level") Integer level,
                                @Param("categoryPath") String categoryPath,
                                @Param("breadcrumb") List<String> breadcrumb,
                                @Param("updatedBy") String updatedBy);
    
    /**
     * Check if category has children
     */
    @Select({
        "SELECT COUNT(*) > 0 FROM categories",
        "WHERE parent_id = #{categoryId} AND deleted = false"
    })
    boolean hasChildren(Long categoryId);
    
    /**
     * Check if category has products
     */
    @Select({
        "SELECT COUNT(*) > 0 FROM products",
        "WHERE category_id = #{categoryId} AND deleted = false"
    })
    boolean hasProducts(Long categoryId);
    
    /**
     * Check if slug exists for different category
     */
    @Select({
        "SELECT COUNT(*) FROM categories",
        "WHERE slug = #{slug} AND id != #{categoryId} AND deleted = false"
    })
    boolean existsBySlugAndNotId(@Param("slug") String slug, @Param("categoryId") Long categoryId);
    
    /**
     * Soft delete category
     */
    @Update({
        "UPDATE categories SET deleted = true, updated_at = NOW(), updated_by = #{updatedBy}",
        "WHERE id = #{id}"
    })
    int deleteCategory(@Param("id") Long id, @Param("updatedBy") String updatedBy);
    
    /**
     * Update category status (active/inactive)
     */
    @Update({
        "UPDATE categories SET active = #{active}, updated_at = NOW(), updated_by = #{updatedBy}",
        "WHERE id = #{id} AND deleted = false"
    })
    int updateCategoryStatus(@Param("id") Long id,
                            @Param("active") Boolean active,
                            @Param("updatedBy") String updatedBy);
    
    /**
     * Update featured status
     */
    @Update({
        "UPDATE categories SET featured = #{featured}, updated_at = NOW(), updated_by = #{updatedBy}",
        "WHERE id = #{id} AND deleted = false"
    })
    int updateFeaturedStatus(@Param("id") Long id,
                            @Param("featured") Boolean featured,
                            @Param("updatedBy") String updatedBy);
    
    /**
     * Get category statistics
     */
    @Select({
        "SELECT",
        "COUNT(*) as totalCategories,",
        "COUNT(CASE WHEN active = true THEN 1 END) as activeCategories,",
        "COUNT(CASE WHEN featured = true THEN 1 END) as featuredCategories,",
        "COUNT(CASE WHEN product_count > 0 THEN 1 END) as categoriesWithProducts",
        "FROM categories WHERE deleted = false"
    })
    Map<String, Object> getCategoryStatistics();
    
    /**
     * Find categories for breadcrumb
     */
    @Select({
        "WITH RECURSIVE category_path AS (",
        "  SELECT id, parent_id, name, level FROM categories WHERE id = #{categoryId}",
        "  UNION ALL",
        "  SELECT c.id, c.parent_id, c.name, c.level",
        "  FROM categories c",
        "  INNER JOIN category_path cp ON c.id = cp.parent_id",
        ")",
        "SELECT * FROM category_path ORDER BY level ASC"
    })
    List<Category> findCategoryPath(Long categoryId);
    
    /**
     * Find all subcategories (recursive)
     */
    @Select({
        "<script>",
        "WITH RECURSIVE subcategories AS (",
        "  SELECT id FROM categories WHERE parent_id = #{categoryId} AND deleted = false",
        "  UNION ALL",
        "  SELECT c.id",
        "  FROM categories c",
        "  INNER JOIN subcategories s ON c.parent_id = s.id AND c.deleted = false",
        ")",
        "SELECT id FROM subcategories",
        "</script>"
    })
    List<Long> findAllSubcategoryIds(Long categoryId);
    
    /**
     * Check if category name exists for same parent
     */
    @Select({
        "SELECT COUNT(*) > 0 FROM categories",
        "WHERE name = #{name}",
        "AND parent_id = #{parentId}",
        "AND deleted = false"
    })
    boolean existsByNameAndParentId(@Param("name") String name, @Param("parentId") Long parentId);
    
    /**
     * Update children count for category
     */
    @Update({
        "UPDATE categories",
        "SET children_count = #{count}",
        "WHERE id = #{categoryId}"
    })
    void updateChildrenCount(@Param("categoryId") Long categoryId, @Param("count") int count);

    /**
     * Get category breadcrumbs
     */
    @Select({
        "WITH RECURSIVE breadcrumbs(id, name, parent_id, level) AS (",
        "SELECT id, name, parent_id, 0 FROM categories WHERE id = #{categoryId}",
        "UNION ALL",
        "SELECT c.id, c.name, c.parent_id, b.level + 1",
        "FROM categories c INNER JOIN breadcrumbs b ON c.id = b.parent_id",
        ")",
        "SELECT name FROM breadcrumbs ORDER BY level DESC"
    })
    List<String> getCategoryBreadcrumbs(Long categoryId);

    /**
     * Find all categories with pagination
     */
    @Select({
        "SELECT * FROM categories WHERE deleted = false",
        "ORDER BY parent_id NULLS FIRST, display_order, name",
        "LIMIT #{limit} OFFSET #{offset}"
    })
    List<Category> findAllWithPagination(@Param("offset") int offset, @Param("limit") int limit);

    /**
     * Count all categories
     */
    @Select("SELECT COUNT(*) FROM categories WHERE deleted = false")
    Long countAll();

    /**
     * Find active categories with pagination
     */
    @Select({
        "SELECT * FROM categories WHERE deleted = false AND active = true",
        "ORDER BY parent_id NULLS FIRST, display_order, name",
        "LIMIT #{limit} OFFSET #{offset}"
    })
    List<Category> findActiveWithPagination(@Param("offset") int offset, @Param("limit") int limit);

    /**
     * Find leaf categories (categories without children)
     */
    @Select({
        "SELECT * FROM categories",
        "WHERE deleted = false AND active = true",
        "AND id NOT IN (SELECT DISTINCT parent_id FROM categories WHERE parent_id IS NOT NULL AND deleted = false)",
        "ORDER BY name"
    })
    List<Category> findLeafCategories();

    /**
     * Find popular categories based on product count
     */
    @Select({
        "SELECT c.*, COUNT(p.id) as product_count",
        "FROM categories c",
        "LEFT JOIN products p ON c.id = p.category_id AND p.deleted = false",
        "WHERE c.deleted = false AND c.active = true",
        "GROUP BY c.id",
        "ORDER BY product_count DESC",
        "LIMIT #{limit}"
    })
    List<Category> findPopularCategories(@Param("limit") int limit);

    /**
     * Update category status
     */
    @Update({
        "UPDATE categories SET active = #{active}, updated_by = #{updatedBy}, updated_at = NOW()",
        "WHERE id = #{categoryId} AND deleted = false"
    })
    int updateStatus(@Param("categoryId") Long categoryId,
                    @Param("active") boolean active,
                    @Param("updatedBy") String updatedBy);

    /**
     * Move category to new parent
     */
    @Update({
        "UPDATE categories SET",
        "parent_id = #{newParentId},",
        "display_order = #{displayOrder},",
        "updated_by = #{updatedBy},",
        "updated_at = NOW()",
        "WHERE id = #{categoryId} AND deleted = false"
    })
    int moveCategory(@Param("categoryId") Long categoryId,
                    @Param("newParentId") Long newParentId,
                    @Param("displayOrder") int displayOrder,
                    @Param("updatedBy") String updatedBy,
                    @Param("reason") String reason);

    /**
     * Get category statistics
     */
    @Select({
        "SELECT",
        "COUNT(CASE WHEN p.status = 'ACTIVE' THEN 1 END) as active_products,",
        "COUNT(CASE WHEN p.status = 'INACTIVE' THEN 1 END) as inactive_products,",
        "COUNT(p.id) as total_products,",
        "AVG(p.price) as avg_price",
        "FROM products p",
        "WHERE p.category_id = #{categoryId} AND p.deleted = false"
    })
    Map<String, Object> getCategoryStats(Long categoryId);

    /**
     * Rebuild category hierarchy (placeholder for complex operation)
     */
    @Update("UPDATE categories SET path = CONCAT('/', id) WHERE parent_id IS NULL")
    void rebuildHierarchy();

    /**
     * Find active children categories
     */
    @Select({
        "SELECT * FROM categories",
        "WHERE parent_id = #{parentId} AND deleted = false AND active = true",
        "ORDER BY display_order, name"
    })
    List<Category> findActiveChildren(Long parentId);

    /**
     * Get active product count for category
     */
    @Select({
        "SELECT COUNT(*) FROM products",
        "WHERE category_id = #{categoryId} AND deleted = false AND status = 'ACTIVE'"
    })
    Long getActiveProductCount(Long categoryId);

    /**
     * Get category descendants (all children recursively)
     */
    @Select({
        "WITH RECURSIVE descendants(id) AS (",
        "SELECT id FROM categories WHERE parent_id = #{categoryId}",
        "UNION ALL",
        "SELECT c.id FROM categories c INNER JOIN descendants d ON c.parent_id = d.id",
        ")",
        "SELECT c.* FROM categories c INNER JOIN descendants d ON c.id = d.id",
        "WHERE c.deleted = false"
    })
    List<Category> getCategoryDescendants(Long categoryId);

    /**
     * Get children count for category
     */
    @Select({
        "SELECT COUNT(*) FROM categories",
        "WHERE parent_id = #{categoryId} AND deleted = false"
    })
    Long getChildrenCount(Long categoryId);

    /**
     * Get total product count for category
     */
    @Select({
        "SELECT COUNT(*) FROM products",
        "WHERE category_id = #{categoryId} AND deleted = false"
    })
    Long getProductCount(Long categoryId);

    /**
     * Deactivate child categories
     */
    @Update({
        "UPDATE categories SET active = false, updated_by = #{updatedBy}, updated_at = NOW()",
        "WHERE parent_id = #{categoryId} AND deleted = false"
    })
    void deactivateChildCategories(@Param("categoryId") Long categoryId, @Param("updatedBy") String updatedBy);

    /**
     * Update category path
     */
    @Update({
        "UPDATE categories SET",
        "path = #{path},",
        "level = #{level},",
        "updated_by = #{updatedBy},",
        "updated_at = NOW()",
        "WHERE id = #{categoryId}"
    })
    void updateCategoryPath(@Param("categoryId") Long categoryId,
                           @Param("path") String path,
                           @Param("level") int level,
                           @Param("updatedBy") String updatedBy);

    /**
     * Check if slug exists for same parent
     */
    @Select({
        "SELECT COUNT(*) > 0 FROM categories",
        "WHERE slug = #{slug} AND parent_id = #{parentId} AND deleted = false"
    })
    boolean existsBySlugAndParentId(@Param("slug") String slug, @Param("parentId") Long parentId);
}