package com.fisk.task.extend.aop;

import com.alibaba.fastjson.JSON;
import com.fisk.common.core.enums.task.MessageLevelEnum;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.exception.FkException;
import com.fisk.common.framework.mdc.MDCHelper;
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
        //方法名称
        String name = "";
        //是否发送websocket消息通知
        boolean sendMsg = false;
        try {
            Class<?> tClass = joinPoint.getTarget().getClass();
            name = joinPoint.getSignature().getName();
            Class<?>[] argClass = ((MethodSignature) joinPoint.getSignature()).getParameterTypes();
            //通过反射获得该方法
            Method method = tClass.getMethod(name, argClass);
            //获得任务类型注解
            MQConsumerLog ano = method.getAnnotation(MQConsumerLog.class);
            MDCHelper.setAppLogType(ano.type());
            sendMsg = ano.sendMsg();
        } catch (Exception ex) {
            log.error("方法元数据获取失败");
            ex.printStackTrace();
        }

        //设置TraceID
        String traceId = MDCHelper.setTraceId();

        TaskLogPO model = null;
        MQBaseDTO data = null;
        String taskName = "";
        //获取日志，修改状态
        try {
            Object[] args = joinPoint.getArgs();
            if (args == null) {
                log.error("后台任务：{}, 参数为空", name);
                throw new FkException(ResultEnum.PARAMTER_NOTNULL);
            }
            //获取方法参数
            data = JSON.parseObject((String) args[0], MQBaseDTO.class);
            if (data.logId != null) {
                model = mapper.selectById(data.logId);
                taskName = model == null ? "" : model.taskName;
            }
        } catch (Exception ex) {
            log.error("任务状态更新失败", ex);
            ex.printStackTrace();
        }
        if (data == null || data.userId == null) {
            throw new FkException(ResultEnum.PARAMTER_ERROR);
        }

        if (model != null) {
            model.taskStatus = TaskStatusEnum.PROCESSING;
            model.taskSendOk = true;
            mapper.updateById(model);
        }

        if (sendMsg) {
            WsSessionManager.sendMsgById("【" + traceId + "】【" + taskName + "】后台任务开始处理", data.userId, MessageLevelEnum.MEDIUM);
        }

        log.info("【{}】开始执行", name);
        Object res = null;
        boolean isSuccess = false;
        try {
            res = joinPoint.proceed();
            isSuccess = true;
        } catch (Exception ex) {
            log.error("消费者处理报错，", ex);
            ex.printStackTrace();
        }
        log.info("【{}】执行结束，执行结果【{}】", name, isSuccess);

        TaskStatusEnum statusEnum = isSuccess ? TaskStatusEnum.SUCCESS : TaskStatusEnum.FAILURE;
        if (model != null) {
            model.taskStatus = statusEnum;
            mapper.updateById(model);
        }

        if (sendMsg) {
            WsSessionManager.sendMsgById("【" + traceId + "】【" + taskName + "】后台任务处理完成，处理结果：【" + statusEnum.getName() + "】", data.userId, MessageLevelEnum.HIGH);
        }
        MDCHelper.clear();
        return res;
    }

}
