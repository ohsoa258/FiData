package com.fisk.common.mdc;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * @author gy
 */
@Aspect
@Component
public class TraceAspect {
    @Pointcut("@annotation(com.fisk.common.mdc.TraceType)")

    public void traceType() {
    }

    @Around("traceType()")
    public Object doAroundDeviceControl(ProceedingJoinPoint joinPoint) throws Throwable {
        try {
            Class<?> tClass = joinPoint.getTarget().getClass();
            String name = joinPoint.getSignature().getName();
            Class<?>[] argClass = ((MethodSignature) joinPoint.getSignature()).getParameterTypes();
            //通过反射获得该方法
            Method method = tClass.getMethod(name, argClass);
            //获得该注解
            TraceType ano = method.getAnnotation(TraceType.class);
            MDCHelper.setAppLogType(ano.type());
            MDCHelper.setFunction(name);
            MDCHelper.setClass(tClass.getName());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return joinPoint.proceed();
    }
}
