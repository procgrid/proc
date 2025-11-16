package com.procgrid.userservice.dto.response;

import com.procgrid.userservice.model.Buyer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for buyer response
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BuyerResponse {
    
    private String id;
    private String userId;
    
    // User details
    private String email;
    private String firstName;
    private String lastName;
    private String fullName;
    private String phone;
    private String profileImageUrl;
    private String location;
    private String gstNumber;
    private Boolean emailVerified;
    private Boolean phoneVerified;
    
    // Business details
    private String businessName;
    private String businessDescription;
    private Buyer.BusinessType businessType;
    private String businessAddress;
    private String businessCity;
    private String businessState;
    private String businessPincode;
    private String businessCountry;
    private String fullBusinessAddress;
    private String businessRegistrationNumber;
    private String panNumber;
    private String cinNumber;
    private String industryType;
    private String productInterests;
    private BigDecimal annualTurnover;
    private Integer numberOfEmployees;
    private String websiteUrl;
    private Integer establishmentYear;
    
    // Contact details
    private String primaryContactPerson;
    private String primaryContactDesignation;
    private String primaryContactPhone;
    private String primaryContactEmail;
    private String secondaryContactPerson;
    private String secondaryContactDesignation;
    private String secondaryContactPhone;
    private String secondaryContactEmail;
    
    // Banking details (masked for security)
    private String bankAccountHolderName;
    private String maskedBankAccountNumber;
    private String bankIfscCode;
    private String bankName;
    private String bankBranch;
    
    // Credit and payment preferences
    private BigDecimal creditLimit;
    private String preferredPaymentTerms;
    
    // Verification status
    private Buyer.VerificationStatus verificationStatus;
    private String verificationNotes;
    
    // Derived fields
    private Boolean isCorporate;
    
    // Timestamps
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Business logic methods
    public boolean isVerified() {
        return Buyer.VerificationStatus.VERIFIED.equals(this.verificationStatus);
    }
    
    public boolean isPendingVerification() {
        return Buyer.VerificationStatus.PENDING.equals(this.verificationStatus);
    }
    
    public boolean canPlaceOrders() {
        return this.isVerified();
    }
    
    public boolean isCorporateEntity() {
        return this.isCorporate != null && this.isCorporate;
    }
}