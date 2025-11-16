package com.procgrid.userservice.model;

import com.procgrid.common.model.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

/**
 * Producer entity extending User with farm-specific details
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Producer extends BaseEntity {
    
    @NotBlank(message = "User ID is required")
    @Size(max = 50, message = "User ID length cannot exceed 50 characters")
    private String userId;
    
    @NotBlank(message = "Farm name is required")
    @Size(max = 200, message = "Farm name length cannot exceed 200 characters")
    private String farmName;
    
    @Size(max = 1000, message = "Farm description length cannot exceed 1000 characters")
    private String farmDescription;
    
    @DecimalMin(value = "0.0", inclusive = false, message = "Farm size must be greater than 0")
    @Digits(integer = 10, fraction = 2, message = "Invalid farm size format")
    private BigDecimal farmSizeInAcres;
    
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
    
    // GPS coordinates
    @DecimalMin(value = "-90.0", message = "Latitude must be between -90 and 90")
    @DecimalMax(value = "90.0", message = "Latitude must be between -90 and 90")
    @Digits(integer = 2, fraction = 8, message = "Invalid latitude format")
    private BigDecimal farmLatitude;
    
    @DecimalMin(value = "-180.0", message = "Longitude must be between -180 and 180")
    @DecimalMax(value = "180.0", message = "Longitude must be between -180 and 180")
    @Digits(integer = 3, fraction = 8, message = "Invalid longitude format")
    private BigDecimal farmLongitude;
    
    @Size(max = 1000, message = "Farming practices length cannot exceed 1000 characters")
    private String farmingPractices;
    
    @Size(max = 1000, message = "Crop specialization length cannot exceed 1000 characters")
    private String cropSpecialization;
    
    @Size(max = 1000, message = "Certifications length cannot exceed 1000 characters")
    private String certifications; // JSON array of certification details
    
    @Size(max = 500, message = "Equipment details length cannot exceed 500 characters")
    private String equipmentDetails;
    
    @Min(value = 0, message = "Years of experience cannot be negative")
    @Max(value = 100, message = "Years of experience cannot exceed 100")
    private Integer yearsOfExperience;
    
    @Size(max = 100, message = "Primary contact person length cannot exceed 100 characters")
    private String primaryContactPerson;
    
    @Size(max = 20, message = "Primary contact phone length cannot exceed 20 characters")
    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Invalid phone number format")
    private String primaryContactPhone;
    
    @Size(max = 255, message = "Primary contact email length cannot exceed 255 characters")
    @Email(message = "Invalid email format")
    private String primaryContactEmail;
    
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
    
    @NotNull(message = "Verification status is required")
    private VerificationStatus verificationStatus;
    
    @Size(max = 1000, message = "Verification notes length cannot exceed 1000 characters")
    private String verificationNotes;
    
    // Business logic methods
    public boolean isVerified() {
        return VerificationStatus.VERIFIED.equals(this.verificationStatus);
    }
    
    public boolean isPendingVerification() {
        return VerificationStatus.PENDING.equals(this.verificationStatus);
    }
    
    public boolean canListProducts() {
        return this.isVerified();
    }
    
    public String getFullFarmAddress() {
        StringBuilder address = new StringBuilder();
        if (farmAddress != null) address.append(farmAddress);
        if (farmCity != null) address.append(", ").append(farmCity);
        if (farmState != null) address.append(", ").append(farmState);
        if (farmPincode != null) address.append(" - ").append(farmPincode);
        if (farmCountry != null) address.append(", ").append(farmCountry);
        return address.toString();
    }
    
    /**
     * Producer verification status enumeration
     */
    public enum VerificationStatus {
        PENDING("Verification pending"),
        IN_REVIEW("Under review"),
        VERIFIED("Verified producer"),
        REJECTED("Verification rejected"),
        SUSPENDED("Verification suspended");
        
        private final String description;
        
        VerificationStatus(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
}