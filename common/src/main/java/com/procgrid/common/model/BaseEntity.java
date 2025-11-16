package com.procgrid.common.model;

import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

/**
 * Base entity class with common audit fields
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public abstract class BaseEntity {
    
    private String id;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
    private boolean active = true;
    private Long version = 0L;
    
    /**
     * Pre-persist callback to set creation timestamp
     */
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        if (this.createdAt == null) {
            this.createdAt = now;
        }
        this.updatedAt = now;
    }
    
    /**
     * Pre-update callback to set update timestamp
     */
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}