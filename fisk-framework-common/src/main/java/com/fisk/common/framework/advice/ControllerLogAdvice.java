package com.fisk.common.framework.advice;

import com.alibaba.fastjson.JSON;
import com.fisk.common.core.constants.SystemConstants;
import com.fisk.common.core.constants.TraceConstant;
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
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

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
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        // log var
        long userId = 0L;
        int remotePort = 0;
        String remoteAddr = "",
                requestUrl = "",
                token = "",
                traceId = "",
                spanId = MDCHelper.setSpanId();

        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (requestAttributes != null) {
            HttpServletRequest request = requestAttributes.getRequest();
            token = request.getHeader(SystemConstants.HTTP_HEADER_AUTH);
            if (StringUtils.isNotEmpty(token)) {
                userId = JwtUtils.getUserIdByToken(secret, token);
            }
            remoteAddr = request.getRemoteAddr();
            remotePort = request.getRemotePort();
            requestUrl = request.getRequestURI();
            traceId = request.getHeader(TraceConstant.HTTP_HEADER_TRACE);
            if (StringUtils.isNotEmpty(traceId)) {
                MDCHelper.setTraceId(traceId);
            }
        }

        try {

            // get method meta
            Class<?> targetClass = jp.getTarget().getClass();
            Class<?>[] argClass = ((MethodSignature) jp.getSignature()).getParameterTypes();
            Method method = targetClass.getMethod(jp.getSignature().getName(), argClass);
            ControllerAOPConfig ano = method.getAnnotation(ControllerAOPConfig.class);

            // log
            log.debug("IP: 【{}】, Port: 【{}】, 请求地址: 【{}】, 用户ID: 【{}】, Token: 【{}】", remoteAddr, remotePort, requestUrl, userId, token);

            if (Objects.nonNull(ano) && !ano.printParams()) {
                log.debug("方法准备调用, 方法详情【{}】", jp.getSignature());
            } else {
                // get method params
                Map<String, Object> args = new HashMap<>();
                String[] argNames = ((MethodSignature) jp.getSignature()).getParameterNames();
                for (int i = 0; i < argNames.length; i++) {
                    args.put(argNames[i] + "参数", jp.getArgs()[i]);
                }
                log.debug("方法准备调用, 方法详情【{}】, 参数: {}", jp.getSignature(), JSON.toJSONString(args));
            }

            long execTime = System.currentTimeMillis();
            // 调用切点方法
            Object result = jp.proceed();
            // 拦截返回值，设置TraceID
            if (result instanceof ResultEntity) {
                ResultEntity resultEntity = (ResultEntity) result;
                resultEntity.traceId = traceId;
            }
            stopWatch.stop();
            log.debug("控制器【{}】调用成功，执行耗时: {} ms", jp.getSignature(), stopWatch.getTotalTimeMillis());
            MDCHelper.clear();
            return result;
        } catch (Throwable throwable) {
            stopWatch.stop();
            log.debug("控制器【{}】执行失败，执行耗时: {} ms，原因：{}", jp.getSignature(), stopWatch.getTotalTimeMillis(), throwable.toString(), throwable);
            //出现异常不清除mdc，等待全局异常拦截处清理
            if (throwable instanceof FkException) {
                throw throwable;
            } else {
                throw new FkException(ResultEnum.ERROR, throwable);
            }
        }
    }
}