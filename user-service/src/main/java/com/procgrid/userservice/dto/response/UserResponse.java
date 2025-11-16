package com.procgrid.userservice.dto.response;

import com.procgrid.userservice.model.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for user response
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    
    private String id;
    private String email;
    private String phone;
    private User.UserRole role;
    private User.UserStatus status;
    private String firstName;
    private String lastName;
    private String fullName;
    private String profileImageUrl;
    private String bio;
    private String location;
    private String gstNumber;
    private Boolean emailVerified;
    private Boolean phoneVerified;
    private LocalDateTime emailVerifiedAt;
    private LocalDateTime phoneVerifiedAt;
    private LocalDateTime lastLoginAt;
    private String preferredLanguage;
    private String timezone;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Convenience methods for UI
    public boolean isProducer() {
        return User.UserRole.PRODUCER.equals(this.role);
    }
    
    public boolean isBuyer() {
        return User.UserRole.BUYER.equals(this.role);
    }
    
    public boolean isAdmin() {
        return User.UserRole.ADMIN.equals(this.role);
    }
    
    public boolean isActive() {
        return User.UserStatus.ACTIVE.equals(this.status);
    }
    
    public boolean canLogin() {
        return this.isActive() && this.emailVerified;
    }
}