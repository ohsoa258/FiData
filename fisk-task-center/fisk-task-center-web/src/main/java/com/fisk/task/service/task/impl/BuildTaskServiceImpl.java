package com.fisk.task.service.task.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.task.dto.MQBaseDTO;
import com.fisk.task.entity.TaskLogPO;
import com.fisk.task.enums.TaskStatusEnum;
import com.fisk.task.mapper.TaskLogMapper;
import com.fisk.task.service.task.IBuildTaskService;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.KafkaException;
import org.springframework.stereotype.Service;

/**
 * @author gy
 */
@Service
@Slf4j
public class BuildTaskServiceImpl extends ServiceImpl<TaskLogMapper, TaskLogPO> implements IBuildTaskService {

    private final int dataMaxLength = 2000;



    @Override
    public ResultEntity<Object> publishTask(String name, String exchange, String queue, MQBaseDTO data) {
        String str = JSON.toJSONString(data);

        TaskLogPO model = new TaskLogPO();
        model.taskName = name;
        model.taskExchange = exchange;
        model.taskQueue = queue;
        model.taskStatus = TaskStatusEnum.TASK_BUILD;
        if (str.length() <= dataMaxLength) {
            model.taskData = str;
        }
        this.save(model);

        data.logId = model.id;
        data.traceId = model.traceId;

        try {
            //rabbitTemplate.convertAndSend(exchange, queue, JSON.toJSONString(data));
            model.taskSendOk = true;
            this.updateById(model);
            return ResultEntityBuild.build(ResultEnum.SUCCESS);
        } catch (KafkaException ex) {
            log.error("【{}】【{}】消息发布失败，消息内容【{}】，ex：", exchange, queue, str, ex);
            model.taskSendOk = false;
            this.updateById(model);
            return ResultEntityBuild.build(ResultEnum.TASK_PUBLISH_ERROR);
        } catch (Exception ex) {
            log.error("系统报错，", ex);
            return ResultEntityBuild.build(ResultEnum.ERROR);
        }
    }

}
