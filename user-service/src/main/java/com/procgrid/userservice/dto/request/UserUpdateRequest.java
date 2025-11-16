package com.procgrid.userservice.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for user profile update request
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateRequest {
    
    @Size(max = 100, message = "First name length cannot exceed 100 characters")
    private String firstName;
    
    @Size(max = 100, message = "Last name length cannot exceed 100 characters")
    private String lastName;
    
    @Size(max = 1000, message = "Bio length cannot exceed 1000 characters")
    private String bio;
    
    @Size(max = 200, message = "Location length cannot exceed 200 characters")
    private String location;
    
    @Size(max = 500, message = "Profile image URL length cannot exceed 500 characters")
    private String profileImageUrl;
    
    @Size(max = 255, message = "Preferred language length cannot exceed 255 characters")
    private String preferredLanguage;
    
    @Size(max = 50, message = "Timezone length cannot exceed 50 characters")
    private String timezone;
}