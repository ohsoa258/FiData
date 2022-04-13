package com.fisk.task.service.task;

import com.fisk.common.core.response.ResultEntity;
import com.fisk.task.dto.MQBaseDTO;

public interface IBuildKfkTaskService {

    /**
     * 消费投递到队列
     * @param name 提示框内容
     * @param exchange 无
     * @param queue 队列名称
     * @param data data数据
     * @return
     */
    ResultEntity<Object> publishTask(String name, String exchange, String queue, MQBaseDTO data);
}
