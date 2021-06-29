package com.fisk.task.service;

import com.fisk.common.response.ResultEntity;
import com.fisk.task.dto.BuildNifiFlowDTO;

/**
 * @author gy
 */
public interface IBuildTaskService {

    /**
     * 发布任务
     * @param exchange 交换机名称
     * @param queue 队列名称
     * @param data 任务参数（json）
     * @return 结果
     */
    public ResultEntity<Object> publishTask(String exchange, String queue, BuildNifiFlowDTO data);
}
