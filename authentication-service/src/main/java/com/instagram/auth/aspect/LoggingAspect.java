package com.instagram.auth.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class LoggingAspect {

    @Pointcut("execution(* com.instagram.auth.service..*(..))")
    public void serviceLayer() {}

    @Pointcut("execution(* com.instagram.auth.controller..*(..))")
    public void controllerLayer() {}

    @Around("serviceLayer() || controllerLayer()")
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
