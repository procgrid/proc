package com.procgrid.auth.service;

import com.procgrid.auth.dto.LoginRequest;
import com.procgrid.auth.dto.LoginResponse;
import com.procgrid.auth.dto.RegisterRequest;
import com.procgrid.common.exception.BusinessException;
import com.procgrid.common.utils.ErrorCodes;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import jakarta.ws.rs.core.Response;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Authentication service with Keycloak integration
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {
    
    private final RedisTemplate<String, Object> redisTemplate;
    private final RestTemplate restTemplate = new RestTemplate();
    
    @Value("${keycloak.auth-server-url}")
    private String keycloakServerUrl;
    
    @Value("${keycloak.realm}")
    private String realm;
    
    @Value("${keycloak.resource}")
    private String clientId;
    
    @Value("${keycloak.credentials.secret}")
    private String clientSecret;
    
    @Value("${keycloak.admin.username}")
    private String adminUsername;
    
    @Value("${keycloak.admin.password}")
    private String adminPassword;
    
    /**
     * User login with Keycloak
     */
    public LoginResponse login(LoginRequest request) {
        try {
            String tokenEndpoint = keycloakServerUrl + "/realms/" + realm + "/protocol/openid-connect/token";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            
            MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
            map.add("grant_type", "password");
            map.add("client_id", clientId);
            map.add("client_secret", clientSecret);
            map.add("username", request.getEmail());
            map.add("password", request.getPassword());
            
            HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(map, headers);
            
            ResponseEntity<Map> response = restTemplate.exchange(
                tokenEndpoint, HttpMethod.POST, requestEntity, Map.class);
            
            if (response.getStatusCode() == HttpStatus.OK) {
                Map<String, Object> tokenResponse = response.getBody();
                
                String accessToken = (String) tokenResponse.get("access_token");
                String refreshToken = (String) tokenResponse.get("refresh_token");
                Integer expiresIn = (Integer) tokenResponse.get("expires_in");
                
                // Get user info from token
                Map<String, Object> userInfo = getUserInfo(accessToken);
                
                String userId = (String) userInfo.get("sub");
                String email = (String) userInfo.get("email");
                String firstName = (String) userInfo.get("given_name");
                String lastName = (String) userInfo.get("family_name");
                
                @SuppressWarnings("unchecked")
                Map<String, Object> realmAccess = (Map<String, Object>) userInfo.get("realm_access");
                @SuppressWarnings("unchecked")
                List<String> roles = realmAccess != null ? (List<String>) realmAccess.get("roles") : List.of();
                
                // Cache user session
                cacheUserSession(userId, accessToken, Duration.ofSeconds(expiresIn.longValue()));
                
                return LoginResponse.of(
                    accessToken, refreshToken, expiresIn.longValue(),
                    userId, email, firstName, lastName, roles
                );
            } else {
                throw new BusinessException("Invalid credentials", ErrorCodes.AUTH_INVALID_CREDENTIALS);
            }
        } catch (Exception e) {
            log.error("Login failed for user: {}", request.getEmail(), e);
            throw new BusinessException("Login failed", ErrorCodes.AUTH_INVALID_CREDENTIALS);
        }
    }
    
    /**
     * User registration with Keycloak
     */
    public void register(RegisterRequest request) {
        try {
            Keycloak keycloak = getAdminKeycloak();
            RealmResource realmResource = keycloak.realm(realm);
            UsersResource usersResource = realmResource.users();
            
            // Check if user already exists
            List<UserRepresentation> existingUsers = usersResource.search(request.getEmail());
            if (!existingUsers.isEmpty()) {
                throw new BusinessException("User already exists", ErrorCodes.USER_ALREADY_EXISTS);
            }
            
            // Create user representation
            UserRepresentation user = new UserRepresentation();
            user.setUsername(request.getEmail());
            user.setEmail(request.getEmail());
            user.setFirstName(request.getFirstName());
            user.setLastName(request.getLastName());
            user.setEnabled(true);
            user.setEmailVerified(false);
            
            // Set user attributes
            user.singleAttribute("phoneNumber", request.getPhoneNumber());
            if (request.getCompanyName() != null) {
                user.singleAttribute("companyName", request.getCompanyName());
            }
            if (request.getGstNumber() != null) {
                user.singleAttribute("gstNumber", request.getGstNumber());
            }
            
            // Create user
            Response response = usersResource.create(user);
            if (response.getStatus() != 201) {
                throw new BusinessException("User creation failed", ErrorCodes.USER_INVALID_DATA);
            }
            
            String userId = extractUserId(response.getLocation().getPath());
            UserResource userResource = usersResource.get(userId);
            
            // Set password
            CredentialRepresentation credential = new CredentialRepresentation();
            credential.setType(CredentialRepresentation.PASSWORD);
            credential.setValue(request.getPassword());
            credential.setTemporary(false);
            userResource.resetPassword(credential);
            
            // Assign role
            assignRole(realmResource, userResource, request.getRole());
            
            log.info("User registered successfully: {}", request.getEmail());
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Registration failed for user: {}", request.getEmail(), e);
            throw new BusinessException("Registration failed", ErrorCodes.USER_INVALID_DATA);
        }
    }
    
    /**
     * Refresh access token
     */
    public LoginResponse refreshToken(String refreshToken) {
        try {
            String tokenEndpoint = keycloakServerUrl + "/realms/" + realm + "/protocol/openid-connect/token";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            
            MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
            map.add("grant_type", "refresh_token");
            map.add("client_id", clientId);
            map.add("client_secret", clientSecret);
            map.add("refresh_token", refreshToken);
            
            HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(map, headers);
            
            ResponseEntity<Map> response = restTemplate.exchange(
                tokenEndpoint, HttpMethod.POST, requestEntity, Map.class);
            
            if (response.getStatusCode() == HttpStatus.OK) {
                Map<String, Object> tokenResponse = response.getBody();
                
                String accessToken = (String) tokenResponse.get("access_token");
                String newRefreshToken = (String) tokenResponse.get("refresh_token");
                Integer expiresIn = (Integer) tokenResponse.get("expires_in");
                
                // Get user info from token
                Map<String, Object> userInfo = getUserInfo(accessToken);
                
                String userId = (String) userInfo.get("sub");
                String email = (String) userInfo.get("email");
                String firstName = (String) userInfo.get("given_name");
                String lastName = (String) userInfo.get("family_name");
                
                @SuppressWarnings("unchecked")
                Map<String, Object> realmAccess = (Map<String, Object>) userInfo.get("realm_access");
                @SuppressWarnings("unchecked")
                List<String> roles = realmAccess != null ? (List<String>) realmAccess.get("roles") : List.of();
                
                return LoginResponse.of(
                    accessToken, newRefreshToken, expiresIn.longValue(),
                    userId, email, firstName, lastName, roles
                );
            } else {
                throw new BusinessException("Invalid refresh token", ErrorCodes.AUTH_TOKEN_INVALID);
            }
        } catch (Exception e) {
            log.error("Token refresh failed", e);
            throw new BusinessException("Token refresh failed", ErrorCodes.AUTH_TOKEN_INVALID);
        }
    }
    
    /**
     * Logout user
     */
    public void logout(String userId) {
        try {
            // Remove user session from cache
            redisTemplate.delete("user_session:" + userId);
            log.info("User logged out successfully: {}", userId);
        } catch (Exception e) {
            log.error("Logout failed for user: {}", userId, e);
        }
    }
    
    private Keycloak getAdminKeycloak() {
        return KeycloakBuilder.builder()
            .serverUrl(keycloakServerUrl)
            .realm("master")
            .grantType(OAuth2Constants.PASSWORD)
            .clientId("admin-cli")
            .username(adminUsername)
            .password(adminPassword)
            .build();
    }
    
    private Map<String, Object> getUserInfo(String accessToken) {
        String userInfoEndpoint = keycloakServerUrl + "/realms/" + realm + "/protocol/openid-connect/userinfo";
        
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        
        HttpEntity<?> requestEntity = new HttpEntity<>(headers);
        
        ResponseEntity<Map> response = restTemplate.exchange(
            userInfoEndpoint, HttpMethod.GET, requestEntity, Map.class);
        
        return response.getBody();
    }
    
    private void cacheUserSession(String userId, String accessToken, Duration expiration) {
        redisTemplate.opsForValue().set("user_session:" + userId, accessToken, expiration);
    }
    
    private String extractUserId(String locationPath) {
        return locationPath.substring(locationPath.lastIndexOf('/') + 1);
    }
    
    private void assignRole(RealmResource realmResource, UserResource userResource, String roleName) {
        RoleRepresentation roleRepresentation = realmResource.roles().get(roleName).toRepresentation();
        userResource.roles().realmLevel().add(Arrays.asList(roleRepresentation));
    }
}