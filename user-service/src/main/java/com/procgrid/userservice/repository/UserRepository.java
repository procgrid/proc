package com.procgrid.userservice.repository;

import com.procgrid.userservice.mapper.mybatis.UserMyBatisMapper;
import com.procgrid.userservice.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository layer for User entity
 * Wraps MyBatis mapper with additional business logic and caching
 */
@Repository
@RequiredArgsConstructor
@Slf4j
public class UserRepository {
    
    private final UserMyBatisMapper userMapper;
    
    /**
     * Create a new user
     */
    @CacheEvict(value = {"users", "userStats"}, allEntries = true)
    public User save(User user) {
        log.debug("Saving user: {}", user.getEmail());
        
        if (user.getId() == null) {
            // Generate UUID for new user
            user.setId(java.util.UUID.randomUUID().toString());
            user.setCreatedAt(LocalDateTime.now());
            user.setUpdatedAt(LocalDateTime.now());
            
            int result = userMapper.insert(user);
            if (result > 0) {
                log.info("Successfully created user: {}", user.getId());
                return user;
            } else {
                throw new RuntimeException("Failed to create user");
            }
        } else {
            // Update existing user
            user.setUpdatedAt(LocalDateTime.now());
            int result = userMapper.update(user);
            if (result > 0) {
                log.info("Successfully updated user: {}", user.getId());
                return user;
            } else {
                throw new RuntimeException("Failed to update user");
            }
        }
    }
    
    /**
     * Find user by ID with caching
     */
    @Cacheable(value = "users", key = "#id")
    public Optional<User> findById(String id) {
        log.debug("Finding user by ID: {}", id);
        return userMapper.findById(id);
    }
    
    /**
     * Find user by email
     */
    @Cacheable(value = "users", key = "'email:' + #email")
    public Optional<User> findByEmail(String email) {
        log.debug("Finding user by email: {}", email);
        return userMapper.findByEmail(email);
    }
    
    /**
     * Find user by phone number
     */
    @Cacheable(value = "users", key = "'phone:' + #phone")
    public Optional<User> findByPhone(String phone) {
        log.debug("Finding user by phone: {}", phone);
        return userMapper.findByPhone(phone);
    }
    
    /**
     * Find user by Keycloak user ID
     */
    @Cacheable(value = "users", key = "'keycloak:' + #keycloakUserId")
    public Optional<User> findByKeycloakUserId(String keycloakUserId) {
        log.debug("Finding user by Keycloak ID: {}", keycloakUserId);
        return userMapper.findByKeycloakUserId(keycloakUserId);
    }
    
    /**
     * Find all users with pagination
     */
    public List<User> findAll(int page, int size) {
        log.debug("Finding all users - page: {}, size: {}", page, size);
        int offset = page * size;
        return userMapper.findAll(offset, size);
    }
    
    /**
     * Find users by role with pagination
     */
    public List<User> findByRole(User.UserRole role, int page, int size) {
        log.debug("Finding users by role: {} - page: {}, size: {}", role, page, size);
        int offset = page * size;
        return userMapper.findByRole(role, offset, size);
    }
    
    /**
     * Find users by status with pagination
     */
    public List<User> findByStatus(User.UserStatus status, int page, int size) {
        log.debug("Finding users by status: {} - page: {}, size: {}", status, page, size);
        int offset = page * size;
        return userMapper.findByStatus(status, offset, size);
    }
    
    /**
     * Search users by name or email
     */
    public List<User> searchUsers(String searchTerm, int page, int size) {
        log.debug("Searching users with term: {} - page: {}, size: {}", searchTerm, page, size);
        int offset = page * size;
        return userMapper.searchUsers(searchTerm, offset, size);
    }
    
    /**
     * Count total users
     */
    @Cacheable(value = "userStats", key = "'totalUsers'")
    public long count() {
        log.debug("Counting total users");
        return userMapper.count();
    }
    
    /**
     * Count users by role
     */
    @Cacheable(value = "userStats", key = "'roleCount:' + #role")
    public long countByRole(User.UserRole role) {
        log.debug("Counting users by role: {}", role);
        return userMapper.countByRole(role);
    }
    
    /**
     * Count users by status
     */
    @Cacheable(value = "userStats", key = "'statusCount:' + #status")
    public long countByStatus(User.UserStatus status) {
        log.debug("Counting users by status: {}", status);
        return userMapper.countByStatus(status);
    }
    
