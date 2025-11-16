package com.procgrid.userservice.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for producer registration request
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProducerRegistrationRequest {
    
    // Basic user information
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Size(max = 255, message = "Email length cannot exceed 255 characters")
    private String email;
    
    @Size(max = 20, message = "Phone number length cannot exceed 20 characters")
    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Invalid phone number format")
    private String phone;
    
    @NotBlank(message = "First name is required")
    @Size(max = 100, message = "First name length cannot exceed 100 characters")
    private String firstName;
    
    @NotBlank(message = "Last name is required")
    @Size(max = 100, message = "Last name length cannot exceed 100 characters")
    private String lastName;
    
    @Size(max = 1000, message = "Bio length cannot exceed 1000 characters")
    private String bio;
    
    @Size(max = 200, message = "Location length cannot exceed 200 characters")
    private String location;
    
    @Size(max = 15, message = "GST number length cannot exceed 15 characters")
    @Pattern(regexp = "^[0-9]{2}[A-Z]{5}[0-9]{4}[A-Z]{1}[1-9A-Z]{1}Z[0-9A-Z]{1}$", 
             message = "Invalid GST number format")
    private String gstNumber;
    
    @Size(max = 255, message = "Preferred language length cannot exceed 255 characters")
    private String preferredLanguage;
    
    @Size(max = 50, message = "Timezone length cannot exceed 50 characters")
    private String timezone;
    
    // Farm information
    @NotBlank(message = "Farm name is required")
    @Size(max = 200, message = "Farm name length cannot exceed 200 characters")
    private String farmName;
    
    @Size(max = 1000, message = "Farm description length cannot exceed 1000 characters")
    private String farmDescription;
    
    @DecimalMin(value = "0.0", inclusive = false, message = "Farm size must be greater than 0")
    @Digits(integer = 10, fraction = 2, message = "Invalid farm size format")
    private java.math.BigDecimal farmSizeInAcres;
    
    @Size(max = 500, message = "Farm address length cannot exceed 500 characters")
    private String farmAddress;
    
    @Size(max = 100, message = "Farm city length cannot exceed 100 characters")
    private String farmCity;
    
    @Size(max = 100, message = "Farm state length cannot exceed 100 characters")
    private String farmState;
    
    @Size(max = 20, message = "Farm pincode length cannot exceed 20 characters")
    @Pattern(regexp = "^[1-9][0-9]{5}$", message = "Invalid pincode format")
    private String farmPincode;
    
    @Size(max = 100, message = "Farm country length cannot exceed 100 characters")
    private String farmCountry;
    
    @DecimalMin(value = "-90.0", message = "Latitude must be between -90 and 90")
    @DecimalMax(value = "90.0", message = "Latitude must be between -90 and 90")
    @Digits(integer = 2, fraction = 8, message = "Invalid latitude format")
    private java.math.BigDecimal farmLatitude;
    
    @DecimalMin(value = "-180.0", message = "Longitude must be between -180 and 180")
    @DecimalMax(value = "180.0", message = "Longitude must be between -180 and 180")
    @Digits(integer = 3, fraction = 8, message = "Invalid longitude format")
    private java.math.BigDecimal farmLongitude;
    
    @Size(max = 1000, message = "Farming practices length cannot exceed 1000 characters")
    private String farmingPractices;
    
    @Size(max = 1000, message = "Crop specialization length cannot exceed 1000 characters")
    private String cropSpecialization;
    
    @Size(max = 1000, message = "Certifications length cannot exceed 1000 characters")
    private String certifications;
    
    @Size(max = 500, message = "Equipment details length cannot exceed 500 characters")
    private String equipmentDetails;
    
    @Min(value = 0, message = "Years of experience cannot be negative")
    @Max(value = 100, message = "Years of experience cannot exceed 100")
    private Integer yearsOfExperience;
    
    // Contact information
    @Size(max = 100, message = "Primary contact person length cannot exceed 100 characters")
    private String primaryContactPerson;
    
    @Size(max = 20, message = "Primary contact phone length cannot exceed 20 characters")
    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Invalid phone number format")
    private String primaryContactPhone;
    
    @Size(max = 255, message = "Primary contact email length cannot exceed 255 characters")
    @Email(message = "Invalid email format")
    private String primaryContactEmail;
    
    // Banking details
    @Size(max = 100, message = "Bank account holder name length cannot exceed 100 characters")
    private String bankAccountHolderName;
    
    @Size(max = 30, message = "Bank account number length cannot exceed 30 characters")
    private String bankAccountNumber;
    
    @Size(max = 15, message = "Bank IFSC code length cannot exceed 15 characters")
    @Pattern(regexp = "^[A-Z]{4}0[A-Z0-9]{6}$", message = "Invalid IFSC code format")
    private String bankIfscCode;
    
    @Size(max = 100, message = "Bank name length cannot exceed 100 characters")
    private String bankName;
    
    @Size(max = 100, message = "Bank branch length cannot exceed 100 characters")
    private String bankBranch;
    
    // Terms acceptance
    @NotNull(message = "Terms and conditions acceptance is required")
    @AssertTrue(message = "You must accept the terms and conditions")
    private Boolean acceptTermsAndConditions;
    
    @NotNull(message = "Privacy policy acceptance is required")
    @AssertTrue(message = "You must accept the privacy policy")
    private Boolean acceptPrivacyPolicy;
}