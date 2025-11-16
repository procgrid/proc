package com.procgrid.userservice.mapper.mybatis;

import com.procgrid.userservice.model.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

/**
 * MyBatis mapper interface for User entity
 */
@Mapper
public interface UserMyBatisMapper {
    
    /**
     * Insert a new user
     */
    int insert(User user);
    
    /**
     * Update an existing user
     */
    int update(User user);
    
    /**
     * Find user by ID
     */
    Optional<User> findById(@Param("id") String id);
    
    /**
     * Find user by email
     */
    Optional<User> findByEmail(@Param("email") String email);
    
    /**
     * Find user by phone number
     */
    Optional<User> findByPhone(@Param("phone") String phone);
    
    /**
     * Find user by Keycloak user ID
     */
    Optional<User> findByKeycloakUserId(@Param("keycloakUserId") String keycloakUserId);
    
    /**
     * Find all users with pagination
     */
    List<User> findAll(@Param("offset") int offset, @Param("limit") int limit);
    
    /**
     * Find users by role
     */
    List<User> findByRole(@Param("role") User.UserRole role, 
                          @Param("offset") int offset, 
                          @Param("limit") int limit);
    
    /**
     * Find users by status
     */
    List<User> findByStatus(@Param("status") User.UserStatus status,
                            @Param("offset") int offset, 
                            @Param("limit") int limit);
    
    /**
     * Search users by name or email
     */
    List<User> searchUsers(@Param("searchTerm") String searchTerm,
                           @Param("offset") int offset, 
                           @Param("limit") int limit);
    
    /**
     * Count total users
     */
    long count();
    
    /**
     * Count users by role
     */
    long countByRole(@Param("role") User.UserRole role);
    
    /**
     * Count users by status
     */
    long countByStatus(@Param("status") User.UserStatus status);
    
    /**
     * Update user status
     */
    int updateStatus(@Param("id") String id, @Param("status") User.UserStatus status);
    
    /**
     * Update email verification status
     */
    int updateEmailVerification(@Param("id") String id, 
                               @Param("emailVerified") Boolean emailVerified,
                               @Param("emailVerifiedAt") java.time.LocalDateTime emailVerifiedAt);
    
    /**
     * Update phone verification status
     */
    int updatePhoneVerification(@Param("id") String id, 
                               @Param("phoneVerified") Boolean phoneVerified,
                               @Param("phoneVerifiedAt") java.time.LocalDateTime phoneVerifiedAt);
    
    /**
     * Update last login timestamp
     */
    int updateLastLogin(@Param("id") String id, 
                       @Param("lastLoginAt") java.time.LocalDateTime lastLoginAt);
    
    /**
     * Update password reset token
     */
    int updatePasswordResetToken(@Param("id") String id,
                                @Param("passwordResetToken") String token,
                                @Param("expiresAt") java.time.LocalDateTime expiresAt);
    
    /**
     * Update email verification token
     */
    int updateEmailVerificationToken(@Param("id") String id,
                                   @Param("emailVerificationToken") String token,
                                   @Param("expiresAt") java.time.LocalDateTime expiresAt);
    
    /**
     * Clear password reset token
     */
    int clearPasswordResetToken(@Param("id") String id);
    
    /**
     * Clear email verification token
     */
    int clearEmailVerificationToken(@Param("id") String id);
    
    /**
     * Check if email exists
     */
    boolean existsByEmail(@Param("email") String email);
    
    /**
     * Check if phone exists
     */
    boolean existsByPhone(@Param("phone") String phone);
    
    /**
     * Find user by email verification token
     */
    Optional<User> findByEmailVerificationToken(@Param("token") String token);
    
    /**
     * Soft delete user (update status to inactive)
     */
    int softDelete(@Param("id") String id);
    
    /**
     * Hard delete user (permanently remove from database)
     */
    int delete(@Param("id") String id);
}