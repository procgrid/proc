package com.procgrid.userservice.service;

import com.procgrid.userservice.dto.request.ProducerRegistrationRequest;
import com.procgrid.userservice.dto.response.ProducerResponse;
import com.procgrid.userservice.dto.response.UserResponse;
import com.procgrid.userservice.mapper.UserMapper;
import com.procgrid.userservice.model.User;
import com.procgrid.userservice.model.Producer;
import com.procgrid.userservice.repository.UserRepository;
import com.procgrid.userservice.repository.ProducerRepository;
import com.procgrid.userservice.repository.BuyerRepository;
import com.procgrid.common.exception.BusinessException;
import com.procgrid.common.model.ApiResponse;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Unit tests for UserService
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserService Tests")
class UserServiceTest {
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private ProducerRepository producerRepository;
    
    @Mock
    private BuyerRepository buyerRepository;
    
    @Mock
    private UserMapper userMapper;
    
    @Mock
    private EmailService emailService;
    
    @Mock
    private KeycloakService keycloakService;
    
    @InjectMocks
    private UserService userService;
    
    private ProducerRegistrationRequest producerRequest;
    private User mockUser;
    private Producer mockProducer;
    
    @BeforeEach
    void setUp() {
        // Setup test data
        producerRequest = new ProducerRegistrationRequest();
        producerRequest.setEmail("farmer@example.com");
        producerRequest.setPhone("+919876543210");
        producerRequest.setFirstName("John");
        producerRequest.setLastName("Doe");
        producerRequest.setFarmName("Green Valley Farm");
        producerRequest.setFarmSizeInAcres(new BigDecimal("25.5"));
        producerRequest.setFarmCity("Punjab");
        producerRequest.setFarmState("Punjab");
        producerRequest.setFarmCountry("India");
        producerRequest.setAcceptTermsAndConditions(true);
        producerRequest.setAcceptPrivacyPolicy(true);
        
        mockUser = User.builder()
            .id("user123")
            .email("farmer@example.com")
            .firstName("John")
            .lastName("Doe")
            .role(User.UserRole.PRODUCER)
            .status(User.UserStatus.PENDING_VERIFICATION)
            .emailVerified(false)
            .phoneVerified(false)
            .createdAt(LocalDateTime.now())
            .build();
        
        mockProducer = Producer.builder()
            .id("producer123")
            .userId("user123")
            .farmName("Green Valley Farm")
            .farmSizeInAcres(new BigDecimal("25.5"))
            .verificationStatus(Producer.VerificationStatus.PENDING)
            .createdAt(LocalDateTime.now())
            .build();
    }
    
    @Test
    @DisplayName("Should register producer successfully")
    void shouldRegisterProducerSuccessfully() {
        // Given
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.existsByPhone(anyString())).thenReturn(false);
        when(userMapper.toUserFromProducerRequest(any())).thenReturn(mockUser);
        when(userMapper.toProducerFromRequest(any())).thenReturn(mockProducer);
        when(userRepository.save(any(User.class))).thenReturn(mockUser);
        when(producerRepository.save(any(Producer.class))).thenReturn(mockProducer);
        when(keycloakService.createUser(any(User.class), anyString())).thenReturn("keycloak123");
        
        ProducerResponse expectedResponse = new ProducerResponse();
        expectedResponse.setId("producer123");
        expectedResponse.setUserId("user123");
        expectedResponse.setEmail("farmer@example.com");
        
        when(userMapper.toCombinedProducerResponse(any(User.class), any(Producer.class)))
            .thenReturn(expectedResponse);
        
        // When
        ApiResponse<ProducerResponse> result = userService.registerProducer(producerRequest);
        
        // Then
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals("producer123", result.getData().getId());
        assertEquals("farmer@example.com", result.getData().getEmail());
        
