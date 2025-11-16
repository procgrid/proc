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
 * Buyer entity extending User with business-specific details
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Buyer extends BaseEntity {
    
    @NotBlank(message = "User ID is required")
    @Size(max = 50, message = "User ID length cannot exceed 50 characters")
    private String userId;
    
    @NotBlank(message = "Business name is required")
    @Size(max = 200, message = "Business name length cannot exceed 200 characters")
    private String businessName;
    
    @Size(max = 1000, message = "Business description length cannot exceed 1000 characters")
    private String businessDescription;
    
    @NotNull(message = "Business type is required")
    private BusinessType businessType;
    
    @Size(max = 500, message = "Business address length cannot exceed 500 characters")
    private String businessAddress;
    
    @Size(max = 100, message = "Business city length cannot exceed 100 characters")
    private String businessCity;
    
    @Size(max = 100, message = "Business state length cannot exceed 100 characters")
    private String businessState;
    
    @Size(max = 20, message = "Business pincode length cannot exceed 20 characters")
    @Pattern(regexp = "^[1-9][0-9]{5}$", message = "Invalid pincode format")
    private String businessPincode;
    
    @Size(max = 100, message = "Business country length cannot exceed 100 characters")
    private String businessCountry;
    
    @Size(max = 50, message = "Business registration number length cannot exceed 50 characters")
    private String businessRegistrationNumber;
    
    @Size(max = 20, message = "PAN number length cannot exceed 20 characters")
    @Pattern(regexp = "^[A-Z]{5}[0-9]{4}[A-Z]{1}$", message = "Invalid PAN number format")
    private String panNumber;
    
    @Size(max = 50, message = "CIN number length cannot exceed 50 characters")
    private String cinNumber;
    
    @Size(max = 100, message = "Industry type length cannot exceed 100 characters")
    private String industryType;
    
    @Size(max = 1000, message = "Product interests length cannot exceed 1000 characters")
    private String productInterests; // JSON array of interested product categories
    
    @DecimalMin(value = "0.0", message = "Annual turnover cannot be negative")
    @Digits(integer = 15, fraction = 2, message = "Invalid annual turnover format")
    private BigDecimal annualTurnover;
    
    @Min(value = 0, message = "Number of employees cannot be negative")
    private Integer numberOfEmployees;
    
    @Size(max = 500, message = "Website URL length cannot exceed 500 characters")
    @Pattern(regexp = "^(https?://)?(www\\.)?[a-zA-Z0-9-]+(\\.[a-zA-Z]{2,})+(/.*)?$", 
             message = "Invalid website URL format")
    private String websiteUrl;
    
    @Min(value = 1900, message = "Establishment year must be valid")
    @Max(value = 2030, message = "Establishment year cannot be in the future")
    private Integer establishmentYear;
    
    // Primary contact details
    @Size(max = 100, message = "Primary contact person length cannot exceed 100 characters")
    private String primaryContactPerson;
    
    @Size(max = 100, message = "Primary contact designation length cannot exceed 100 characters")
    private String primaryContactDesignation;
    
    @Size(max = 20, message = "Primary contact phone length cannot exceed 20 characters")
    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Invalid phone number format")
    private String primaryContactPhone;
    
    @Size(max = 255, message = "Primary contact email length cannot exceed 255 characters")
    @Email(message = "Invalid email format")
    private String primaryContactEmail;
    
    // Secondary contact details (optional)
    @Size(max = 100, message = "Secondary contact person length cannot exceed 100 characters")
    private String secondaryContactPerson;
    
    @Size(max = 100, message = "Secondary contact designation length cannot exceed 100 characters")
    private String secondaryContactDesignation;
    
    @Size(max = 20, message = "Secondary contact phone length cannot exceed 20 characters")
    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Invalid phone number format")
    private String secondaryContactPhone;
    
    @Size(max = 255, message = "Secondary contact email length cannot exceed 255 characters")
    @Email(message = "Invalid email format")
    private String secondaryContactEmail;
    
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
    
    @NotNull(message = "Verification status is required")
    private VerificationStatus verificationStatus;
    
    @Size(max = 1000, message = "Verification notes length cannot exceed 1000 characters")
    private String verificationNotes;
    
    // Credit and payment preferences
    @DecimalMin(value = "0.0", message = "Credit limit cannot be negative")
    @Digits(integer = 15, fraction = 2, message = "Invalid credit limit format")
    private BigDecimal creditLimit;
    
    @Size(max = 50, message = "Payment terms length cannot exceed 50 characters")
    private String preferredPaymentTerms; // e.g., "Net 30", "COD", "Advance"
    
    // Business logic methods
    public boolean isVerified() {
        return VerificationStatus.VERIFIED.equals(this.verificationStatus);
    }
    
    public boolean isPendingVerification() {
        return VerificationStatus.PENDING.equals(this.verificationStatus);
    }
    
    public boolean canPlaceOrders() {
        return this.isVerified();
    }
    
    public String getFullBusinessAddress() {
        StringBuilder address = new StringBuilder();
        if (businessAddress != null) address.append(businessAddress);
        if (businessCity != null) address.append(", ").append(businessCity);
        if (businessState != null) address.append(", ").append(businessState);
        if (businessPincode != null) address.append(" - ").append(businessPincode);
        if (businessCountry != null) address.append(", ").append(businessCountry);
        return address.toString();
    }
    
    public boolean isCorporate() {
        return BusinessType.CORPORATION.equals(this.businessType) || 
               BusinessType.LIMITED_COMPANY.equals(this.businessType);
    }
    
    /**
     * Business type enumeration
     */
    public enum BusinessType {
        SOLE_PROPRIETORSHIP("Sole Proprietorship"),
        PARTNERSHIP("Partnership"),
        LIMITED_COMPANY("Private Limited Company"),
        CORPORATION("Corporation"),
        COOPERATIVE("Cooperative Society"),
        TRUST("Trust"),
        NGO("Non-Governmental Organization"),
        GOVERNMENT("Government Entity"),
        OTHER("Other");
        
        private final String description;
        
        BusinessType(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    /**
     * Buyer verification status enumeration
     */
    public enum VerificationStatus {
        PENDING("Verification pending"),
        IN_REVIEW("Under review"),
        VERIFIED("Verified buyer"),
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