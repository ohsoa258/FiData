package com.fisk.dataservice.aop;

import com.alibaba.fastjson.JSON;
import com.fisk.common.core.enums.datamanage.DataOperationTypeEnum;
import com.fisk.common.core.enums.datamanage.MetaDataSyncTypeEnum;
import com.fisk.common.framework.mdc.MDCHelper;
import com.fisk.dataservice.dto.api.ApiRegisterDTO;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.List;

/**
 * @author jx
 */
@Aspect
@Component
@Slf4j
public class MetaDataSyncAspect {

    @Pointcut("@annotation(com.fisk.dataservice.aop.MetaDataSync)")
    public void traceType() {
    }

    @Around("traceType()")
    public Object doAroundDeviceControl(ProceedingJoinPoint joinPoint) throws Throwable {
        // 方法名称
        String name = "";
        //invoke
        Object res = null;
        // 获取方法元数据
        MetaDataSync ano = null;
        try {

            Class<?> tClass = joinPoint.getTarget().getClass();
            name = joinPoint.getSignature().getName();
            Class<?>[] argClass = ((MethodSignature) joinPoint.getSignature()).getParameterTypes();
            //通过反射获得该方法
            Method method = tClass.getMethod(name, argClass);
            //获得任务类型注解
            ano = method.getAnnotation(MetaDataSync.class);
            MDCHelper.setAppLogType(ano.type());
        } catch (Exception ex) {
            log.error("方法元数据获取失败");
            ex.printStackTrace();
        }
        Object[] args = joinPoint.getArgs();
        res = joinPoint.proceed();
        try {
            if (ano.syncType().equals(MetaDataSyncTypeEnum.DATA_CONSUME_API)){
                if(ano.operationType().equals(DataOperationTypeEnum.ADD)||ano.operationType().equals(DataOperationTypeEnum.UPDATE)){
                    //修改
                    ApiRegisterDTO apiRegisterDTO =JSON.parseObject((String) args[0], ApiRegisterDTO.class);
                    apiRegisterDTO.apiDTO.getApiName();

                }else {
                    //删除
                }
            }else if (ano.syncType().equals(MetaDataSyncTypeEnum.DATA_CONSUME_VIEW)){

            }else if (ano.syncType().equals(MetaDataSyncTypeEnum.DATA_CONSUME_DATABASE_SYNC)){

            }else {

            }
        }catch (Exception ex){


        }


        return res;
    }

}
