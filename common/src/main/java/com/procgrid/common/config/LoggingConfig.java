package com.procgrid.common.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.UUID;

/**
 * Logging configuration with request tracking
 */
@Configuration
public class LoggingConfig implements WebMvcConfigurer {
    
    @Bean
    public RequestLoggingInterceptor requestLoggingInterceptor() {
        return new RequestLoggingInterceptor();
    }
    
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(requestLoggingInterceptor());
    }
    
    /**
     * Request logging interceptor to track API calls
     */
    public static class RequestLoggingInterceptor implements HandlerInterceptor {
        private static final Logger logger = LoggerFactory.getLogger(RequestLoggingInterceptor.class);
        private static final String REQUEST_ID_HEADER = "X-Request-ID";
        
        @Override
        public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
            String requestId = request.getHeader(REQUEST_ID_HEADER);
            if (requestId == null) {
                requestId = UUID.randomUUID().toString();
            }
            
            response.setHeader(REQUEST_ID_HEADER, requestId);
            request.setAttribute("requestId", requestId);
            request.setAttribute("startTime", System.currentTimeMillis());
            
            logger.info("Request started - ID: {}, Method: {}, URI: {}, User-Agent: {}",
                    requestId,
                    request.getMethod(),
                    request.getRequestURI(),
                    request.getHeader("User-Agent"));
            
            return true;
        }
        
        @Override
        public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
            String requestId = (String) request.getAttribute("requestId");
            Long startTime = (Long) request.getAttribute("startTime");
            long duration = System.currentTimeMillis() - startTime;
            
            if (ex != null) {
                logger.error("Request completed with error - ID: {}, Status: {}, Duration: {}ms, Error: {}",
                        requestId, response.getStatus(), duration, ex.getMessage());
            } else {
                logger.info("Request completed - ID: {}, Status: {}, Duration: {}ms",
                        requestId, response.getStatus(), duration);
            }
        }
    }
}