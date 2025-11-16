package com.procgrid.productservice.model;

/**
 * Enumeration for quality grades used in product lots and inventory
 */
public enum QualityGrade {
    
    PREMIUM("Premium", "Highest quality grade with superior characteristics"),
    GRADE_A("Grade A", "Excellent quality meeting all standards"),
    GRADE_B("Grade B", "Good quality with minor imperfections"),
    GRADE_C("Grade C", "Standard quality suitable for processing"),
    ORGANIC("Organic", "Certified organic quality"),
    EXPORT("Export", "Quality suitable for international export"),
    DOMESTIC("Domestic", "Quality suitable for domestic market"),
    PROCESSING("Processing", "Quality suitable for processing/manufacturing"),
    REJECT("Reject", "Below standard quality");
    
    private final String displayName;
    private final String description;
    
    QualityGrade(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getDescription() {
        return description;
    }
    
    /**
     * Get quality grade by display name
     */
    public static QualityGrade fromDisplayName(String displayName) {
        if (displayName == null) {
            return null;
        }
        
        for (QualityGrade grade : values()) {
            if (grade.displayName.equalsIgnoreCase(displayName)) {
                return grade;
            }
        }
        return null;
    }
    
    /**
     * Get quality score for comparison
     */
    public int getScore() {
        return switch (this) {
            case PREMIUM -> 100;
            case GRADE_A -> 90;
            case ORGANIC -> 85;
            case EXPORT -> 80;
            case GRADE_B -> 75;
            case DOMESTIC -> 70;
            case GRADE_C -> 60;
            case PROCESSING -> 40;
            case REJECT -> 0;
        };
    }
    
    /**
     * Check if this grade is suitable for export
     */
    public boolean isExportGrade() {
        return this == PREMIUM || this == GRADE_A || this == ORGANIC || this == EXPORT;
    }
    
    /**
     * Check if this grade is premium quality
     */
    public boolean isPremiumGrade() {
        return this == PREMIUM || this == ORGANIC;
    }
}