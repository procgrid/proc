package com.procgrid.userservice.config;

import com.procgrid.common.security.JwtAuthenticationEntryPoint;
import com.procgrid.common.security.JwtRequestFilter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Security configuration for User Service
 * Handles JWT authentication with Keycloak integration
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true, securedEnabled = true, jsr250Enabled = true)
@RequiredArgsConstructor
@Slf4j
public class SecurityConfig {

    @Value("${spring.security.oauth2.resourceserver.jwt.jwk-set-uri}")
    private String jwkSetUri;

    @Value("#{'${app.security.cors.allowed-origins}'.split(',')}")
    private List<String> allowedOrigins;

    @Value("#{'${app.security.cors.allowed-methods}'.split(',')}")
    private List<String> allowedMethods;

    @Value("#{'${app.security.cors.allowed-headers}'.split(',')}")
    private List<String> allowedHeaders;

    @Value("${app.security.cors.allow-credentials:true}")
    private boolean allowCredentials;

    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtRequestFilter jwtRequestFilter;

    /**
     * Main security filter chain configuration
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        log.info("Configuring security filter chain for User Service");
        
        return http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(ex -> ex.authenticationEntryPoint(jwtAuthenticationEntryPoint))
            
            // Configure authorization rules
            .authorizeHttpRequests(authz -> authz
                // Public endpoints
                .requestMatchers(HttpMethod.GET, "/actuator/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                .requestMatchers(HttpMethod.GET, "/health", "/info").permitAll()
                
                // Registration endpoints (public)
                .requestMatchers(HttpMethod.POST, "/users/register/producer").permitAll()
                .requestMatchers(HttpMethod.POST, "/users/register/buyer").permitAll()
                .requestMatchers(HttpMethod.POST, "/users/verify-email").permitAll()
                .requestMatchers(HttpMethod.POST, "/users/forgot-password").permitAll()
                .requestMatchers(HttpMethod.POST, "/users/reset-password").permitAll()
                
                // User profile endpoints (authenticated users)
                .requestMatchers(HttpMethod.GET, "/users/profile").hasAnyRole("PRODUCER", "BUYER", "ADMIN")
                .requestMatchers(HttpMethod.PUT, "/users/profile").hasAnyRole("PRODUCER", "BUYER", "ADMIN")
                .requestMatchers(HttpMethod.PATCH, "/users/profile").hasAnyRole("PRODUCER", "BUYER", "ADMIN")
                
                // Producer specific endpoints
                .requestMatchers(HttpMethod.GET, "/users/producers/**").hasAnyRole("PRODUCER", "ADMIN")
                .requestMatchers(HttpMethod.PUT, "/users/producers/**").hasRole("PRODUCER")
                .requestMatchers(HttpMethod.POST, "/users/producers/*/farm-details").hasRole("PRODUCER")
                
                // Buyer specific endpoints
                .requestMatchers(HttpMethod.GET, "/users/buyers/**").hasAnyRole("BUYER", "ADMIN")
                .requestMatchers(HttpMethod.PUT, "/users/buyers/**").hasRole("BUYER")
                .requestMatchers(HttpMethod.POST, "/users/buyers/*/business-details").hasRole("BUYER")
                
                // KYC endpoints
                .requestMatchers(HttpMethod.POST, "/users/kyc/submit").hasAnyRole("PRODUCER", "BUYER")
                .requestMatchers(HttpMethod.GET, "/users/kyc/status").hasAnyRole("PRODUCER", "BUYER")
                .requestMatchers(HttpMethod.PUT, "/users/kyc/approve/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/users/kyc/reject/**").hasRole("ADMIN")
                
                // Address management
                .requestMatchers(HttpMethod.GET, "/users/addresses").hasAnyRole("PRODUCER", "BUYER")
                .requestMatchers(HttpMethod.POST, "/users/addresses").hasAnyRole("PRODUCER", "BUYER")
                .requestMatchers(HttpMethod.PUT, "/users/addresses/**").hasAnyRole("PRODUCER", "BUYER")
                .requestMatchers(HttpMethod.DELETE, "/users/addresses/**").hasAnyRole("PRODUCER", "BUYER")
                
                // Admin endpoints
                .requestMatchers(HttpMethod.GET, "/users/admin/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/users/admin/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/users/admin/**").hasRole("ADMIN")
                
                // File upload endpoints
                .requestMatchers(HttpMethod.POST, "/users/upload/**").hasAnyRole("PRODUCER", "BUYER")
                
                // All other requests require authentication
                .anyRequest().authenticated()
            )
            
            // Configure OAuth2 Resource Server
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt
                    .decoder(jwtDecoder())
                    .jwtAuthenticationConverter(jwtAuthenticationConverter())
                )
            )
            
            // Add custom JWT filter before UsernamePasswordAuthenticationFilter
            .addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class)
            
            .build();
    }

    /**
     * JWT Decoder configuration for Keycloak integration
     */
    @Bean
    public JwtDecoder jwtDecoder() {
        log.info("Configuring JWT decoder with JWK Set URI: {}", jwkSetUri);
        return NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build();
    }

    /**
     * JWT Authentication Converter to extract roles from Keycloak JWT
     */
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        
        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            // Extract realm roles from Keycloak JWT
            Map<String, Object> realmAccess = jwt.getClaimAsMap("realm_access");
            Collection<String> roles = (Collection<String>) realmAccess.get("roles");
            
            log.debug("Extracting roles from JWT: {}", roles);
            
            return roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()))
                .collect(Collectors.toList());
        });
        
        // Set principal name from preferred_username claim
        converter.setPrincipalClaimName("preferred_username");
        
        return converter;
    }

    /**
     * CORS Configuration
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        log.info("Configuring CORS with allowed origins: {}", allowedOrigins);
        
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(allowedOrigins);
        configuration.setAllowedMethods(allowedMethods);
        configuration.setAllowedHeaders(allowedHeaders);
        configuration.setAllowCredentials(allowCredentials);
        configuration.setMaxAge(3600L); // 1 hour
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        
        return source;
    }
}