package com.procgrid.common.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Standard pagination request
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageRequest {
    
    private int page = 0;
    private int size = 20;
    private String sortBy = "id";
    private String sortDirection = "ASC";
    
    public int getOffset() {
        return page * size;
    }
    
    public boolean isValidSortDirection() {
        return "ASC".equalsIgnoreCase(sortDirection) || "DESC".equalsIgnoreCase(sortDirection);
    }
}