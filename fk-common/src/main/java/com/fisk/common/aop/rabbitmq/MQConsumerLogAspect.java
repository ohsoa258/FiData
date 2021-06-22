package com.fisk.common.aop.rabbitmq;

import com.fisk.common.mdc.MDCHelper;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * @author gy
 */
@Aspect
@Component
@Slf4j
public class MQConsumerLogAspect {
    @Pointcut("@annotation(com.fisk.common.aop.rabbitmq.MQConsumerLog)")

    public void traceType() {
    }

    @Around("traceType()")
    public Object doAroundDeviceControl(ProceedingJoinPoint joinPoint) throws Throwable {
        String name = "";
        try {
            Class<?> tClass = joinPoint.getTarget().getClass();
            name = joinPoint.getSignature().getName();
            Class<?>[] argClass = ((MethodSignature) joinPoint.getSignature()).getParameterTypes();
            //通过反射获得该方法
            Method method = tClass.getMethod(name, argClass);
            //获得该注解
            MQConsumerLog ano = method.getAnnotation(MQConsumerLog.class);
            MDCHelper.setAppLogType(ano.type());
            MDCHelper.setFunction(name);
            MDCHelper.setClass(tClass.getName());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        String code = UUID.randomUUID().toString();
        log.info("【{}】【{}】【{}】开始执行", LocalDateTime.now(), code, name);
        Object res = joinPoint.proceed();
        log.info("【{}】【{}】【{}】执行结束", LocalDateTime.now(), code, name);
        return res;
    }
}
