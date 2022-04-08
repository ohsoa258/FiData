package com.fisk.common.framework.advice;

import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.exception.FkException;
import com.fisk.common.framework.mdc.MDCHelper;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * @author Lock
 * 统一日志记录,利用AOP拦截所有的service方法,对执行结果日志进行记录
 */
@Slf4j
@Aspect
@Component
public class CommonLogAdvice {

    @Around("within(@org.springframework.stereotype.Service *)" +
            "||within(com.baomidou.mybatisplus.extension.service.IService+)")
    public Object handleExceptionLog(ProceedingJoinPoint jp) throws Throwable {
        MDCHelper.setTraceId();
        try {
            log.debug("【{}】方法准备调用，参数: {}", jp.getSignature(), Arrays.toString(jp.getArgs()));
            long execTime = System.currentTimeMillis();
            // 调用切点方法
            Object result = jp.proceed();
            log.debug("【{}】方法调用成功，执行耗时: {} ms", jp.getSignature(), System.currentTimeMillis() - execTime);
            MDCHelper.clear();
            return result;
        } catch (Throwable throwable) {
            log.debug("【{}】方法执行失败，原因：{}", jp.getSignature(), throwable.toString(), throwable);
            if (throwable instanceof FkException) {
                throw throwable;
            } else {
                throw new FkException(ResultEnum.ERROR, throwable);
            }
        }
    }
}