        // Verify interactions
        verify(userRepository).existsByEmail("farmer@example.com");
        verify(userRepository).existsByPhone("+919876543210");
        verify(userRepository, times(2)).save(any(User.class));
        verify(producerRepository).save(any(Producer.class));
        verify(keycloakService).createUser(any(User.class), eq("PRODUCER"));
        verify(emailService).sendEmailVerification(any(User.class));
    }
    
    @Test
    @DisplayName("Should throw exception when email already exists")
    void shouldThrowExceptionWhenEmailExists() {
        // Given
        when(userRepository.existsByEmail(anyString())).thenReturn(true);
        
        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            userService.registerProducer(producerRequest);
        });
        
        assertEquals("User with this email already exists", exception.getMessage());
        
        // Verify no user was created
        verify(userRepository, never()).save(any(User.class));
        verify(producerRepository, never()).save(any(Producer.class));
    }
    
    @Test
    @DisplayName("Should throw exception when phone already exists")
    void shouldThrowExceptionWhenPhoneExists() {
        // Given
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.existsByPhone(anyString())).thenReturn(true);
        
        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            userService.registerProducer(producerRequest);
        });
        
        assertEquals("User with this phone number already exists", exception.getMessage());
    }
    
    @Test
    @DisplayName("Should get user by ID successfully")
    void shouldGetUserByIdSuccessfully() {
        // Given
        String userId = "user123";
        when(userRepository.findById(userId)).thenReturn(java.util.Optional.of(mockUser));
        
        UserResponse expectedResponse = new UserResponse();
        expectedResponse.setId("user123");
        expectedResponse.setEmail("farmer@example.com");
        
        when(userMapper.toUserResponse(any(User.class))).thenReturn(expectedResponse);
        
        // When
        UserResponse result = userService.getUserById(userId);
        
        // Then
        assertNotNull(result);
        assertEquals("user123", result.getId());
        assertEquals("farmer@example.com", result.getEmail());
        
        verify(userRepository).findById(userId);
        verify(userMapper).toUserResponse(mockUser);
    }
    
    @Test
    @DisplayName("Should throw exception when user not found")
    void shouldThrowExceptionWhenUserNotFound() {
        // Given
        String userId = "nonexistent";
        when(userRepository.findById(userId)).thenReturn(java.util.Optional.empty());
        
        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userService.getUserById(userId);
        });
        
        assertTrue(exception.getMessage().contains("User not found"));
    }
    
    @Test
    @DisplayName("Should verify email successfully")
    void shouldVerifyEmailSuccessfully() {
        // Given
        String token = "verification-token-123";
        mockUser.setEmailVerificationTokenExpiresAt(LocalDateTime.now().plusHours(1));
        
        when(userRepository.findByEmailVerificationToken(token))
            .thenReturn(java.util.Optional.of(mockUser));
        when(userRepository.updateEmailVerification(anyString(), anyBoolean())).thenReturn(true);
        when(userRepository.clearEmailVerificationToken(anyString())).thenReturn(true);
        when(userRepository.updateStatus(anyString(), any(User.UserStatus.class))).thenReturn(true);
        
        // When
        ApiResponse<String> result = userService.verifyEmail(token);
        
        // Then
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals("Email verified successfully", result.getMessage());
        
        verify(userRepository).findByEmailVerificationToken(token);
        verify(userRepository).updateEmailVerification("user123", true);
        verify(userRepository).clearEmailVerificationToken("user123");
    }
    
    @Test
    @DisplayName("Should throw exception for invalid verification token")
    void shouldThrowExceptionForInvalidToken() {
        // Given
        String token = "invalid-token";
        when(userRepository.findByEmailVerificationToken(token))
            .thenReturn(java.util.Optional.empty());
        
        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            userService.verifyEmail(token);
        });
        
        assertEquals("Invalid or expired verification token", exception.getMessage());
    }
    
    @Test
    @DisplayName("Should throw exception for expired verification token")
    void shouldThrowExceptionForExpiredToken() {
        // Given
        String token = "expired-token";
        mockUser.setEmailVerificationTokenExpiresAt(LocalDateTime.now().minusHours(1)); // Expired
        
        when(userRepository.findByEmailVerificationToken(token))
            .thenReturn(java.util.Optional.of(mockUser));
        
        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            userService.verifyEmail(token);
        });
        
        assertEquals("Verification token has expired", exception.getMessage());
    }
}