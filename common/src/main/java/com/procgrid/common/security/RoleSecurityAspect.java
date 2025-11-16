package com.procgrid.common.security;

import com.procgrid.common.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Aspect for handling role-based security annotations
 */
@Aspect
@Component
@RequiredArgsConstructor
public class RoleSecurityAspect {
    
    private final JwtUtil jwtUtil;
    
    @Around("@annotation(producerOnly)")
    public Object checkProducerAccess(ProceedingJoinPoint joinPoint, ProducerOnly producerOnly) throws Throwable {
        List<String> userRoles = jwtUtil.getCurrentUserRoles();
        
        if (!userRoles.contains("PRODUCER") && !userRoles.contains("ADMIN")) {
            throw new AccessDeniedException(producerOnly.message());
        }
        
        return joinPoint.proceed();
    }
    
    @Around("@annotation(buyerOnly)")
    public Object checkBuyerAccess(ProceedingJoinPoint joinPoint, BuyerOnly buyerOnly) throws Throwable {
        List<String> userRoles = jwtUtil.getCurrentUserRoles();
        
        if (!userRoles.contains("BUYER") && !userRoles.contains("ADMIN")) {
            throw new AccessDeniedException(buyerOnly.message());
        }
        
        return joinPoint.proceed();
    }
}