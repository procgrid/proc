package com.procgrid.userservice.dto.response;

import com.procgrid.userservice.model.Producer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for producer response
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProducerResponse {
    
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
    
    // Farm details
    private String farmName;
    private String farmDescription;
    private BigDecimal farmSizeInAcres;
    private String farmAddress;
    private String farmCity;
    private String farmState;
    private String farmPincode;
    private String farmCountry;
    private String fullFarmAddress;
    private BigDecimal farmLatitude;
    private BigDecimal farmLongitude;
    private String farmingPractices;
    private String cropSpecialization;
    private String certifications;
    private String equipmentDetails;
    private Integer yearsOfExperience;
    
    // Contact details
    private String primaryContactPerson;
    private String primaryContactPhone;
    private String primaryContactEmail;
    
    // Banking details (masked for security)
    private String bankAccountHolderName;
    private String maskedBankAccountNumber;
    private String bankIfscCode;
    private String bankName;
    private String bankBranch;
    
    // Verification status
    private Producer.VerificationStatus verificationStatus;
    private String verificationNotes;
    
    // Timestamps
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Business logic methods
    public boolean isVerified() {
        return Producer.VerificationStatus.VERIFIED.equals(this.verificationStatus);
    }
    
    public boolean isPendingVerification() {
        return Producer.VerificationStatus.PENDING.equals(this.verificationStatus);
    }
    
    public boolean canListProducts() {
        return this.isVerified();
    }
    
    public boolean hasGpsCoordinates() {
        return farmLatitude != null && farmLongitude != null;
    }
}