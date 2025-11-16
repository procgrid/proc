package com.procgrid.userservice.controller;

import com.procgrid.userservice.dto.request.ProducerRegistrationRequest;
import com.procgrid.userservice.dto.request.BuyerRegistrationRequest;
import com.procgrid.userservice.dto.request.UserUpdateRequest;
import com.procgrid.userservice.dto.response.UserResponse;
import com.procgrid.userservice.dto.response.ProducerResponse;
import com.procgrid.userservice.dto.response.BuyerResponse;
import com.procgrid.userservice.dto.response.UserStatsResponse;
import com.procgrid.userservice.model.User;
import com.procgrid.userservice.service.UserService;
import com.procgrid.common.model.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for User management operations
 */
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Slf4j
@Validated
@Tag(name = "User Management", description = "APIs for user registration, profile management, and user operations")
public class UserController {
    
    private final UserService userService;
    
    /**
     * Register a new producer
     */
    @PostMapping("/register/producer")
    @Operation(
        summary = "Register Producer", 
        description = "Register a new producer account with farm details"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Producer registered successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid input data"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Email or phone already exists")
    })
    public ResponseEntity<ApiResponse<ProducerResponse>> registerProducer(
            @Valid @RequestBody ProducerRegistrationRequest request) {
        
        log.info("Producer registration request received for email: {}", request.getEmail());
        
        ApiResponse<ProducerResponse> response = userService.registerProducer(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    /**
     * Register a new buyer
     */
    @PostMapping("/register/buyer")
    @Operation(
        summary = "Register Buyer", 
        description = "Register a new buyer account with business details"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Buyer registered successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid input data"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Email or phone already exists")
    })
    public ResponseEntity<ApiResponse<BuyerResponse>> registerBuyer(
            @Valid @RequestBody BuyerRegistrationRequest request) {
        
        log.info("Buyer registration request received for email: {}", request.getEmail());
        
        ApiResponse<BuyerResponse> response = userService.registerBuyer(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    /**
     * Get current user profile
     */
    @GetMapping("/profile")
    @Operation(
        summary = "Get Current User Profile", 
        description = "Get the profile of the currently authenticated user"
    )
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAnyRole('PRODUCER', 'BUYER', 'ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUserProfile() {
        
        log.debug("Getting current user profile");
        
        UserResponse response = userService.getCurrentUserProfile();
        return ResponseEntity.ok(ApiResponse.success("User profile retrieved successfully", response));
    }
    
    /**
     * Get user by ID
     */
    @GetMapping("/{id}")
    @Operation(
        summary = "Get User by ID", 
        description = "Get user profile by user ID (admin only or own profile)"
    )
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('ADMIN') or @userService.isCurrentUser(#id)")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(
            @Parameter(description = "User ID") @PathVariable String id) {
        
        log.debug("Getting user by ID: {}", id);
        
        UserResponse response = userService.getUserById(id);
        return ResponseEntity.ok(ApiResponse.success("User retrieved successfully", response));
    }
    
    /**
     * Update user profile
     */
    @PutMapping("/{id}")
    @Operation(
        summary = "Update User Profile", 
        description = "Update user profile information"
    )
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('ADMIN') or @userService.isCurrentUser(#id)")
    public ResponseEntity<ApiResponse<UserResponse>> updateUserProfile(
            @Parameter(description = "User ID") @PathVariable String id,
            @Valid @RequestBody UserUpdateRequest request) {
        
        log.info("Updating user profile: {}", id);
        
        UserResponse response = userService.updateUserProfile(id, request);
        return ResponseEntity.ok(ApiResponse.success("User profile updated successfully", response));
    }
    
    /**
     * Verify email address
     */
    @PostMapping("/verify-email")
    @Operation(
        summary = "Verify Email", 
        description = "Verify user email address using verification token"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Email verified successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid or expired token")
    })
    public ResponseEntity<ApiResponse<String>> verifyEmail(
            @Parameter(description = "Email verification token") @RequestParam String token) {
        
        log.info("Email verification request received for token: {}", token);
        
        ApiResponse<String> response = userService.verifyEmail(token);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Send password reset email
     */
    @PostMapping("/forgot-password")
    @Operation(
        summary = "Forgot Password", 
        description = "Send password reset email to user"
    )
    public ResponseEntity<ApiResponse<String>> forgotPassword(
            @Parameter(description = "User email address") @RequestParam String email) {
        
        log.info("Password reset request received for email: {}", email);
        
        ApiResponse<String> response = userService.sendPasswordResetEmail(email);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get all users (Admin only)
     */
    @GetMapping
    @Operation(
        summary = "Get All Users", 
        description = "Get paginated list of all users (admin only)"
    )
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<UserResponse>>> getUsers(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "createdAt") String sort,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "desc") String direction) {
        
        log.debug("Getting users - page: {}, size: {}, sort: {}, direction: {}", page, size, sort, direction);
        
        Sort.Direction sortDirection = "desc".equalsIgnoreCase(direction) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));
        
        Page<UserResponse> response = userService.getUsers(pageable);
        return ResponseEntity.ok(ApiResponse.success("Users retrieved successfully", response));
    }
    
    /**
     * Search users (Admin only)
     */
    @GetMapping("/search")
    @Operation(
        summary = "Search Users", 
        description = "Search users by name or email (admin only)"
    )
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<UserResponse>>> searchUsers(
            @Parameter(description = "Search term") @RequestParam String q,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
        
        log.debug("Searching users with term: {}", q);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        
        Page<UserResponse> response = userService.searchUsers(q, pageable);
        return ResponseEntity.ok(ApiResponse.success("Search completed successfully", response));
    }
    
    /**
     * Update user status (Admin only)
     */
    @PatchMapping("/{id}/status")
    @Operation(
        summary = "Update User Status", 
        description = "Update user account status (admin only)"
    )
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> updateUserStatus(
            @Parameter(description = "User ID") @PathVariable String id,
            @Parameter(description = "New status") @RequestParam User.UserStatus status,
            @Parameter(description = "Reason for status change") @RequestParam(required = false) String reason) {
        
        log.info("Updating user status: {} to {}, reason: {}", id, status, reason);
        
        UserResponse response = userService.updateUserStatus(id, status, reason);
        return ResponseEntity.ok(ApiResponse.success("User status updated successfully", response));
    }
    
    /**
     * Get user statistics (Admin only)
     */
    @GetMapping("/stats")
    @Operation(
        summary = "Get User Statistics", 
        description = "Get user statistics and counts (admin only)"
    )
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserStatsResponse>> getUserStatistics() {
        
        log.debug("Getting user statistics");
        
        UserStatsResponse response = userService.getUserStatistics();
        return ResponseEntity.ok(ApiResponse.success("User statistics retrieved successfully", response));
    }
    
    /**
     * Get users by role (Admin only)
     */
    @GetMapping("/role/{role}")
    @Operation(
        summary = "Get Users by Role", 
        description = "Get users filtered by role (admin only)"
    )
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<UserResponse>>> getUsersByRole(
            @Parameter(description = "User role") @PathVariable User.UserRole role,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
        
        log.debug("Getting users by role: {}", role);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        
        Page<UserResponse> response = userService.getUsersByRole(role, pageable);
        return ResponseEntity.ok(ApiResponse.success("Users retrieved successfully", response));
    }
    
    /**
     * Get users by status (Admin only)
     */
    @GetMapping("/status/{status}")
    @Operation(
        summary = "Get Users by Status", 
        description = "Get users filtered by status (admin only)"
    )
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<UserResponse>>> getUsersByStatus(
            @Parameter(description = "User status") @PathVariable User.UserStatus status,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
        
        log.debug("Getting users by status: {}", status);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        
        Page<UserResponse> response = userService.getUsersByStatus(status, pageable);
        return ResponseEntity.ok(ApiResponse.success("Users retrieved successfully", response));
    }
}