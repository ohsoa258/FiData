package com.fisk.task.service.task;

import com.fisk.common.response.ResultEntity;
import com.fisk.task.dto.MQBaseDTO;
import com.fisk.task.dto.task.BuildNifiFlowDTO;

/**
 * @author gy
 */
public interface IBuildTaskService {

    /**
     * 发布任务
     *
     * @param name 任务名称
     * @param exchange 交换机名称
     * @param queue    队列名称
     * @param data     任务参数（json）
     * @return 结果
     */
    public ResultEntity<Object> publishTask(String name, String exchange, String queue, MQBaseDTO data);
}
