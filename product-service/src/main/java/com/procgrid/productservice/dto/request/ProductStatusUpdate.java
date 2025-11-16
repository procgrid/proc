package com.procgrid.productservice.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;

/**
 * DTO for product status updates
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductStatusUpdate {
    
    @NotNull(message = "Status is required")
    private String status; // DRAFT, ACTIVE, OUT_OF_STOCK, DISCONTINUED, SUSPENDED
    
    @NotNull(message = "Visibility is required")
    private String visibility; // PUBLIC, PRIVATE, RESTRICTED
    
    private String reason;
}