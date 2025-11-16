package com.procgrid.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Login response DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    
    private String accessToken;
    private String refreshToken;
    private String tokenType = "Bearer";
    private Long expiresIn;
    private String userId;
    private String email;
    private String firstName;
    private String lastName;
    private List<String> roles;
    private LocalDateTime issuedAt;
    
    public static LoginResponse of(String accessToken, String refreshToken, Long expiresIn, 
                                  String userId, String email, String firstName, String lastName, 
                                  List<String> roles) {
        return new LoginResponse(
            accessToken, 
            refreshToken, 
            "Bearer", 
            expiresIn, 
            userId, 
            email, 
            firstName, 
            lastName, 
            roles, 
            LocalDateTime.now()
        );
    }
}