package com.procgrid.userservice.service;

import com.procgrid.userservice.dto.request.ProducerRegistrationRequest;
import com.procgrid.userservice.dto.request.BuyerRegistrationRequest;
import com.procgrid.userservice.dto.request.UserUpdateRequest;
import com.procgrid.userservice.dto.response.UserResponse;
import com.procgrid.userservice.dto.response.ProducerResponse;
import com.procgrid.userservice.dto.response.BuyerResponse;
import com.procgrid.userservice.dto.response.UserStatsResponse;
import com.procgrid.userservice.mapper.UserMapper;
import com.procgrid.userservice.model.User;
import com.procgrid.userservice.model.Producer;
import com.procgrid.userservice.model.Buyer;
import com.procgrid.userservice.repository.UserRepository;
import com.procgrid.userservice.repository.ProducerRepository;
import com.procgrid.userservice.repository.BuyerRepository;
import com.procgrid.common.exception.BusinessException;
import com.procgrid.common.exception.ResourceNotFoundException;
import com.procgrid.common.model.ApiResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service layer for User management operations
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserService {
    
    private final UserRepository userRepository;
    private final ProducerRepository producerRepository;
    private final BuyerRepository buyerRepository;
    private final UserMapper userMapper;
    private final EmailService emailService;
    private final KeycloakService keycloakService;
    
    /**
     * Register a new producer
     */
    public ApiResponse<ProducerResponse> registerProducer(ProducerRegistrationRequest request) {
        log.info("Registering new producer with email: {}", request.getEmail());
        
        try {
            // Validate email uniqueness
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new BusinessException("User with this email already exists");
            }
            
            // Validate phone uniqueness (if provided)
            if (request.getPhone() != null && userRepository.existsByPhone(request.getPhone())) {
                throw new BusinessException("User with this phone number already exists");
            }
            
            // Create user entity
            User user = userMapper.toUserFromProducerRequest(request);
            user.setId(UUID.randomUUID().toString());
            user.setRole(User.UserRole.PRODUCER);
            user.setStatus(User.UserStatus.PENDING_VERIFICATION);
            user.setEmailVerified(false);
            user.setPhoneVerified(false);
            user.setCreatedAt(LocalDateTime.now());
            user.setUpdatedAt(LocalDateTime.now());
            
            // Save user
            User savedUser = userRepository.save(user);
            
            // Create producer profile
            Producer producer = userMapper.toProducerFromRequest(request);
            producer.setId(UUID.randomUUID().toString());
            producer.setUserId(savedUser.getId());
            producer.setVerificationStatus(Producer.VerificationStatus.PENDING);
            producer.setCreatedAt(LocalDateTime.now());
            producer.setUpdatedAt(LocalDateTime.now());
            
            // Save producer
            Producer savedProducer = producerRepository.save(producer);
            
            // Create user in Keycloak
            String keycloakUserId = keycloakService.createUser(savedUser, "PRODUCER");
            savedUser.setKeycloakUserId(keycloakUserId);
            userRepository.save(savedUser);
            
            // Send verification email
            emailService.sendEmailVerification(savedUser);
            
            // Map to response DTO
            ProducerResponse response = userMapper.toCombinedProducerResponse(savedUser, savedProducer);
            
            log.info("Successfully registered producer: {}", savedUser.getId());
            return ApiResponse.success(response, "Producer registered successfully. Please verify your email.");
            
        } catch (BusinessException e) {
            log.error("Business error during producer registration: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error registering producer: ", e);
            throw new BusinessException("Failed to register producer: " + e.getMessage());
        }
    }
    
    /**
     * Register a new buyer
     */
    public ApiResponse<BuyerResponse> registerBuyer(BuyerRegistrationRequest request) {
        log.info("Registering new buyer with email: {}", request.getEmail());
        
        try {
            // Validate email uniqueness
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new BusinessException("User with this email already exists");
            }
            
            // Validate phone uniqueness (if provided)
            if (request.getPhone() != null && userRepository.existsByPhone(request.getPhone())) {
                throw new BusinessException("User with this phone number already exists");
            }
            
            // Create user entity
            User user = userMapper.toUserFromBuyerRequest(request);
            user.setId(UUID.randomUUID().toString());
            user.setRole(User.UserRole.BUYER);
            user.setStatus(User.UserStatus.PENDING_VERIFICATION);
            user.setEmailVerified(false);
            user.setPhoneVerified(false);
            user.setCreatedAt(LocalDateTime.now());
            user.setUpdatedAt(LocalDateTime.now());
            
            // Save user
            User savedUser = userRepository.save(user);
            
            // Create buyer profile
            Buyer buyer = userMapper.toBuyerFromRequest(request);
            buyer.setId(UUID.randomUUID().toString());
            buyer.setUserId(savedUser.getId());
            buyer.setVerificationStatus(Buyer.VerificationStatus.PENDING);
            buyer.setCreatedAt(LocalDateTime.now());
            buyer.setUpdatedAt(LocalDateTime.now());
            
            // Save buyer
            Buyer savedBuyer = buyerRepository.save(buyer);
            
            // Create user in Keycloak
            String keycloakUserId = keycloakService.createUser(savedUser, "BUYER");
            savedUser.setKeycloakUserId(keycloakUserId);
            userRepository.save(savedUser);
            
            // Send verification email
            emailService.sendEmailVerification(savedUser);
            
            // Map to response DTO
            BuyerResponse response = userMapper.toCombinedBuyerResponse(savedUser, savedBuyer);
            
            log.info("Successfully registered buyer: {}", savedUser.getId());
            return ApiResponse.success(response, "Buyer registered successfully. Please verify your email.");
            
        } catch (BusinessException e) {
            log.error("Business error during buyer registration: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error registering buyer: ", e);
            throw new BusinessException("Failed to register buyer: " + e.getMessage());
        }
    }
    
    /**
     * Get user by ID
     */
    @Transactional(readOnly = true)
    public UserResponse getUserById(String id) {
        log.debug("Getting user by ID: {}", id);
        
        User user = userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));
        
        return userMapper.toUserResponse(user);
    }
    
    /**
     * Get user by email
     */
    @Transactional(readOnly = true)
    public UserResponse getUserByEmail(String email) {
        log.debug("Getting user by email: {}", email);
        
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
        
        return userMapper.toUserResponse(user);
    }
    
    /**
     * Get current user profile
     */
    @Transactional(readOnly = true)
    public UserResponse getCurrentUserProfile() {
        String currentUserId = getCurrentUserId();
        return getUserById(currentUserId);
    }
    
    /**
     * Update user profile
     */
    public UserResponse updateUserProfile(String id, UserUpdateRequest request) {
        log.info("Updating user profile: {}", id);
        
        User user = userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));
        
        // Validate that current user can update this profile
        validateUserAccess(id);
        
        // Update user fields
        if (request.getFirstName() != null) {
            user.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            user.setLastName(request.getLastName());
        }
        if (request.getBio() != null) {
            user.setBio(request.getBio());
        }
        if (request.getLocation() != null) {
            user.setLocation(request.getLocation());
        }
        if (request.getProfileImageUrl() != null) {
            user.setProfileImageUrl(request.getProfileImageUrl());
        }
        if (request.getPreferredLanguage() != null) {
            user.setPreferredLanguage(request.getPreferredLanguage());
        }
        if (request.getTimezone() != null) {
            user.setTimezone(request.getTimezone());
        }
        
        user.setUpdatedBy(getCurrentUserId());
        user.setUpdatedAt(LocalDateTime.now());
        
        User updatedUser = userRepository.save(user);
        
        log.info("Successfully updated user profile: {}", id);
        return userMapper.toUserResponse(updatedUser);
    }
    
    /**
     * Verify email address
     */
    public ApiResponse<String> verifyEmail(String token) {
        log.info("Verifying email with token: {}", token);
        
        // Find user by verification token
        Optional<User> userOpt = userRepository.findByEmailVerificationToken(token);
        if (userOpt.isEmpty()) {
            throw new BusinessException("Invalid or expired verification token");
        }
        
        User user = userOpt.get();
        
        // Check if token is expired
        if (user.getEmailVerificationTokenExpiresAt() != null && 
            user.getEmailVerificationTokenExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BusinessException("Verification token has expired");
        }
        
        // Mark email as verified
        userRepository.updateEmailVerification(user.getId(), true);
        userRepository.clearEmailVerificationToken(user.getId());
        
        // Update user status if both email and phone are verified (or phone not required)
        if (user.getPhone() == null || user.getPhoneVerified()) {
            userRepository.updateStatus(user.getId(), User.UserStatus.ACTIVE);
        }
        
        log.info("Successfully verified email for user: {}", user.getId());
        return ApiResponse.success(null, "Email verified successfully");
    }
    
    /**
     * Send password reset email
     */
    public ApiResponse<String> sendPasswordResetEmail(String email) {
        log.info("Sending password reset email to: {}", email);
        
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            // Don't reveal if email exists for security
            return ApiResponse.success(null, "If an account with this email exists, you will receive a password reset email");
        }
        
        User user = userOpt.get();
        
        // Generate reset token
        String resetToken = UUID.randomUUID().toString();
        LocalDateTime expiresAt = LocalDateTime.now().plusHours(1); // 1 hour expiry
        
        userRepository.setPasswordResetToken(user.getId(), resetToken, expiresAt);
        
        // Send reset email
        emailService.sendPasswordResetEmail(user, resetToken);
        
        log.info("Password reset email sent for user: {}", user.getId());
        return ApiResponse.success(null, "Password reset email sent");
    }
    
    /**
     * Get users with pagination
     */
    @Transactional(readOnly = true)
    public Page<UserResponse> getUsers(Pageable pageable) {
        log.debug("Getting users with pagination: page={}, size={}", pageable.getPageNumber(), pageable.getPageSize());
        
        List<User> users = userRepository.findAll(pageable.getPageNumber(), pageable.getPageSize());
        long total = userRepository.count();
        
        List<UserResponse> userResponses = users.stream()
            .map(userMapper::toUserResponse)
            .collect(Collectors.toList());
        
        return new PageImpl<>(userResponses, pageable, total);
    }
    
    /**
     * Search users
     */
    @Transactional(readOnly = true)
    public Page<UserResponse> searchUsers(String searchTerm, Pageable pageable) {
        log.debug("Searching users with term: {}", searchTerm);
        
        List<User> users = userRepository.searchUsers(searchTerm, pageable.getPageNumber(), pageable.getPageSize());
        // Note: This is a simplified count, in production you might want to count search results specifically
        long total = userRepository.count();
        
        List<UserResponse> userResponses = users.stream()
            .map(userMapper::toUserResponse)
            .collect(Collectors.toList());
        
        return new PageImpl<>(userResponses, pageable, total);
    }
    
    /**
     * Update user status (Admin only)
     */
    public UserResponse updateUserStatus(String id, User.UserStatus status, String reason) {
        log.info("Updating user status: {} to {}, reason: {}", id, status, reason);
        
        // Validate admin access
        validateAdminAccess();
        
        User user = userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));
        
        boolean updated = userRepository.updateStatus(id, status);
        if (!updated) {
            throw new BusinessException("Failed to update user status");
        }
        
        // Audit log the status change
        // TODO: Add audit logging
        
        user.setStatus(status);
        log.info("Successfully updated user status: {} to {}", id, status);
        return userMapper.toUserResponse(user);
    }
    
    /**
     * Get user statistics
     */
    @Transactional(readOnly = true)
    public UserStatsResponse getUserStatistics() {
        log.debug("Getting user statistics");
        
        long totalUsers = userRepository.count();
        long producers = userRepository.countByRole(User.UserRole.PRODUCER);
        long buyers = userRepository.countByRole(User.UserRole.BUYER);
        long activeUsers = userRepository.countByStatus(User.UserStatus.ACTIVE);
        long pendingUsers = userRepository.countByStatus(User.UserStatus.PENDING_VERIFICATION);
        long suspendedUsers = userRepository.countByStatus(User.UserStatus.SUSPENDED);
        
        return UserStatsResponse.builder()
            .totalUsers(totalUsers)
            .producers(producers)
            .buyers(buyers)
            .activeUsers(activeUsers)
            .pendingUsers(pendingUsers)
            .suspendedUsers(suspendedUsers)
            .verifiedUsers(activeUsers) // Simplified: active users are considered verified
            .unverifiedUsers(pendingUsers)
            .build();
    }
    
    /**
     * Get users by role with pagination
     */
    @Transactional(readOnly = true)
    public Page<UserResponse> getUsersByRole(User.UserRole role, Pageable pageable) {
        log.debug("Getting users by role: {}", role);
        
        List<User> users = userRepository.findByRole(role, pageable.getPageNumber(), pageable.getPageSize());
        long total = userRepository.countByRole(role);
        
        List<UserResponse> userResponses = users.stream()
            .map(userMapper::toUserResponse)
            .collect(Collectors.toList());
        
        return new PageImpl<>(userResponses, pageable, total);
    }
    
    /**
     * Get users by status with pagination
     */
    @Transactional(readOnly = true)
    public Page<UserResponse> getUsersByStatus(User.UserStatus status, Pageable pageable) {
        log.debug("Getting users by status: {}", status);
        
        List<User> users = userRepository.findByStatus(status, pageable.getPageNumber(), pageable.getPageSize());
        long total = userRepository.countByStatus(status);
        
        List<UserResponse> userResponses = users.stream()
            .map(userMapper::toUserResponse)
            .collect(Collectors.toList());
        
        return new PageImpl<>(userResponses, pageable, total);
    }
    
    /**
     * Check if the provided ID is the current user's ID
     */
    public boolean isCurrentUser(String userId) {
        try {
            String currentUserId = getCurrentUserId();
            return currentUserId.equals(userId);
        } catch (Exception e) {
            return false;
        }
    }
    
    // Helper methods
    
    private String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            return authentication.getName(); // Assuming this returns user ID
        }
        throw new BusinessException("User not authenticated");
    }
    
    private void validateUserAccess(String userId) {
        String currentUserId = getCurrentUserId();
        if (!currentUserId.equals(userId) && !isAdmin()) {
            throw new BusinessException("Access denied: Cannot access this user's data");
        }
    }
    
    private void validateAdminAccess() {
        if (!isAdmin()) {
            throw new BusinessException("Access denied: Admin role required");
        }
    }
    
    private boolean isAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && 
               authentication.getAuthorities().stream()
                   .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
    }
}