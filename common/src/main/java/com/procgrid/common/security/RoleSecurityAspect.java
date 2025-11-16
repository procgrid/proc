package com.procgrid.common.security;

import com.procgrid.common.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.List;

/**
 * Aspect for handling role-based security annotations
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class RoleSecurityAspect {
    
    private final JwtUtil jwtUtil;
    
    @Around("@annotation(com.procgrid.common.security.ProducerOnly)")
    public Object checkProducerAccess(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        ProducerOnly annotation = method.getAnnotation(ProducerOnly.class);
        
        List<String> userRoles = jwtUtil.getCurrentUserRoles();
        
        if (!userRoles.contains("ROLE_PRODUCER") && !userRoles.contains("ROLE_ADMIN")) {
            log.warn("Access denied for user with roles: {} - Producer access required", userRoles);
            throw new AccessDeniedException(annotation.message());
        }
        
        log.debug("Producer access granted for user with roles: {}", userRoles);
        return joinPoint.proceed();
    }
    
    @Around("@annotation(com.procgrid.common.security.BuyerOnly)")
    public Object checkBuyerAccess(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        BuyerOnly annotation = method.getAnnotation(BuyerOnly.class);
        
        List<String> userRoles = jwtUtil.getCurrentUserRoles();
        
        if (!userRoles.contains("ROLE_BUYER") && !userRoles.contains("ROLE_ADMIN")) {
            log.warn("Access denied for user with roles: {} - Buyer access required", userRoles);
            throw new AccessDeniedException(annotation.message());
        }
        
        log.debug("Buyer access granted for user with roles: {}", userRoles);
        return joinPoint.proceed();
    }
    
    @Around("@annotation(com.procgrid.common.security.AdminOnly)")
    public Object checkAdminAccess(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        AdminOnly annotation = method.getAnnotation(AdminOnly.class);
        
        List<String> userRoles = jwtUtil.getCurrentUserRoles();
        
        if (!userRoles.contains("ROLE_ADMIN")) {
            log.warn("Access denied for user with roles: {} - Admin access required", userRoles);
            throw new AccessDeniedException(annotation.message());
        }
        
        log.debug("Admin access granted for user with roles: {}", userRoles);
        return joinPoint.proceed();
    }
}