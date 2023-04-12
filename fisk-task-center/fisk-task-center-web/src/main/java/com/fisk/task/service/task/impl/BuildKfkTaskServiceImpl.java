package com.fisk.task.service.task.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.core.constants.TraceConstant;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.mdc.MDCHelper;
import com.fisk.task.dto.MQBaseDTO;
import com.fisk.task.entity.TaskLogPO;
import com.fisk.task.enums.TaskStatusEnum;
import com.fisk.task.mapper.TaskLogMapper;
import com.fisk.task.service.task.IBuildKfkTaskService;
import com.fisk.task.utils.KafkaTemplateHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.KafkaException;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * @author gy
 */
@Service
@Slf4j
public class BuildKfkTaskServiceImpl extends ServiceImpl<TaskLogMapper, TaskLogPO> implements IBuildKfkTaskService {

    @Resource
    KafkaTemplateHelper kafkaTemplateHelper;

    @Override
    public ResultEntity<Object> publishTask(String name, String exchange, String queue, MQBaseDTO data) {
        setTraceID(data);

        String str = JSON.toJSONString(data);

        TaskLogPO model = new TaskLogPO();
        model.taskName = name;
        model.taskExchange = exchange;
        model.taskQueue = queue;
        model.taskStatus = TaskStatusEnum.TASK_BUILD;
        int dataMaxLength = 2000;
        if (str.length() <= dataMaxLength) {
            model.taskData = str;
        }
        this.save(model);

        data.logId = model.id;
        str = JSON.toJSONString(data);
        try {
            kafkaTemplateHelper.sendMessageSync(queue, str);
            model.taskSendOk = true;
            this.updateById(model);
            return ResultEntityBuild.build(ResultEnum.SUCCESS);
        } catch (KafkaException ex) {
            log.error("【{}】消息发布失败，消息内容【{}】，ex：", queue, str, ex);
            model.taskSendOk = false;
            this.updateById(model);
            return ResultEntityBuild.build(ResultEnum.TASK_PUBLISH_ERROR);
        } catch (Exception ex) {
            log.error("系统报错，", ex);
            return ResultEntityBuild.build(ResultEnum.ERROR);
        }
    }

    private void setTraceID(MQBaseDTO data) {
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (requestAttributes != null) {
            HttpServletRequest request = requestAttributes.getRequest();
            data.traceId = request.getHeader(TraceConstant.HTTP_HEADER_TRACE);
            data.spanId = MDCHelper.setSpanId();
        }
    }
}
