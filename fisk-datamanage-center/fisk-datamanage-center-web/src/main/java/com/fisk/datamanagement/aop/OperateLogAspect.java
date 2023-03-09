package com.fisk.datamanagement.aop;

import com.fisk.datamanagement.dto.metadataentityoperationLog.MetaDataEntityOperationLogDTO;
import com.fisk.datamanagement.entity.MetaDataEntityOperationLogPO;
import com.fisk.datamanagement.map.MetaDataEntityOperationLogMap;
import com.fisk.datamanagement.service.IMetaDataEntityOperationLog;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.lang.reflect.Method;
import java.time.LocalDateTime;

/**
 * @author zjy
 * @version 1.0
 * @createTime 2023-03-08 13:58
 * @description AOP 切面类
 */
@Aspect
@Component
public class OperateLogAspect {
    @Resource
    private IMetaDataEntityOperationLog entityOperationLog;
    /**
     * 对所有使用@OperateLog注解的方法进行拦截
     */
    @Pointcut("@annotation(com.fisk.datamanagement.aop.OperateLog)")
    private void pointcut(){
    }

    // 定义环绕在切点前后的操作
    @Around("pointcut()")
    public Object doAround(ProceedingJoinPoint point) {
        Object result = null;

        try {
            // 调用方法，完成具体逻辑
            result = point.proceed();
        } catch (Throwable e) {
            e.printStackTrace();
        }finally {
            // 保存操作日志
            saveLog(point);
        }


        return result;
    }

    private void saveLog(ProceedingJoinPoint point) {
        MetaDataEntityOperationLogDTO sysLog= new MetaDataEntityOperationLogDTO();
        // 设置用户名
        sysLog.setCreateUser("admin");

        // 从切点获取方法签名
        MethodSignature signature = (MethodSignature) point.getSignature();
        // 获取方法
        Method method = signature.getMethod();
        // 获取日志注解
        OperateLog annotation = method.getAnnotation(OperateLog.class);


        // 获取日志注解上的描述，并且设置日志行为描述
        if (annotation != null) {
            sysLog.setOperationType(annotation.value());
        }

        // 获取类名
        String className = point.getTarget().getClass().getName();
        // 获取方法名
        String methodName = method.getName();
        // 设置方法全名
        //sysLog.setMethod(className + "." + methodName + "()");

        sysLog.setBeforeChange(methodName);

        // 获取参数
        Object[] args = point.getArgs();
        // 读取参数名
        LocalVariableTableParameterNameDiscoverer l = new LocalVariableTableParameterNameDiscoverer();
        String[] paramNames = l.getParameterNames(method);
        // 设置参数
        if (args != null && paramNames != null) {
            StringBuilder params = new StringBuilder();
            for (int i = 0; i < paramNames.length; i++) {
                params.append(paramNames[i]);
                params.append(":");
                params.append(args[i].toString());
                params.append(", ");
            }
            sysLog.setAfterChange(params.toString());
        }
        sysLog.setMetadataEntityId("4");
        sysLog.setCreateTime(LocalDateTime.now());
        entityOperationLog.addOperationLog(sysLog);
    }


}
