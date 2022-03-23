package com.fisk.task.service.task;

import com.fisk.common.response.ResultEntity;
import com.fisk.task.dto.MQBaseDTO;

public interface IBuildKfkTaskService {

    ResultEntity<Object> publishTask(String name, String exchange, String queue, MQBaseDTO data);
}
