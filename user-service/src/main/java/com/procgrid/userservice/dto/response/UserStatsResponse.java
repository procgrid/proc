package com.procgrid.userservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for user statistics response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserStatsResponse {
    
    private long totalUsers;
    private long producers;
    private long buyers;
    private long activeUsers;
    private long pendingUsers;
    private long suspendedUsers;
    private long verifiedUsers;
    private long unverifiedUsers;
    
    // Calculated percentages
    public double getProducerPercentage() {
        return totalUsers > 0 ? (double) producers / totalUsers * 100 : 0;
    }
    
    public double getBuyerPercentage() {
        return totalUsers > 0 ? (double) buyers / totalUsers * 100 : 0;
    }
    
    public double getActiveUserPercentage() {
        return totalUsers > 0 ? (double) activeUsers / totalUsers * 100 : 0;
    }
    
    public double getVerificationRate() {
        return totalUsers > 0 ? (double) verifiedUsers / totalUsers * 100 : 0;
    }
}