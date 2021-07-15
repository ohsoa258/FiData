package com.fisk.task.extend.aop;

import com.alibaba.fastjson.JSON;
import com.fisk.common.exception.FkException;
import com.fisk.common.mdc.MDCHelper;
import com.fisk.common.response.ResultEnum;
import com.fisk.task.dto.MQBaseDTO;
import com.fisk.task.entity.TaskLogPO;
import com.fisk.task.enums.TaskStatusEnum;
import com.fisk.task.mapper.TaskLogMapper;
import com.fisk.task.utils.WsSessionManager;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
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

    @Resource
    TaskLogMapper mapper;

    @Pointcut("@annotation(com.fisk.task.extend.aop.MQConsumerLog)")
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
            log.error("方法元数据获取失败");
        }

        TaskLogPO model = null;
        MQBaseDTO data = null;
        String taskName = "";
        //获取日志，修改状态
        try {
            Object[] args = joinPoint.getArgs();
            if (args == null) {
                throw new FkException(ResultEnum.PARAMTER_NOTNULL);
            }
            //获取方法参数
            data = JSON.parseObject((String) args[0], MQBaseDTO.class);
            model = mapper.selectById(data.logId);
            taskName = model == null ? "" : model.taskName;
        } catch (Exception ex) {
            log.error("任务状态更新失败");
        }
        if (data == null || data.userId == null) {
            throw new FkException(ResultEnum.PARAMTER_ERROR);
        }

        if (model != null) {
            model.taskStatus = TaskStatusEnum.PROCESSING;
            mapper.updateById(model);
        }

        WsSessionManager.sendMsgById("【" + taskName + "】后台任务开始处理", data.userId);

        String code = UUID.randomUUID().toString();
        log.info("【{}】【{}】【{}】开始执行", LocalDateTime.now(), code, name);
        Object res = null;
        boolean isSuccess = false;
        try {
            res = joinPoint.proceed();
            isSuccess = true;
        } catch (Exception ex) {
            log.error("消费者处理报错，", ex);
        }
        log.info("【{}】【{}】【{}】执行结束，执行结果【{}】", LocalDateTime.now(), code, name, isSuccess);

        TaskStatusEnum statusEnum = isSuccess ? TaskStatusEnum.SUCCESS : TaskStatusEnum.FAILURE;
        if (model != null) {
            model.taskStatus = statusEnum;
            mapper.updateById(model);
        }

        WsSessionManager.sendMsgById("【" + taskName + "】后台任务处理完成，处理结果：【" + statusEnum.getName() + "】", data.userId);
        return res;
    }

}
