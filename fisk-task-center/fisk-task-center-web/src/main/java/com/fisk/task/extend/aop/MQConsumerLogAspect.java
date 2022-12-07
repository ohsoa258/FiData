package com.fisk.task.extend.aop;

import com.alibaba.druid.util.StringUtils;
import com.alibaba.fastjson.JSON;
import com.fisk.common.core.enums.task.MessageLevelEnum;
import com.fisk.common.core.response.ResultEntity;
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

        TaskLogPO model = null;
        MQBaseDTO data = null;
        String taskName = "",
                taskQueue = "",
                traceId = "",
                spanId = MDCHelper.setSpanId();
        // 获取日志，修改状态
        try {
            Object[] args = joinPoint.getArgs();
            if (args == null) {
                log.error("后台任务：{}, 参数为空", name);
                throw new FkException(ResultEnum.PARAMTER_NOTNULL);
            }
            log.info("切面参数:{}", JSON.toJSONString(args));
            // 获取方法参数
            data = JSON.parseObject((String) args[0], MQBaseDTO.class);
            if (data == null) {
                throw new FkException(ResultEnum.PARAMTER_ERROR);
            }

            // 获取任务信息
            if (data.logId != null) {
                model = mapper.selectById(data.logId);
                if (model != null) {
                    taskName = model.taskName;
                    taskQueue = model.taskQueue;
                }
                log.info("此次调度队列: {},此次队列参数: {}", taskQueue, JSON.toJSONString(args[0]));
            }
            // 设置TraceID
            if (!StringUtils.isEmpty(data.traceId)) {
                traceId = data.traceId;
                MDCHelper.setTraceId(traceId);
            }
            // 如果参数中没有TraceID，则创建
            else {
                traceId = MDCHelper.setTraceId();
            }
        } catch (Exception ex) {
            log.error("任务状态更新失败", ex);
        }

        if (model != null) {
            model.taskStatus = TaskStatusEnum.PROCESSING;
            model.taskSendOk = true;
            model.traceId = traceId;
            mapper.updateById(model);
        }

        if (sendMsg && data.userId != null) {
            WsSessionManager.sendMsgById("【" + traceId + "】【" + taskName + "】后台任务开始处理", data.userId, MessageLevelEnum.MEDIUM);
        }

        //invoke
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
            model.traceId = traceId;
            mapper.updateById(model);
        }

        String outPutMsg = "任务执行完成";
        if (res instanceof ResultEntity<?>) {
            outPutMsg = ((ResultEntity<?>) res).msg;
        }

        if (sendMsg && data.userId != null) {
            WsSessionManager.sendMsgById("【" + traceId + "】【" + taskName + "】后台任务处理完成，处理结果：【" + outPutMsg + "】", data.userId, MessageLevelEnum.HIGH);
        }
        MDCHelper.clear();
        return res;
    }

}
