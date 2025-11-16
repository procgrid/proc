package com.procgrid.common.config;

import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import feign.Logger;
import feign.RequestInterceptor;
import feign.codec.ErrorDecoder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

/**
 * Feign client configuration for inter-service communication
 */
@Configuration
@EnableFeignClients(basePackages = "com.procgrid")
public class FeignConfig {
    
    @Bean
    Logger.Level feignLoggerLevel() {
        return Logger.Level.BASIC;
    }
    
    @Bean
    public RequestInterceptor requestInterceptor() {
        return requestTemplate -> {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() instanceof Jwt) {
                Jwt jwt = (Jwt) authentication.getPrincipal();
                requestTemplate.header("Authorization", "Bearer " + jwt.getTokenValue());
            }
        };
    }
    
    @Bean
    public ErrorDecoder errorDecoder() {
        return new FeignErrorDecoder();
    }
    
    /**
     * Custom error decoder for Feign clients
     */
    public static class FeignErrorDecoder implements ErrorDecoder {
        private final ErrorDecoder defaultErrorDecoder = new Default();
        
        @Override
        public Exception decode(String methodKey, feign.Response response) {
            switch (response.status()) {
                case 400:
                    return new IllegalArgumentException("Bad Request: " + methodKey);
                case 401:
                    return new SecurityException("Unauthorized: " + methodKey);
                case 403:
                    return new SecurityException("Forbidden: " + methodKey);
                case 404:
                    return new IllegalArgumentException("Not Found: " + methodKey);
                case 500:
                    return new RuntimeException("Internal Server Error: " + methodKey);
                default:
                    return defaultErrorDecoder.decode(methodKey, response);
            }
        }
    }
}