package com.fisk.common.framework.advice;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

/**
 * @author Lock
 * Service的AOP切面，暂无作用，待扩展
 * 针对MybatisPlus，以及有@Service注解的类
 */
@Slf4j
@Aspect
@Component
public class ServiceLogAdvice {

    @Around("within(@org.springframework.stereotype.Service *)" +
            "||within(com.baomidou.mybatisplus.extension.service.IService+)")
    public Object handleLog(ProceedingJoinPoint jp) throws Throwable {
        return jp.proceed();
    }
}