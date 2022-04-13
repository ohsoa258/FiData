package com.fisk.common.framework.advice;

import com.fisk.common.core.constants.SystemConstants;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.core.utils.JwtUtils;
import com.fisk.common.framework.exception.FkException;
import com.fisk.common.framework.mdc.MDCHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;

/**
 * @author guoyu
 * Controller的AOP切面，用来做日志记录
 * 针对@RestController注解的类生效
 */
@Slf4j
@Aspect
@Component
public class ControllerLogAdvice {

    @Value("${fk.jwt.key}")
    String secret;

    @Around("within(@org.springframework.web.bind.annotation.RestController *)")
    public Object handleLog(ProceedingJoinPoint jp) throws Throwable {
        // 设置TraceID
        String traceId = MDCHelper.setTraceId();
        try {
            // get token
            ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            HttpServletRequest request = requestAttributes.getRequest();
            String token = request.getHeader(SystemConstants.HTTP_HEADER_AUTH);
            long userId = 0L;
            if (StringUtils.isNotEmpty(token)) {
                userId = JwtUtils.getUserIdByToken(secret, token);
            }
            // log
            log.debug("IP: 【{}】, Port: 【{}】, 请求地址: 【{}】, 用户ID: 【{}】, Token: 【{}】", request.getRemoteAddr(), request.getRemotePort(), request.getRequestURL(), userId, token);
            log.debug("控制器【{}】准备调用，参数: {}", jp.getSignature(), Arrays.toString(jp.getArgs()));
            long execTime = System.currentTimeMillis();
            // 调用切点方法
            Object result = jp.proceed();
            if (result instanceof ResultEntity) {
                ResultEntity resultEntity = (ResultEntity) result;
                resultEntity.traceId = traceId;
            }
            log.debug("控制器【{}】调用成功，执行耗时: {} ms", jp.getSignature(), System.currentTimeMillis() - execTime);
            MDCHelper.clear();
            return result;
        } catch (Throwable throwable) {
            log.debug("控制器【{}】执行失败，原因：{}", jp.getSignature(), throwable.toString(), throwable);
            //出现异常不清楚mdc，等待全局异常拦截处清理
            if (throwable instanceof FkException) {
                throw throwable;
            } else {
                throw new FkException(ResultEnum.ERROR, throwable);
            }
        }
    }
}