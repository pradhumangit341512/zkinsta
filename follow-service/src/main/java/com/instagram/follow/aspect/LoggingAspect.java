package com.instagram.follow.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class LoggingAspect {

    @Around("execution(* com.instagram.follow.service..*(..)) || execution(* com.instagram.follow.controller..*(..))")
    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().toShortString();
        log.info("Entering method: {}", methodName);
        try {
            Object result = joinPoint.proceed();
            log.info("Exiting method: {}", methodName);
            return result;
        } catch (Exception e) {
            log.error("Exception in method: {} - {}", methodName, e.getMessage());
            throw e;
        }
    }
}
