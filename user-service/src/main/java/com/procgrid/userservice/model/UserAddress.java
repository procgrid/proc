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
 * User Address entity for storing multiple addresses per user
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class UserAddress extends BaseEntity {
    
    @NotBlank(message = "User ID is required")
    @Size(max = 50, message = "User ID length cannot exceed 50 characters")
    private String userId;
    
    @NotNull(message = "Address type is required")
    private AddressType addressType;
    
    @NotBlank(message = "Address line 1 is required")
    @Size(max = 255, message = "Address line 1 length cannot exceed 255 characters")
    private String addressLine1;
    
    @Size(max = 255, message = "Address line 2 length cannot exceed 255 characters")
    private String addressLine2;
    
    @Size(max = 100, message = "Landmark length cannot exceed 100 characters")
    private String landmark;
    
    @NotBlank(message = "City is required")
    @Size(max = 100, message = "City length cannot exceed 100 characters")
    private String city;
    
    @NotBlank(message = "State is required")
    @Size(max = 100, message = "State length cannot exceed 100 characters")
    private String state;
    
    @NotBlank(message = "Pincode is required")
    @Size(max = 20, message = "Pincode length cannot exceed 20 characters")
    @Pattern(regexp = "^[1-9][0-9]{5}$", message = "Invalid pincode format")
    private String pincode;
    
    @NotBlank(message = "Country is required")
    @Size(max = 100, message = "Country length cannot exceed 100 characters")
    private String country;
    
    // GPS coordinates (optional)
    @DecimalMin(value = "-90.0", message = "Latitude must be between -90 and 90")
    @DecimalMax(value = "90.0", message = "Latitude must be between -90 and 90")
    @Digits(integer = 2, fraction = 8, message = "Invalid latitude format")
    private BigDecimal latitude;
    
    @DecimalMin(value = "-180.0", message = "Longitude must be between -180 and 180")
    @DecimalMax(value = "180.0", message = "Longitude must be between -180 and 180")
    @Digits(integer = 3, fraction = 8, message = "Invalid longitude format")
    private BigDecimal longitude;
    
    @NotNull(message = "Default address flag is required")
    private Boolean isDefault;
    
    @NotNull(message = "Active status is required")
    private Boolean isActive;
    
    // Contact person for this address (especially for business addresses)
    @Size(max = 100, message = "Contact person length cannot exceed 100 characters")
    private String contactPersonName;
    
    @Size(max = 20, message = "Contact phone length cannot exceed 20 characters")
    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Invalid phone number format")
    private String contactPhone;
    
    @Size(max = 255, message = "Contact email length cannot exceed 255 characters")
    @Email(message = "Invalid email format")
    private String contactEmail;
    
    @Size(max = 500, message = "Special instructions length cannot exceed 500 characters")
    private String specialInstructions; // Delivery instructions
    
    @Size(max = 100, message = "Address alias length cannot exceed 100 characters")
    private String addressAlias; // e.g., "Home", "Office", "Warehouse 1"
    
    // Validation status for address verification
    @NotNull(message = "Verification status is required")
    private VerificationStatus verificationStatus;
    
    @Size(max = 500, message = "Verification notes length cannot exceed 500 characters")
    private String verificationNotes;
    
    // Business logic methods
    public String getFullAddress() {
        StringBuilder address = new StringBuilder();
        address.append(addressLine1);
        if (addressLine2 != null && !addressLine2.trim().isEmpty()) {
            address.append(", ").append(addressLine2);
        }
        if (landmark != null && !landmark.trim().isEmpty()) {
            address.append(", ").append(landmark);
        }
        address.append(", ").append(city);
        address.append(", ").append(state);
        address.append(" - ").append(pincode);
        address.append(", ").append(country);
        return address.toString();
    }
    
    public String getShortAddress() {
        return city + ", " + state + " - " + pincode;
    }
    
    public boolean isVerified() {
        return VerificationStatus.VERIFIED.equals(this.verificationStatus);
    }
    
    public boolean isPending() {
        return VerificationStatus.PENDING.equals(this.verificationStatus);
    }
    
    public boolean canUseForDelivery() {
        return this.isActive && this.isVerified();
    }
    
    public boolean hasGpsCoordinates() {
        return latitude != null && longitude != null;
    }
    
    /**
     * Address type enumeration
     */
    public enum AddressType {
        HOME("Home Address"),
        OFFICE("Office Address"),
        WAREHOUSE("Warehouse Address"),
        BILLING("Billing Address"),
        SHIPPING("Shipping Address"),
        FARM("Farm Address"),
        FACTORY("Factory Address"),
        PICKUP_POINT("Pickup Point"),
        OTHER("Other Address");
        
        private final String description;
        
        AddressType(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    /**
     * Address verification status enumeration
     */
    public enum VerificationStatus {
        PENDING("Verification pending"),
        VERIFIED("Verified address"),
        REJECTED("Verification rejected"),
        UNVERIFIED("Not verified");
        
        private final String description;
        
        VerificationStatus(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
}