package com.procgrid.userservice.model;

import com.procgrid.common.model.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

/**
 * User entity representing both producers and buyers
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class User extends BaseEntity {
    
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Size(max = 255, message = "Email length cannot exceed 255 characters")
    private String email;
    
    @Size(max = 20, message = "Phone number length cannot exceed 20 characters")
    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Invalid phone number format")
    private String phone;
    
    @NotNull(message = "User role is required")
    private UserRole role;
    
    @NotNull(message = "User status is required")
    private UserStatus status;
    
    @NotBlank(message = "First name is required")
    @Size(max = 100, message = "First name length cannot exceed 100 characters")
    private String firstName;
    
    @NotBlank(message = "Last name is required")
    @Size(max = 100, message = "Last name length cannot exceed 100 characters")
    private String lastName;
    
    @Size(max = 500, message = "Profile image URL length cannot exceed 500 characters")
    private String profileImageUrl;
    
    @Size(max = 1000, message = "Bio length cannot exceed 1000 characters")
    private String bio;
    
    @Size(max = 200, message = "Location length cannot exceed 200 characters")
    private String location;
    
    @Size(max = 15, message = "GST number length cannot exceed 15 characters")
    @Pattern(regexp = "^[0-9]{2}[A-Z]{5}[0-9]{4}[A-Z]{1}[1-9A-Z]{1}Z[0-9A-Z]{1}$", 
             message = "Invalid GST number format")
    private String gstNumber;
    
    @NotNull(message = "Email verification status is required")
    private Boolean emailVerified;
    
    @NotNull(message = "Phone verification status is required")
    private Boolean phoneVerified;
    
    private LocalDateTime emailVerifiedAt;
    private LocalDateTime phoneVerifiedAt;
    private LocalDateTime lastLoginAt;
    private LocalDateTime passwordChangedAt;
    
    @Size(max = 100, message = "Password reset token length cannot exceed 100 characters")
    private String passwordResetToken;
    
    private LocalDateTime passwordResetTokenExpiresAt;
    
    @Size(max = 100, message = "Email verification token length cannot exceed 100 characters")
    private String emailVerificationToken;
    
    private LocalDateTime emailVerificationTokenExpiresAt;
    
    // Keycloak integration fields
    @Size(max = 100, message = "Keycloak user ID length cannot exceed 100 characters")
    private String keycloakUserId;
    
    @Size(max = 255, message = "Preferred language length cannot exceed 255 characters")
    private String preferredLanguage;
    
    @Size(max = 50, message = "Timezone length cannot exceed 50 characters")
    private String timezone;
    
    // Audit fields
    @Size(max = 100, message = "Created by length cannot exceed 100 characters")
    private String createdBy;
    
    @Size(max = 100, message = "Updated by length cannot exceed 100 characters")
    private String updatedBy;
    
    // Business logic methods
    public String getFullName() {
        return firstName + " " + lastName;
    }
    
    public boolean isProducer() {
        return UserRole.PRODUCER.equals(this.role);
    }
    
    public boolean isBuyer() {
        return UserRole.BUYER.equals(this.role);
    }
    
    public boolean isAdmin() {
        return UserRole.ADMIN.equals(this.role);
    }
    
    public boolean isActive() {
        return UserStatus.ACTIVE.equals(this.status);
    }
    
    public boolean isKycRequired() {
        return this.isProducer() || this.isBuyer();
    }
    
    public boolean canLogin() {
        return this.isActive() && this.emailVerified;
    }
    
    /**
     * User roles enumeration
     */
    public enum UserRole {
        PRODUCER("Producer - Sells agricultural products"),
        BUYER("Buyer - Purchases agricultural products"),
        ADMIN("Administrator - Platform management");
        
        private final String description;
        
        UserRole(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    /**
     * User status enumeration
     */
    public enum UserStatus {
        ACTIVE("Active user account"),
        INACTIVE("Inactive user account"),
        SUSPENDED("Suspended due to policy violations"),
        PENDING_VERIFICATION("Pending email or phone verification"),
        BLOCKED("Blocked account");
        
        private final String description;
        
        UserStatus(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
}