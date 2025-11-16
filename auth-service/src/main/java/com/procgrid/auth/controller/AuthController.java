package com.procgrid.auth.controller;

import com.procgrid.auth.dto.LoginRequest;
import com.procgrid.auth.dto.LoginResponse;
import com.procgrid.auth.dto.RegisterRequest;
import com.procgrid.auth.service.AuthService;
import com.procgrid.common.model.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Authentication REST controller
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Authentication", description = "Authentication and authorization endpoints")
public class AuthController {
    
    private final AuthService authService;
    
    @PostMapping("/login")
    @Operation(summary = "User login", description = "Authenticate user with email and password")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        log.info("Login attempt for user: {}", request.getEmail());
        
        LoginResponse response = authService.login(request);
        ApiResponse<LoginResponse> apiResponse = ApiResponse.success("Login successful", response);
        
        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }
    
    @PostMapping("/register")
    @Operation(summary = "User registration", description = "Register new user (Producer or Buyer)")
    public ResponseEntity<ApiResponse<String>> register(@Valid @RequestBody RegisterRequest request) {
        log.info("Registration attempt for user: {}", request.getEmail());
        
        authService.register(request);
        ApiResponse<String> apiResponse = ApiResponse.success("Registration successful", 
            "User registered successfully. Please check your email for verification.");
        
        return new ResponseEntity<>(apiResponse, HttpStatus.CREATED);
    }
    
    @PostMapping("/refresh")
    @Operation(summary = "Refresh token", description = "Refresh access token using refresh token")
    public ResponseEntity<ApiResponse<LoginResponse>> refreshToken(@RequestParam String refreshToken) {
        log.info("Token refresh attempt");
        
        LoginResponse response = authService.refreshToken(refreshToken);
        ApiResponse<LoginResponse> apiResponse = ApiResponse.success("Token refreshed successfully", response);
        
        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }
    
    @PostMapping("/logout")
    @Operation(summary = "User logout", description = "Logout user and invalidate session")
    public ResponseEntity<ApiResponse<String>> logout(@RequestParam String userId) {
        log.info("Logout attempt for user: {}", userId);
        
        authService.logout(userId);
        ApiResponse<String> apiResponse = ApiResponse.success("Logout successful", "User logged out successfully");
        
        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }
    
    @GetMapping("/health")
    @Operation(summary = "Health check", description = "Check authentication service health")
    public ResponseEntity<ApiResponse<String>> healthCheck() {
        ApiResponse<String> response = ApiResponse.success("Authentication service is running", "OK");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}