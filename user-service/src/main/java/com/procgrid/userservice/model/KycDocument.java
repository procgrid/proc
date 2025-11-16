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
 * KYC Document entity for storing user verification documents
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class KycDocument extends BaseEntity {
    
    @NotBlank(message = "User ID is required")
    @Size(max = 50, message = "User ID length cannot exceed 50 characters")
    private String userId;
    
    @NotNull(message = "Document type is required")
    private DocumentType documentType;
    
    @NotBlank(message = "Document number is required")
    @Size(max = 100, message = "Document number length cannot exceed 100 characters")
    private String documentNumber;
    
    @NotBlank(message = "File name is required")
    @Size(max = 255, message = "File name length cannot exceed 255 characters")
    private String fileName;
    
    @NotBlank(message = "File path is required")
    @Size(max = 500, message = "File path length cannot exceed 500 characters")
    private String filePath;
    
    @Size(max = 50, message = "File type length cannot exceed 50 characters")
    private String fileType; // e.g., "PDF", "JPG", "PNG"
    
    @Min(value = 0, message = "File size cannot be negative")
    private Long fileSize; // in bytes
    
    @Size(max = 500, message = "File URL length cannot exceed 500 characters")
    private String fileUrl; // for external storage like S3
    
    @NotNull(message = "Verification status is required")
    private VerificationStatus verificationStatus;
    
    @Size(max = 1000, message = "Verification notes length cannot exceed 1000 characters")
    private String verificationNotes;
    
    private LocalDateTime submittedAt;
    private LocalDateTime verifiedAt;
    
    @Size(max = 100, message = "Verified by length cannot exceed 100 characters")
    private String verifiedBy; // Admin user ID who verified the document
    
    @Size(max = 100, message = "Submission ID length cannot exceed 100 characters")
    private String submissionId; // For grouping documents submitted together
    
    private LocalDateTime expiresAt; // Document expiry date (if applicable)
    
    @Size(max = 500, message = "Additional information length cannot exceed 500 characters")
    private String additionalInfo; // Any additional document-specific information
    
    // AI/OCR extracted information (optional)
    @Size(max = 1000, message = "Extracted data length cannot exceed 1000 characters")
    private String extractedData; // JSON string of extracted information
    
    @DecimalMin(value = "0.0", message = "Confidence score cannot be negative")
    @DecimalMax(value = "1.0", message = "Confidence score cannot exceed 1.0")
    private Double confidenceScore; // AI confidence in document authenticity
    
    // Business logic methods
    public boolean isVerified() {
        return VerificationStatus.VERIFIED.equals(this.verificationStatus);
    }
    
    public boolean isPending() {
        return VerificationStatus.PENDING.equals(this.verificationStatus);
    }
    
    public boolean isRejected() {
        return VerificationStatus.REJECTED.equals(this.verificationStatus);
    }
    
    public boolean isExpired() {
        return expiresAt != null && expiresAt.isBefore(LocalDateTime.now());
    }
    
    public boolean isIdentityDocument() {
        return DocumentType.AADHAAR_CARD.equals(this.documentType) ||
               DocumentType.PAN_CARD.equals(this.documentType) ||
               DocumentType.PASSPORT.equals(this.documentType) ||
               DocumentType.DRIVING_LICENSE.equals(this.documentType) ||
               DocumentType.VOTER_ID.equals(this.documentType);
    }
    
    public boolean isAddressDocument() {
        return DocumentType.UTILITY_BILL.equals(this.documentType) ||
               DocumentType.BANK_STATEMENT.equals(this.documentType) ||
               DocumentType.AADHAAR_CARD.equals(this.documentType);
    }
    
    public boolean isBusinessDocument() {
        return DocumentType.BUSINESS_REGISTRATION.equals(this.documentType) ||
               DocumentType.GST_CERTIFICATE.equals(this.documentType) ||
               DocumentType.INCORPORATION_CERTIFICATE.equals(this.documentType);
    }
    
    public boolean isFarmDocument() {
        return DocumentType.LAND_OWNERSHIP.equals(this.documentType) ||
               DocumentType.FARMING_LICENSE.equals(this.documentType) ||
               DocumentType.CROP_INSURANCE.equals(this.documentType);
    }
    
    /**
     * Document type enumeration
     */
    public enum DocumentType {
        // Identity Documents
        AADHAAR_CARD("Aadhaar Card", true, true),
        PAN_CARD("PAN Card", true, false),
        PASSPORT("Passport", true, true),
        DRIVING_LICENSE("Driving License", true, true),
        VOTER_ID("Voter ID Card", true, true),
        
        // Address Documents
        UTILITY_BILL("Utility Bill", false, true),
        BANK_STATEMENT("Bank Statement", false, true),
        RENT_AGREEMENT("Rent Agreement", false, true),
        
        // Business Documents
        BUSINESS_REGISTRATION("Business Registration Certificate", false, false),
        GST_CERTIFICATE("GST Registration Certificate", false, false),
        INCORPORATION_CERTIFICATE("Certificate of Incorporation", false, false),
        PARTNERSHIP_DEED("Partnership Deed", false, false),
        MOA_AOA("Memorandum & Articles of Association", false, false),
        
        // Farm Documents (for Producers)
        LAND_OWNERSHIP("Land Ownership Document", false, false),
        FARMING_LICENSE("Farming License", false, false),
        CROP_INSURANCE("Crop Insurance Policy", false, false),
        ORGANIC_CERTIFICATION("Organic Certification", false, false),
        
        // Financial Documents
        BANK_ACCOUNT_PROOF("Bank Account Proof", false, false),
        CANCELLED_CHEQUE("Cancelled Cheque", false, false),
        
        // Other Documents
        OTHER("Other Document", false, false);
        
        private final String description;
        private final boolean isIdentityProof;
        private final boolean isAddressProof;
        
        DocumentType(String description, boolean isIdentityProof, boolean isAddressProof) {
            this.description = description;
            this.isIdentityProof = isIdentityProof;
            this.isAddressProof = isAddressProof;
        }
        
        public String getDescription() {
            return description;
        }
        
        public boolean isIdentityProof() {
            return isIdentityProof;
        }
        
        public boolean isAddressProof() {
            return isAddressProof;
        }
    }
    
    /**
     * Document verification status enumeration
     */
    public enum VerificationStatus {
        PENDING("Pending verification"),
        IN_REVIEW("Under review"),
        VERIFIED("Verified and approved"),
        REJECTED("Rejected - needs resubmission"),
        EXPIRED("Document has expired");
        
        private final String description;
        
        VerificationStatus(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
}