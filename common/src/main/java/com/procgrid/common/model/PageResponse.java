package com.procgrid.common.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Standard paginated response
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResponse<T> {
    
    private List<T> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
    private boolean first;
    private boolean last;
    private String sortBy;
    private String sortDirection;
    
    public static <T> PageResponse<T> of(List<T> content, PageRequest pageRequest, long totalElements) {
        PageResponse<T> response = new PageResponse<>();
        response.setContent(content);
        response.setPage(pageRequest.getPage());
        response.setSize(pageRequest.getSize());
        response.setTotalElements(totalElements);
        response.setTotalPages((int) Math.ceil((double) totalElements / pageRequest.getSize()));
        response.setFirst(pageRequest.getPage() == 0);
        response.setLast(pageRequest.getPage() >= response.getTotalPages() - 1);
        response.setSortBy(pageRequest.getSortBy());
        response.setSortDirection(pageRequest.getSortDirection());
        return response;
    }
}