    /**
     * Update user status
     */
    @CacheEvict(value = {"users", "userStats"}, allEntries = true)
    public boolean updateStatus(String id, User.UserStatus status) {
        log.info("Updating user status: {} to {}", id, status);
        int result = userMapper.updateStatus(id, status);
        return result > 0;
    }
    
    /**
     * Update email verification status
     */
    @CacheEvict(value = "users", key = "#id")
    public boolean updateEmailVerification(String id, Boolean emailVerified) {
        log.info("Updating email verification for user: {} to {}", id, emailVerified);
        LocalDateTime verifiedAt = emailVerified ? LocalDateTime.now() : null;
        int result = userMapper.updateEmailVerification(id, emailVerified, verifiedAt);
        return result > 0;
    }
    
    /**
     * Update phone verification status
     */
    @CacheEvict(value = "users", key = "#id")
    public boolean updatePhoneVerification(String id, Boolean phoneVerified) {
        log.info("Updating phone verification for user: {} to {}", id, phoneVerified);
        LocalDateTime verifiedAt = phoneVerified ? LocalDateTime.now() : null;
        int result = userMapper.updatePhoneVerification(id, phoneVerified, verifiedAt);
        return result > 0;
    }
    
    /**
     * Update last login timestamp
     */
    @CacheEvict(value = "users", key = "#id")
    public boolean updateLastLogin(String id) {
        log.debug("Updating last login for user: {}", id);
        int result = userMapper.updateLastLogin(id, LocalDateTime.now());
        return result > 0;
    }
    
    /**
     * Set password reset token
     */
    @CacheEvict(value = "users", key = "#id")
    public boolean setPasswordResetToken(String id, String token, LocalDateTime expiresAt) {
        log.info("Setting password reset token for user: {}", id);
        int result = userMapper.updatePasswordResetToken(id, token, expiresAt);
        return result > 0;
    }
    
    /**
     * Set email verification token
     */
    @CacheEvict(value = "users", key = "#id")
    public boolean setEmailVerificationToken(String id, String token, LocalDateTime expiresAt) {
        log.info("Setting email verification token for user: {}", id);
        int result = userMapper.updateEmailVerificationToken(id, token, expiresAt);
        return result > 0;
    }
    
    /**
     * Clear password reset token
     */
    @CacheEvict(value = "users", key = "#id")
    public boolean clearPasswordResetToken(String id) {
        log.info("Clearing password reset token for user: {}", id);
        int result = userMapper.clearPasswordResetToken(id);
        return result > 0;
    }
    
    /**
     * Clear email verification token
     */
    @CacheEvict(value = "users", key = "#id")
    public boolean clearEmailVerificationToken(String id) {
        log.info("Clearing email verification token for user: {}", id);
        int result = userMapper.clearEmailVerificationToken(id);
        return result > 0;
    }
    
    /**
     * Check if email already exists
     */
    public boolean existsByEmail(String email) {
        log.debug("Checking if email exists: {}", email);
        return userMapper.existsByEmail(email);
    }
    
    /**
     * Check if phone already exists
     */
    public boolean existsByPhone(String phone) {
        log.debug("Checking if phone exists: {}", phone);
        return userMapper.existsByPhone(phone);
    }
    
    /**
     * Soft delete user (set status to INACTIVE)
     */
    @CacheEvict(value = {"users", "userStats"}, allEntries = true)
    public boolean softDelete(String id) {
        log.info("Soft deleting user: {}", id);
        int result = userMapper.softDelete(id);
        return result > 0;
    }
    
    /**
     * Hard delete user (permanent removal)
     */
    @CacheEvict(value = {"users", "userStats"}, allEntries = true)
    public boolean delete(String id) {
        log.warn("Hard deleting user: {}", id);
        int result = userMapper.delete(id);
        return result > 0;
    }
    
    /**
     * Check if user exists by ID
     */
    public boolean existsById(String id) {
        log.debug("Checking if user exists by ID: {}", id);
        return findById(id).isPresent();
    }
    
    /**
     * Find user by email verification token
     */
    public Optional<User> findByEmailVerificationToken(String token) {
        log.debug("Finding user by email verification token");
        return userMapper.findByEmailVerificationToken(token);
    }
}